package com.shiroyk.cowork.coworkcollab.stream;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.cloud.stream.annotation.EnableBinding;

@Data
@AllArgsConstructor
@EnableBinding(MsgSource.class)
public class MsgProducer {
    private final MsgSource msgSource;
}
