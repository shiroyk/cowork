package com.shiroyk.cowork.coworkcollab.stream;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface MsgSink {
    String INPUT = "msg-input";

    @Input(INPUT)
    SubscribableChannel input();
}
