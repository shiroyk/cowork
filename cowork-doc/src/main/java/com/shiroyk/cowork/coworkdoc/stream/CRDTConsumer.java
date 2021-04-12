package com.shiroyk.cowork.coworkdoc.stream;

import com.shiroyk.cowork.coworkcommon.dto.Operation;
import com.shiroyk.cowork.coworkdoc.service.DocNodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.Message;

@Slf4j
@RequiredArgsConstructor
@EnableBinding(Sink.class)
public class CRDTConsumer {
    private final DocNodeService docNodeService;

    @StreamListener(Sink.INPUT)
    public void consume(Message<Operation> opMsg) {
        docNodeService.applyDelta(opMsg.getPayload());
    }

}
