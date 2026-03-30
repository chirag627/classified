package com.classified.app.service;

import com.classified.app.exception.ResourceNotFoundException;
import com.classified.app.model.Category;
import com.classified.app.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public Category getCategoryById(String id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findByActiveTrue();
    }

    @Override
    public List<Category> getTopLevelCategories() {
        return categoryRepository.findByParentIdIsNullAndActiveTrue();
    }

    @Override
    public List<Category> getSubCategories(String parentId) {
        return categoryRepository.findByParentIdAndActiveTrue(parentId);
    }

    @Override
    public Category updateCategory(String id, Category category) {
        Category existing = getCategoryById(id);
        existing.setName(category.getName());
        existing.setSlug(category.getSlug());
        existing.setIcon(category.getIcon());
        existing.setActive(category.isActive());
        return categoryRepository.save(existing);
    }

    @Override
    public void deleteCategory(String id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category", "id", id);
        }
        categoryRepository.deleteById(id);
    }
}
