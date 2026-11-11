package com.example.ingles_app.DB;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.ingles_app.entities.Category;
import com.example.ingles_app.entities.Flashcards;
import com.example.ingles_app.entities.ProgressEntity;
import com.example.ingles_app.entities.ShownCard;

@Database(entities = {Category.class, Flashcards.class, ProgressEntity.class, ShownCard.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {

    public abstract VocabularyDAO vocabularyDAO();
    private static volatile AppDatabase INSTANCE;
    public static AppDatabase getINSTANCE(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "vocabulary_offline_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
