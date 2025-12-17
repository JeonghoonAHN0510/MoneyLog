package com.moneylog_backend.moneylog.model.dto;

import com.moneylog_backend.moneylog.model.entity.UserEntity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private int user_pk;
    private String name;
    private String id;
    private String password;
    private String email;
    private String phone;
    private boolean gender;
    private String profile_image_url;
    private String status;
    private String provider;
    private String provider_id;
    private LocalDateTime last_login_at;
    private LocalDateTime create_at;
    private LocalDateTime update_at;

    public UserEntity toEntity(){
        return UserEntity.builder()
                .user_pk(this.user_pk)
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