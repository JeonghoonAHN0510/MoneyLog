package com.moneylog_backend.moneylog.category.entity;

import com.moneylog_backend.global.common.BaseTime;
import com.moneylog_backend.global.type.CategoryType;
import com.moneylog_backend.moneylog.category.dto.CategoryDto;
import com.moneylog_backend.moneylog.user.entity.UserEntity;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "category")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryEntity extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private int category_id;
    @Column(columnDefinition = "VARCHAR(50) NOT NULL")
    private String name;
    @Column(columnDefinition = "ENUM('INCOME', 'EXPENSE') NOT NULL")
    @Enumerated(EnumType.STRING)
    private CategoryType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", columnDefinition = "INT UNSIGNED NOT NULL")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity userEntity;

    public CategoryDto toDto(){
        return CategoryDto.builder()
                .category_id(this.category_id)
                .user_id(this.userEntity != null ? this.userEntity.getUser_id() : 0)
                .name(this.name)
                .type(this.type)
                .created_at(this.getCreated_at())
                .updated_at(this.getUpdated_at())
                .build();
    } // func end
} // class end