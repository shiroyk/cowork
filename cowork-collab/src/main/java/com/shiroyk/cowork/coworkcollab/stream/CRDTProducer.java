package com.shiroyk.cowork.coworkcollab.stream;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.cloud.stream.annotation.EnableBinding;

@Data
@AllArgsConstructor
@EnableBinding(CRDTSource.class)
public class CRDTProducer {
    private final CRDTSource crdtSource;
}
