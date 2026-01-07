package com.moneylog_backend.moneylog.category.entity;

import com.moneylog_backend.global.common.BaseTime;
import com.moneylog_backend.global.type.CategoryEnum;
import com.moneylog_backend.moneylog.category.dto.CategoryDto;

import org.hibernate.annotations.DynamicInsert;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "category")
@Data
@Builder
@DynamicInsert
@AllArgsConstructor
@NoArgsConstructor
public class CategoryEntity extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private int category_id;
    @Column(name = "user_id", columnDefinition = "INT UNSIGNED NOT NULL")
    private int user_id;
    @Column(columnDefinition = "VARCHAR(50) NOT NULL")
    private String name;
    @Column(columnDefinition = "ENUM('INCOME', 'EXPENSE') NOT NULL")
    @Enumerated(EnumType.STRING)
    private CategoryEnum type;

    public CategoryDto toDto () {
        return CategoryDto.builder()
                          .category_id(this.category_id)
                          .user_id(this.user_id)
                          .name(this.name)
                          .type(this.type)
                          .created_at(this.getCreated_at())
                          .updated_at(this.getUpdated_at())
                          .build();
    } // func end
} // class end