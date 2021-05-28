package com.shiroyk.cowork.coworkauth.model;

import com.shiroyk.cowork.coworkcommon.constant.Role;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

@Setter
@Getter
public class User implements UserDetails {
    private String id;
    private String username;
    private String email;
    private String password;
    private Role role;
    private Boolean isEnable;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null) {
            role = Role.Normal;
        }
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role.name()));
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isEnable == null || isEnable;
    }

}
