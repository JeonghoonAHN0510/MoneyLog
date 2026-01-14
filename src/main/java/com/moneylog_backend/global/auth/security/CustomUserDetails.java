package com.moneylog_backend.global.auth.security;

import com.moneylog_backend.moneylog.user.entity.UserEntity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
    private final UserEntity userEntity;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities () {
        return Collections.singletonList(new SimpleGrantedAuthority(userEntity.getRole().name()));
    }

    @Override
    public String getPassword () {
        return userEntity.getPassword();
    }

    @Override
    public String getUsername () {
        return userEntity.getLoginId();
    }
}