package doc.service.stream

import com.fasterxml.jackson.databind.ObjectMapper
import doc.api.constants.Constants
import doc.api.dto.CollabMessage
import doc.api.dto.Event
import doc.service.config.ConsumerProperties
import doc.service.entity.DocEntity
import doc.service.entity.DocNode
import doc.service.service.DocNodeService
import doc.service.service.DocService
import io.nats.client.Connection
import io.nats.client.JetStreamSubscription
import io.nats.client.Message
import io.nats.client.PullSubscribeOptions
import io.nats.client.api.RetentionPolicy
import io.nats.client.api.StreamConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.msgpack.jackson.dataformat.MessagePackFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.Executors
import kotlin.jvm.optionals.getOrNull

/**
 * doc update event consume
 */
@Service
open class DocUpdateEventConsumer(
    private val nc: Connection,
    private val env: Environment,
    private val service: DocService,
    private val conf: ConsumerProperties,
    private val nodeService: DocNodeService,
    private val transaction: TransactionTemplate
) : InitializingBean, DisposableBean {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
    private val dispatcher = Executors.newFixedThreadPool(conf.thread).asCoroutineDispatcher()
    private val mapper = ObjectMapper(MessagePackFactory())
    private val hostname
        get() = if (env.matchesProfiles("dev")) "dev"
        else System.getenv("HOSTNAME")
    private lateinit var sub: JetStreamSubscription

    override fun afterPropertiesSet() {
        val jsm = nc.jetStreamManagement()
        val sc = StreamConfiguration.builder()
            .name(Constants.STREAM_PERSISTENCE)
            .subjects(Event.Update.subject)
            .retentionPolicy(RetentionPolicy.Interest)
            .maxAge(conf.maxAge)
            .build()
        if (!jsm.streamNames.contains(sc.name)) {
            jsm.addStream(sc)
        }

        val op = PullSubscribeOptions.builder().durable("persistence-${hostname}").build()
        sub = nc.jetStream().subscribe(Event.Update.subject, op)

        log.info("Start doc event consume {} properties - {}", op.durable, conf)
        CoroutineScope(dispatcher).launch {
            val wait = conf.wait.toMillis()
            while (true) {
                if (!sub.isActive) break
                val list = sub.fetch(conf.batch, wait)
                log.debug("fetch doc updates: ${list.size}")
                if (list.isNotEmpty()) launch { persistence(list) }
                if (list.size < conf.batch) delay(wait)
            }
        }
    }

    /**
     * persistence the update message
     */
    private fun persistence(list: List<Message>) {
        val msg = list.map { mapper.readValue(it.data, CollabMessage::class.java) }
        val nodes = msg.filter { it.data != null }
            .map { DocNode(it.did, it.uid, it.data!!) }

        transaction.executeWithoutResult { tans ->
            runCatching {
                nodeService.insert(nodes)
                val now = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
                val nowData = mapper.writeValueAsBytes(now)
                val saved = nodes.groupBy { it.did }
                    .mapValues { l -> l.value.mapTo(HashSet()) { it.uid } }
                for (kv in saved) {
                    val origin: DocEntity = service.findById(kv.key).getOrNull() ?: continue
                    origin.updatedAt = now
                    // publish save message
                    for (uid in kv.value) {
                        origin.clients[uid]?.updateAt = now
                        val collabMessage = CollabMessage(Event.Save, uid, kv.key, nowData)
                        nc.publish(Event.Save.subject, mapper.writeValueAsBytes(collabMessage))
                    }
                    service.save(origin)
                }

                for (it in list) {
                    it.ack()
                }
            }.onFailure {
                tans.setRollbackOnly()
                log.warn("failed persistence doc updates: {}", it.message)
            }
        }
    }

    override fun destroy() {
        sub.unsubscribe()
        nc.jetStreamManagement().deleteConsumer(sub.consumerInfo.streamName, sub.consumerInfo.name)
    }

}