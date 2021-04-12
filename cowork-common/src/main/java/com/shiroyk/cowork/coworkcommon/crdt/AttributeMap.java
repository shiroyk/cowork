package com.shiroyk.cowork.coworkcommon.crdt;

import java.util.HashMap;
import java.util.Map;

public class AttributeMap extends HashMap<String, Object> {

    public AttributeMap() {
    }

    public AttributeMap(Map<? extends String, ?> m) {
        super(m);
    }
}
