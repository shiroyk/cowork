package com.shiroyk.cowork.coworkcollab.controller;

import com.shiroyk.cowork.coworkcollab.model.EditorCursor;
import com.shiroyk.cowork.coworkcollab.model.UserSession;
import com.shiroyk.cowork.coworkcollab.stream.CRDTProducer;
import com.shiroyk.cowork.coworkcollab.stream.CursorProducer;
import com.shiroyk.cowork.coworkcommon.dto.Operation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
public class WebSocketController {
    private final RedisTemplate<String, String> redisTemplate;
    private final CRDTProducer crdtProducer;
    private final CursorProducer cursorProducer;

    @MessageExceptionHandler(Exception.class)
    public Exception sendInfo(Exception ex) {
        ex.printStackTrace();
        return ex;
    }

    @MessageMapping("/doc/{did}")
    public void sendDocOperation(UserSession user, @DestinationVariable String did, @Payload Operation op) {
        op.setUid(user.getName());
        op.setDid(did);
        log.debug("User {} send {}", user.getName(), op.toString());
        crdtProducer.getCrdtSource().output()
                .send(MessageBuilder.withPayload(op).build());
    }

    @MessageMapping("/doc/{did}/cursor")
    public void sendDocCursor(UserSession user, @DestinationVariable String did, @Payload EditorCursor cursor) {
        cursor.setUid(user.getName());
        log.debug("User {} send {}", user.getName(), cursor);
        cursorProducer.getCursorSource().output()
                .send(MessageBuilder.withPayload(cursor).setHeader("did", did).build());
    }

    /**
     * @Description: 在线用户数
     * @return int
     */
    @GetMapping("/collab/onlineUser")
    public int getOnlineUser() {
        try {
            RedisConnection redisConnection = redisTemplate.getConnectionFactory().getConnection();
            Cursor<byte[]> c = redisConnection.scan(ScanOptions.scanOptions().match("*").count(100).build());
            List<String> keys = new ArrayList<>();
            while (c.hasNext()) {
                keys.add(new String(c.next()));
            }
            c.close();
           return keys.stream()
                   .mapToInt(key -> redisTemplate.opsForSet().members(key).size())
                   .reduce(Integer::sum).getAsInt();
        } catch (Exception ignored) {
        }
        return 0;
    }
}
