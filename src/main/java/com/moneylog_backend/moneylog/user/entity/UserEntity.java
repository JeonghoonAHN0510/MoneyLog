package com.moneylog_backend.moneylog.user.entity;

import com.moneylog_backend.global.common.BaseTime;
import com.moneylog_backend.global.type.ProviderEnum;
import com.moneylog_backend.global.type.RoleEnum;
import com.moneylog_backend.global.type.StatusEnum;
import com.moneylog_backend.moneylog.user.dto.UserDto;

import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user")
@Data
@Builder
@DynamicInsert
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer user_id;
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer account_id;
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
    @Column(columnDefinition = "VARCHAR(255)")
    private String profile_image_url;
    @Column(columnDefinition = "ENUM('ACTIVE', 'DORMANT', 'WITHDRAWN') DEFAULT 'ACTIVE'")
    @Enumerated(EnumType.STRING)
    private StatusEnum status;
    @Column(columnDefinition = "ENUM('LOCAL', 'KAKAO', 'GOOGLE') DEFAULT 'LOCAL'")
    @Enumerated(EnumType.STRING)
    private ProviderEnum provider;
    @Column(columnDefinition = "VARCHAR(255)")
    private String provider_id;
    @Column(columnDefinition = "DATETIME(6)")
    private LocalDateTime last_login_at;

    public UserDto toDto () {
        return UserDto.builder()
                      .user_id(this.user_id)
                      .account_id(this.account_id)
                      .name(this.name)
                      .id(this.loginId)
                      .password(this.password)
                      .email(this.email)
                      .phone(this.phone)
                      .gender(this.gender)
                      .role(this.role)
                      .profile_image_url(this.profile_image_url)
                      .status(this.status)
                      .provider(this.provider)
                      .provider_id(this.provider_id)
                      .last_login_at(this.last_login_at)
                      .created_at(this.getCreated_at())
                      .updated_at(this.getUpdated_at())
                      .build();
    }
}