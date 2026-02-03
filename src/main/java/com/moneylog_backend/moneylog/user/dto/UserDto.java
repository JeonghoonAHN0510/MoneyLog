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
    private Integer userId;
    private Integer accountId;
    private String name;
    private String id;
    private String password;
    private String email;
    private String phone;
    private boolean gender;
    private RoleEnum role;
    private String profileImageUrl;
    private MultipartFile uploadFile;
    private StatusEnum status;
    private ProviderEnum provider;
    private String providerId;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer bankId;
    private String bankName;
    private String accountNumber;

    public UserEntity toEntity () {
        return UserEntity.builder()
                         .userId(this.userId)
                         .accountId(this.accountId)
                         .name(this.name)
                         .loginId(this.id)
                         .password(this.password)
                         .email(this.email)
                         .phone(this.phone)
                         .gender(this.gender)
                         .role(RoleEnum.USER)
                         .profileImageUrl(this.profileImageUrl)
                         .status(this.status)
                         .provider(this.provider)
                         .providerId(this.providerId)
                         .lastLoginAt(this.lastLoginAt)
                         .build();
    }
}