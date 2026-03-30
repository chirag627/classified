package com.classified.app.repository;

import com.classified.app.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends MongoRepository<Category, String> {
    Optional<Category> findBySlug(String slug);
    List<Category> findByParentIdIsNullAndActiveTrue();
    List<Category> findByParentIdAndActiveTrue(String parentId);
    List<Category> findByActiveTrue();
}
