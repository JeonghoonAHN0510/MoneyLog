package com.moneylog_backend.moneylog.user.entity;

import com.moneylog_backend.global.common.BaseTime;
import com.moneylog_backend.global.type.ProviderEnum;
import com.moneylog_backend.global.type.RoleEnum;
import com.moneylog_backend.global.type.StatusEnum;
import com.moneylog_backend.moneylog.user.dto.UserDto;

import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "user")
@Getter
@SuperBuilder
@DynamicInsert
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", columnDefinition = "INT UNSIGNED")
    private Integer userId;
    @Column(name = "account_id", columnDefinition = "INT UNSIGNED")
    private Integer accountId;
    @Column(columnDefinition = "VARCHAR(50) NOT NULL")
    private String name;
    @Column(name = "id", columnDefinition = "VARCHAR(50) NOT NULL UNIQUE")
    private String loginId;
    @Column(columnDefinition = "VARCHAR(255)")
    private String password;
    @Column(columnDefinition = "VARCHAR(100) NOT NULL UNIQUE")
    private String email;
    @Column(columnDefinition = "VARCHAR(20) NOT NULL")
    private String phone;
    @Column(columnDefinition = "BOOLEAN")
    private boolean gender;
    @Column(columnDefinition = "ENUM('ADMIN', 'USER') DEFAULT 'USER'")
    @Enumerated(EnumType.STRING)
    private RoleEnum role;
    @Column(name = "profile_image_url", columnDefinition = "VARCHAR(255)")
    private String profileImageUrl;
    @Column(columnDefinition = "ENUM('ACTIVE', 'DORMANT', 'WITHDRAWN') DEFAULT 'ACTIVE'")
    @Enumerated(EnumType.STRING)
    private StatusEnum status;
    @Column(columnDefinition = "ENUM('LOCAL', 'KAKAO', 'GOOGLE') DEFAULT 'LOCAL'")
    @Enumerated(EnumType.STRING)
    private ProviderEnum provider;
    @Column(name = "provider_id", columnDefinition = "VARCHAR(255)")
    private String providerId;
    @Column(name = "last_login_at", columnDefinition = "DATETIME(6)")
    private LocalDateTime lastLoginAt;

    public void deleteAccountId () {
        this.accountId = null;
    }

    public void setCreatedAccountId (Integer accountId) {
        this.accountId = accountId;
    }

    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public UserDto excludePassword () {
        return UserDto.builder()
                      .userId(this.userId)
                      .accountId(this.accountId)
                      .name(this.name)
                      .id(this.loginId)
                      .password(null)
                      .email(this.email)
                      .phone(this.phone)
                      .gender(this.gender)
                      .role(this.role)
                      .profileImageUrl(this.profileImageUrl)
                      .status(this.status)
                      .provider(this.provider)
                      .providerId(this.providerId)
                      .lastLoginAt(this.lastLoginAt)
                      .createdAt(this.getCreatedAt())
                      .updatedAt(this.getUpdatedAt())
                      .build();
    }

    public UserDto toDto () {
        return UserDto.builder()
                      .userId(this.userId)
                      .accountId(this.accountId)
                      .name(this.name)
                      .id(this.loginId)
                      .password(this.password)
                      .email(this.email)
                      .phone(this.phone)
                      .gender(this.gender)
                      .role(this.role)
                      .profileImageUrl(this.profileImageUrl)
                      .status(this.status)
                      .provider(this.provider)
                      .providerId(this.providerId)
                      .lastLoginAt(this.lastLoginAt)
                      .createdAt(this.getCreatedAt())
                      .updatedAt(this.getUpdatedAt())
                      .build();
    }
}
