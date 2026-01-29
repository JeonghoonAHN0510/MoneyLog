package com.moneylog_backend.moneylog.user.dto;

import com.moneylog_backend.global.type.ProviderEnum;
import com.moneylog_backend.global.type.RoleEnum;
import com.moneylog_backend.global.type.StatusEnum;
import com.moneylog_backend.moneylog.user.entity.UserEntity;

import org.springframework.web.multipart.MultipartFile;

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
    private Integer user_id;
    private Integer account_id;
    private String name;
    private String id;
    private String password;
    private String email;
    private String phone;
    private boolean gender;
    private RoleEnum role;
    private String profile_image_url;
    private MultipartFile upload_file;
    private StatusEnum status;
    private ProviderEnum provider;
    private String provider_id;
    private LocalDateTime last_login_at;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    private Integer bank_id;
    private String bank_name;
    private String account_number;

    public UserEntity toEntity () {
        return UserEntity.builder()
                         .user_id(this.user_id)
                         .account_id(this.account_id)
                         .name(this.name)
                         .loginId(this.id)
                         .password(this.password)
                         .email(this.email)
                         .phone(this.phone)
                         .gender(this.gender)
                         .role(this.role == null ? RoleEnum.USER : this.role)
                         .profile_image_url(this.profile_image_url)
                         .status(this.status)
                         .provider(this.provider)
                         .provider_id(this.provider_id)
                         .last_login_at(this.last_login_at)
                         .build();
    }
}