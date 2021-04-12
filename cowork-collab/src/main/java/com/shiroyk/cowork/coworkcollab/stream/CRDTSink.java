package com.shiroyk.cowork.coworkcollab.stream;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface CRDTSink {
    String INPUT = "crdt-input";

    @Input(INPUT)
    SubscribableChannel input();
}
