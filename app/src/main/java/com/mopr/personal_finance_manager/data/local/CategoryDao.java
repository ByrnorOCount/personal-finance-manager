package com.mopr.personal_finance_manager.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CategoryDao {

    @Insert
    long insert(Category category);

    @Update
    void update(Category category);

    @Query("DELETE FROM categories WHERE id = :id")
    void deleteById(int id);

    @Query("SELECT * FROM categories ORDER BY name ASC")
    LiveData<List<Category>> getAllCategories();

    @Query("SELECT * FROM categories ORDER BY name ASC")
    List<Category> getAllCategoriesSync();

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name ASC")
    LiveData<List<Category>> getCategoriesByType(String type);

    @Query("SELECT * FROM categories WHERE id = :id")
    Category getById(int id);

    @Query("SELECT * FROM categories WHERE parentId = :parentId ORDER BY name ASC")
    LiveData<List<Category>> getSubcategories(int parentId);

    @Query("SELECT * FROM categories WHERE parentId = :parentId ORDER BY name ASC")
    List<Category> getSubcategoriesSync(int parentId);

    @Query("SELECT * FROM categories WHERE name = :name AND type = :type LIMIT 1")
    Category getByNameAndType(String name, String type);

    @Query("DELETE FROM categories")
    void deleteAll();
}
