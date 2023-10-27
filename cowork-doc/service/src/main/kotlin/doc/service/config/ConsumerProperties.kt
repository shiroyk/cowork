package doc.service.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "nats.consumer")
data class ConsumerProperties(
    val thread: Int = 4,
    val batch: Int = 20,
    val wait: Duration = Duration.ofSeconds(5),
    val maxAge: Duration = Duration.ofMinutes(1)
) {
    override fun toString() = "{ thread='$thread', batch='$batch', wait='$wait', maxAge='$maxAge' }"
}