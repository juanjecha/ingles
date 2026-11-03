package com.example.ingles_app.DB;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.ingles_app.entities.Category;
import com.example.ingles_app.entities.Flashcards;

@Database(entities = {Category.class, Flashcards.class}, version = 1)
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
