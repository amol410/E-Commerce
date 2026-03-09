package com.ecom.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryDto {

    @NotBlank(message = "Category name is required")
    private String name;

    private String description;

    private Long parentId;
}
