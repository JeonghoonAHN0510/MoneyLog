package com.moneylog_backend.moneylog.category.entity;

import com.moneylog_backend.global.common.BaseTime;
import com.moneylog_backend.global.type.CategoryEnum;
import com.moneylog_backend.global.type.ColorEnum;
import com.moneylog_backend.moneylog.category.dto.res.CategoryResDto;

import org.hibernate.annotations.DynamicInsert;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "category")
@Getter
@SuperBuilder
@DynamicInsert
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    public void updateDetails(String name, CategoryEnum type, ColorEnum color) {
        if (name != null && !name.isEmpty()) {
            this.name = name;
        }

        if (type != null) {
            this.type = type;
        }

        if (color != null) {
            this.color = color;
        }
    }

    public CategoryResDto toResDto() {
        return CategoryResDto.builder()
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