package com.jeonghoon.moneylog.model.dto;

import com.jeonghoon.moneylog.model.entity.UserEntity;

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
    private int user_pk;
    private String user_id;
    private String user_pwd;
    private String user_nickname;
    private String user_email;
    private String user_role;
    private String user_address1;
    private String user_address2;
    private LocalDateTime create_date;
    private LocalDateTime update_date;


    public UserEntity toEntity(){
        return UserEntity.builder()
                .user_pk(this.user_pk)
                .user_id(this.user_id)
                .user_nickname(this.user_nickname)
                .user_email(this.user_email)
                .user_role(this.user_role)
                .user_address1(this.user_address1)
                .user_address2(this.user_address2)
                .build();
    } // func end
} // class end