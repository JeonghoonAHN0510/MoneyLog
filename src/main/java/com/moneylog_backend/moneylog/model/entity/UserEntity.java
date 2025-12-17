package com.moneylog_backend.moneylog.model.entity;

import com.moneylog_backend.moneylog.model.dto.UserDto;

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
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity extends BaseTime{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private int user_pk;
    @Column(columnDefinition = "VARCHAR(50) NOT NULL")
    private String name;
    @Column(columnDefinition = "VARCHAR(50) NOT NULL UNIQUE")
    private String id;
    @Column(columnDefinition = "VARCHAR(255)")
    private String password;
    @Column(columnDefinition = "VARCHAR(100) NOT NULL UNIQUE")
    private String email;
    @Column(columnDefinition = "VARCHAR(20)")
    private String phone;
    @Column(columnDefinition = "BOOLEAN")
    private boolean gender;
    @Column(columnDefinition = "VARCHAR(255)")
    private String profile_image_url;
    @Column(columnDefinition = "ENUM('ACTIVE', 'DORMANT', 'WITHDRAWN') DEFAULT 'ACTIVE'")
    private String status;
    @Column(columnDefinition = "ENUM('LOCAL', 'KAKAO', 'GOOGLE') DEFAULT 'LOCAL'")
    private String provider;
    @Column(columnDefinition = "VARCHAR(255)")
    private String provider_id;
    @Column(columnDefinition = "DATETIME(6)")
    private LocalDateTime last_login_at;

    public UserDto toDto(){
        return UserDto.builder()
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
                .create_at(this.getCreate_at())
                .update_at(this.getUpdate_at())
                .build();
    } // func end
} // class end