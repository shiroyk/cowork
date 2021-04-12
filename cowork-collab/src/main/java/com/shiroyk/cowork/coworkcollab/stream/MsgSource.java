package com.shiroyk.cowork.coworkcollab.stream;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface MsgSource {
    @Output("msg-out")
    MessageChannel output();
}
