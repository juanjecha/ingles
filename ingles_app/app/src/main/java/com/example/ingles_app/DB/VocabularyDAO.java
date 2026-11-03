package com.example.ingles_app.DB;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.ingles_app.entities.Category;
import com.example.ingles_app.entities.Flashcards;

import java.util.List;

@Dao
public interface VocabularyDAO {
    @Query("SELECT * FROM category ORDER BY ord ASC")
    List<Category> getCategories();

    @Query("SELECT * FROM resource WHERE category_id = :categoryID")
    List<Flashcards> getFlashcardsByCat(int categoryID);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFlashcard(List<Flashcards> flashcards);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategories(List<Category> categories);
}
