package com.shiroyk.cowork.coworkcollab.stream;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.cloud.stream.annotation.EnableBinding;

@Data
@AllArgsConstructor
@EnableBinding(CursorSource.class)
public class CursorProducer {
    private final CursorSource cursorSource;
}
