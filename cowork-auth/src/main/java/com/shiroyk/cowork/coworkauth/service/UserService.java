package com.shiroyk.cowork.coworkauth.service;

import com.shiroyk.cowork.coworkcommon.constant.ResultCode;
import com.shiroyk.cowork.coworkcommon.dto.APIResponse;
import com.shiroyk.cowork.coworkauth.client.UserFeignClient;
import com.shiroyk.cowork.coworkauth.dto.request.NewUser;
import com.shiroyk.cowork.coworkauth.dto.request.ResetPass;
import com.shiroyk.cowork.coworkauth.model.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {
    private final UserFeignClient userFeignClient;

    @Override
    public User loadUserByUsername(String s) throws UsernameNotFoundException {
        APIResponse<User> response = userFeignClient.getUserInfo(s);
        if ((ResultCode.Ok.equals(response.getCode()))) {
            User user = response.getData();
            if (!user.isEnabled()) {
                throw new DisabledException("该账户已被禁用!");
            }
            return user;
        } else {
            throw new UsernameNotFoundException("用户名或密码错误！");
        }
    }

    public APIResponse<Object> signup(NewUser user) {
        return userFeignClient.signup(user.getUsername(), user.getEmail(), user.getPassword());
    }

    public APIResponse<Object> reset(ResetPass reset) {
        return userFeignClient.reset(reset.getUsername(), reset.getPassword());
    }
}
