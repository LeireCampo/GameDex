package com.example.gamedex.data.local.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.gamedex.data.local.dao.GameDao;
import com.example.gamedex.data.local.dao.TagDao;
import com.example.gamedex.data.local.dao.UserDao;
import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.data.local.entity.GameTagCrossRef;
import com.example.gamedex.data.local.entity.Tag;
import com.example.gamedex.data.local.entity.User;

@Database(
        entities = {
                Game.class,
                Tag.class,
                GameTagCrossRef.class,
                User.class
        },
        version = 2, // Incrementamos la versión por los nuevos campos
        exportSchema = false
)
public abstract class GameDexDatabase extends RoomDatabase {

    public abstract GameDao gameDao();
    public abstract TagDao tagDao();
    public abstract UserDao userDao();

    private static volatile GameDexDatabase INSTANCE;

    public static GameDexDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (GameDexDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    GameDexDatabase.class,
                                    "gamedex_database")
                            .fallbackToDestructiveMigration() // Esto reconstruirá la BD si hay cambio de versión
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}