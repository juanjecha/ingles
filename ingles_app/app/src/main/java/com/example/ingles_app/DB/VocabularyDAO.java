package com.example.ingles_app.DB;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.ingles_app.entities.Category;
import com.example.ingles_app.entities.Flashcards;
import com.example.ingles_app.entities.ProgressEntity;
import com.example.ingles_app.entities.ShownCard;

import java.util.List;

@Dao
public interface VocabularyDAO {
    @Query("SELECT * FROM category ORDER BY ord ASC")
    List<Category> getCategories();

    @Query("SELECT * FROM resource WHERE category_id = :categoryID")
    List<Flashcards> getFlashcardsByCat(int categoryID);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategories(List<Category> categories);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFlashcards(List<Flashcards> flashcards);







    @Query("DELETE FROM category WHERE id NOT IN (:ids)")
    void deleteCategoriesNotIN(List<Integer> ids);

    @Query("DELETE FROM resource WHERE category_id = :categoryID AND id NOT IN (:ids)")
    void deleteFlashcardsNotIn(int categoryID, List<Integer> ids);

    @Query("DELETE FROM resource WHERE category_id NOT IN (SELECT id FROM category)")
    void deleteOrphanFlashcards();





    //Progress
    @Query("SELECT * FROM progress")
    List<ProgressEntity> getProgress();

    @Query("SELECT * FROM progress WHERE category_id = :categoryID")
    ProgressEntity getProgressByCat(int categoryID);

    @Query("SELECT * FROM progress WHERE pending_sync = 1")
    List<ProgressEntity> getPendingProgress();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveProgress(ProgressEntity progress);

    @Query("SELECT COUNT(*) FROM resource WHERE CATEGORY_ID = :categoryID")
    int countFLashcards(int categoryID);


    //Flashcards of the day
    @Query("SELECT COUNT(*) FROM shown_card WHERE shown_date = :date")
    int countShownOn(String date);

    @Query("SELECT MAX(shown_at) FROM shown_card")
    Long getLastShownAt();

    @Query("SELECT * FROM resource WHERE category_id = :categoryID " +
            "AND id NOT IN (SELECT flashcard_id FROM shown_card) ORDER BY id ASC LIMIT 1")
    Flashcards getNextUnseen(int categoryID);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void markShown(ShownCard shown);
}
