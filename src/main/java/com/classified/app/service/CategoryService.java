package com.classified.app.service;

import com.classified.app.model.Category;

import java.util.List;

public interface CategoryService {
    Category createCategory(Category category);
    Category getCategoryById(String id);
    List<Category> getAllCategories();
    List<Category> getTopLevelCategories();
    List<Category> getSubCategories(String parentId);
    Category updateCategory(String id, Category category);
    void deleteCategory(String id);
}
