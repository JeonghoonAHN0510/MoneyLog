package com.moneylog_backend.moneylog.model.dto;

import com.moneylog_backend.moneylog.common.type.ProviderType;
import com.moneylog_backend.moneylog.common.type.StatusType;
import com.moneylog_backend.moneylog.model.entity.AccountEntity;
import com.moneylog_backend.moneylog.model.entity.UserEntity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private int user_id;
    private int account_id;
    private String name;
    private String id;
    private String password;
    private String email;
    private String phone;
    private boolean gender;
    private String profile_image_url;
    private StatusType status;
    private ProviderType provider;
    private String provider_id;
    private LocalDateTime last_login_at;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    public UserEntity toEntity(AccountEntity accountEntity) {
        return UserEntity.builder()
                .user_id(this.user_id)
                .accountEntity(accountEntity)
                .name(this.name)
                .id(this.id)
                .password(this.password)
                .email(this.email)
                .phone(this.phone)
                .gender(this.gender)
                .profile_image_url(this.profile_image_url)
                .status(this.status)
                .provider(this.provider)
                .provider_id(this.provider_id)
                .last_login_at(this.last_login_at)
                .build();
    } // func end
} // class end