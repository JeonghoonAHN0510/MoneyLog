package com.moneylog_backend.moneylog.user.dto;

import com.moneylog_backend.global.type.ProviderEnum;
import com.moneylog_backend.global.type.RoleEnum;
import com.moneylog_backend.global.type.StatusEnum;
import com.moneylog_backend.moneylog.user.entity.UserEntity;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserDto {
    private Integer userId;
    private Integer accountId;

    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 20, message = "이름은 20자 이내여야 합니다")
    private String name;

    @NotBlank(message = "아이디는 필수입니다")
    @Size(min = 4, max = 20, message = "아이디는 4~20자여야 합니다")
    private String id;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 6, message = "비밀번호는 최소 6자 이상이어야 합니다")
    private String password;

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "전화번호는 필수입니다")
    @Pattern(regexp = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$", message = "올바른 전화번호 형식이 아닙니다")
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

    public UserEntity toEntity (String regexPhone, String profileImageUrl, String encodedPassword) {
        return UserEntity.builder()
                         .accountId(this.accountId)
                         .name(this.name)
                         .loginId(this.id)
                         .password(encodedPassword)
                         .email(this.email)
                         .phone(regexPhone)
                         .gender(this.gender)
                         .role(RoleEnum.USER)
                         .profileImageUrl(profileImageUrl)
                         .status(this.status)
                         .provider(this.provider)
                         .providerId(this.providerId)
                         .lastLoginAt(this.lastLoginAt)
                         .build();
    }
}