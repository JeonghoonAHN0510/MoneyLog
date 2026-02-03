package com.moneylog_backend.moneylog.category.entity;

import com.moneylog_backend.global.common.BaseTime;
import com.moneylog_backend.global.type.CategoryEnum;
import com.moneylog_backend.global.type.ColorEnum;
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
    @Column(name = "category_id", columnDefinition = "INT UNSIGNED")
    private Integer categoryId;
    @Column(name = "user_id", columnDefinition = "INT UNSIGNED NOT NULL")
    private Integer userId;
    @Column(columnDefinition = "VARCHAR(50) NOT NULL")
    private String name;
    @Column(columnDefinition = "ENUM('INCOME', 'EXPENSE') NOT NULL")
    @Enumerated(EnumType.STRING)
    private CategoryEnum type;
    @Column(columnDefinition = "ENUM('RED', 'AMBER', 'YELLOW', 'LIME', 'GREEN', 'EMERALD', 'TEAL', 'CYAN', 'BLUE', 'PURPLE', 'PINK', 'SLATE') DEFAULT 'BLUE'")
    @Enumerated(EnumType.STRING)
    private ColorEnum color;

    public CategoryDto toDto () {
        return CategoryDto.builder()
                          .categoryId(this.categoryId)
                          .userId(this.userId)
                          .name(this.name)
                          .type(this.type)
                          .color(this.color)
                          .createdAt(this.getCreatedAt())
                          .updatedAt(this.getUpdatedAt())
                          .build();
    }
}