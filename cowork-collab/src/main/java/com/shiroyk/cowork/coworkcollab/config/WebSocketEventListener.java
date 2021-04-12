package com.shiroyk.cowork.coworkcollab.config;

import com.shiroyk.cowork.coworkcollab.model.UserSession;
import com.shiroyk.cowork.coworkcollab.model.UserSet;
import com.shiroyk.cowork.coworkcollab.service.DocService;
import com.shiroyk.cowork.coworkcollab.stream.MsgProducer;
import com.shiroyk.cowork.coworkcommon.constant.Permission;
import feign.RetryableException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.Set;

@Slf4j
@Component
@AllArgsConstructor
public class WebSocketEventListener {
    private final RedisTemplate<String, String> redisTemplate;
    private final MsgProducer msgProducer;
    private final DocService docService;

    /***
    * @Description: 用户订阅
    * @Param: [event]
    * @return: void
    */
    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        try {
            final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
            final UserSession user = (UserSession) accessor.getUser();
            final String uid = user.getName();
            final String docId = StringUtils.getFilename(accessor.getDestination());
            redisTemplate.opsForSet().add(docId, uid);

            // 缓存用户信息
            if (Permission.Empty.equals(user.getPermission())) {
                user.setDid(docId);
                user.setPermission(docService.getPermission(uid, docId));
            }

            final Set<String> userSet = redisTemplate.opsForSet().members(docId);

            msgProducer.getMsgSource().output()
                    .send(MessageBuilder.withPayload(UserSet.login(uid, userSet))
                    .setHeader("did", docId).build());

            log.debug("User {} subscribe {}", uid, user);
        } catch (NullPointerException | RetryableException ex) {
            ex.printStackTrace();
            throw new MessagingException("订阅失败！");
        }
    }

    /***
    * @Description: 用户退出
    * @Param: [event]
    * @return: void
    */
    @EventListener
    public void handleWebSocketDisConnectListener(SessionDisconnectEvent event) {
        try {
            final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
            final String session = accessor.getSessionId();
            final UserSession user = (UserSession) accessor.getUser();
            final String uid = user.getName();
            final String docId = user.getDid();

            final Set<String> userSet = redisTemplate.opsForSet().members(docId);

            // 从Redis集合中删除用户
            redisTemplate.opsForSet().remove(docId, uid);

            // 广播用户列表
            userSet.remove(uid);

            msgProducer.getMsgSource().output()
                    .send(MessageBuilder.withPayload(UserSet.logout(uid, userSet))
                            .setHeader("did", docId).build());
            log.info("User {} disconnect {}", user, docId);

            log.debug("User {} session {} disconnect", uid, session);
        } catch (NullPointerException ignored) {
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionUnsubscribeEvent event) {
        log.debug("User {} unsubscribe", event.getUser().getName());
    }
}
