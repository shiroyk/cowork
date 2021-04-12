package com.shiroyk.cowork.coworkcollab.config;

import com.shiroyk.cowork.coworkcollab.model.UserSession;
import com.shiroyk.cowork.coworkcommon.constant.Permission;
import com.shiroyk.cowork.coworkcommon.constant.Role;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.util.StringUtils;

@Slf4j
@AllArgsConstructor
public class WebSocketInterceptor implements ChannelInterceptor {
    private final String jwkSetUrl;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        switch (accessor.getCommand()) {
            case CONNECT:
                // 验证Token
                final String token = accessor.getFirstNativeHeader("Authorization");
                if (StringUtils.isEmpty(token))
                    throw new MessagingException("请先登录！");

                try {
                    final Jwt jwt = NimbusJwtDecoder.withJwkSetUri(jwkSetUrl)
                            .build().decode(token);

                    accessor.setUser(UserSession.create(
                            jwt.getClaimAsString("id"),
                            jwt.getClaimAsStringList("authorities").get(0)));

                    log.debug("User {} connect ", accessor.getUser().getName());
                } catch (JwtException ignored) {
                    throw new MessagingException("Token无效或已过期！");
                }
                break;
            case SEND:
                // 用户发送信息时鉴权
                try {
                    final UserSession user = (UserSession) accessor.getUser();

                    if (Role.Admin.equals(user.getRole())) return message;
                    if (!Permission.ReadWrite.equals(user.getPermission())) {
                        log.info("User {} only has {} permission", user.getName(), user.getPermission());
                        return null;
                    }
                } catch (NullPointerException ex) {
                    ex.printStackTrace();
                    throw new MessagingException("发送失败！");
                }
                break;
        }
        return message;
    }
}
