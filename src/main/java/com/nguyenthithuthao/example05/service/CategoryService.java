package com.nguyenthithuthao.example05.service;

import com.nguyenthithuthao.example05.entity.Category;
import com.nguyenthithuthao.example05.payloads.CategoryDTO;
import com.nguyenthithuthao.example05.payloads.CategoryResponse;

public interface CategoryService {

  CategoryDTO createCategory(Category category);

  CategoryDTO getCategoryById(Long categoryId);

  CategoryResponse getCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

  CategoryDTO updateCategory(Category category, Long categoryId);

  String deleteCategory(Long categoryId);
}