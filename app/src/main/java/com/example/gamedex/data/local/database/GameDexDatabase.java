package com.example.gamedex.data.local.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.gamedex.data.local.dao.CustomTagDao;
import com.example.gamedex.data.local.dao.GameDao;
import com.example.gamedex.data.local.dao.UserDao;
import com.example.gamedex.data.local.entity.CustomTag;
import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.data.local.entity.GameCustomTagCrossRef;
import com.example.gamedex.data.local.entity.User;

@Database(
        entities = {
                Game.class,
                User.class,
                CustomTag.class,
                GameCustomTagCrossRef.class
        },
        version = 7, // Incrementar versión para reflejar los cambios
        exportSchema = false
)
public abstract class GameDexDatabase extends RoomDatabase {

    public abstract GameDao gameDao();
    public abstract UserDao userDao();
    public abstract CustomTagDao customTagDao();

    private static volatile GameDexDatabase INSTANCE;

    // Migración de versión 2 a 3 (añadir índices)
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Crear índices para Game
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_games_title` ON `games` (`title`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_games_isInLibrary` ON `games` (`isInLibrary`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_games_status` ON `games` (`status`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_games_lastUpdated` ON `games` (`lastUpdated`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_games_isInLibrary_status` ON `games` (`isInLibrary`, `status`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_games_isInLibrary_lastUpdated` ON `games` (`isInLibrary`, `lastUpdated`)");
        }
    };

    // Migración de versión 3 a 4 (añadir nuevo campo suggestedTags)
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Añadir el nuevo campo suggestedTags a la tabla games
            database.execSQL("ALTER TABLE games ADD COLUMN suggestedTags TEXT");
        }
    };

    // Migración de versión 6 a 7 (limpiar sistema Tag viejo)
    static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Eliminar tablas del sistema Tag viejo si existen
            database.execSQL("DROP TABLE IF EXISTS `game_tag_cross_ref`");
            database.execSQL("DROP TABLE IF EXISTS `tags`");

            // Las tablas CustomTag ya existen, no necesitamos crearlas
        }
    };

    public static GameDexDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (GameDexDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    GameDexDatabase.class,
                                    "gamedex_database")
                            .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_6_7)
                            .fallbackToDestructiveMigration() // Fallback si falla la migración
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}