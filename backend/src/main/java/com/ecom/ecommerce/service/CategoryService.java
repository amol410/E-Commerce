package com.ecom.ecommerce.service;

import com.ecom.ecommerce.dto.CategoryDto;
import com.ecom.ecommerce.entity.Category;
import com.ecom.ecommerce.exception.BadRequestException;
import com.ecom.ecommerce.exception.ResourceNotFoundException;
import com.ecom.ecommerce.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    public List<Category> getCategoryTree() {
        return categoryRepository.findByParentIsNull();
    }

    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }

    public Category create(CategoryDto dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new BadRequestException("Category with name '" + dto.getName() + "' already exists");
        }

        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

        if (dto.getParentId() != null) {
            Category parent = getById(dto.getParentId());
            category.setParent(parent);
        }

        return categoryRepository.save(category);
    }

    public Category update(Long id, CategoryDto dto) {
        Category category = getById(id);
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

        if (dto.getParentId() != null) {
            if (dto.getParentId().equals(id)) {
                throw new BadRequestException("Category cannot be its own parent");
            }
            Category parent = getById(dto.getParentId());
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        return categoryRepository.save(category);
    }

    public void delete(Long id) {
        Category category = getById(id);
        categoryRepository.delete(category);
    }
}
