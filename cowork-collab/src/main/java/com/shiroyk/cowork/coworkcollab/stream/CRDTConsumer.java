package com.shiroyk.cowork.coworkcollab.stream;

import com.shiroyk.cowork.coworkcommon.dto.Operation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.user.SimpUserRegistry;

@Slf4j
@AllArgsConstructor
@EnableBinding(CRDTSink.class)
public class CRDTConsumer {
    private final SimpUserRegistry userRegistry;
    private final SimpMessageSendingOperations messagingTemplate;

    @StreamListener(CRDTSink.INPUT)
    public void consume(Operation op) {
        // 如果在这个节点有订阅，给订阅发送消息
        userRegistry.findSubscriptions(subscription -> subscription
                .getDestination().contains(op.getDid()))
                .stream().findFirst().ifPresent(s -> {
            log.debug("Node has subscription {}, send delta {}", s, op);
            messagingTemplate.convertAndSend(s.getDestination(), op);
        });
    }
}
