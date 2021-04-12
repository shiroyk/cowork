package com.shiroyk.cowork.coworkcollab.stream;

import com.shiroyk.cowork.coworkcollab.model.EditorCursor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.user.SimpUserRegistry;

@Slf4j
@AllArgsConstructor
@EnableBinding(CursorSink.class)
public class CursorConsumer {
    private final SimpUserRegistry userRegistry;
    private final SimpMessageSendingOperations messagingTemplate;

    @StreamListener(CursorSink.INPUT)
    public void consume(Message<EditorCursor> message) {
        // 如果在这个节点有订阅，给订阅发送消息
        String did = message.getHeaders().get("did").toString();

        userRegistry.findSubscriptions(subscription -> subscription
                .getDestination().contains(did))
                .stream().findFirst().ifPresent(s -> {
            log.debug("Node has subscription {}, send Msg {}", s, message.getPayload());
            messagingTemplate.convertAndSend(s.getDestination(), message.getPayload());
        });
    }
}
