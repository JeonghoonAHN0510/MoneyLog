package com.moneylog_backend.moneylog.model.entity;

import com.moneylog_backend.moneylog.model.dto.UserDto;

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
    @Column(columnDefinition = "int unsigned")
    private int user_pk;
    @Column(columnDefinition = "varchar(30) not null unique")
    private String user_id;
    @Column(columnDefinition = "varchar(100) not null")
    private String user_pwd;
    @Column(columnDefinition = "varchar(50) not null unique")
    private String user_nickname;
    @Column(columnDefinition = "varchar(50) not null unique")
    private String user_email;
    @Column(columnDefinition = "enum('USER', 'OWNER', 'ADMIN') not null")
    private String user_role;
    @Column(columnDefinition = "varchar(100)")
    private String user_address1;
    @Column(columnDefinition = "varchar(100)")
    private String user_address2;

    public UserDto toDto(){
        return UserDto.builder()
                .user_pk(this.user_pk)
                .user_id(this.user_id)
                .user_nickname(this.user_nickname)
                .user_email(this.user_email)
                .user_role(this.user_role)
                .user_address1(this.user_address1)
                .user_address2(this.user_address2)
                .create_date(this.getCreate_date())
                .update_date(this.getUpdate_date())
                .build();
    } // func end
} // class end