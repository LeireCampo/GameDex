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
import com.example.gamedex.data.local.dao.TagDao;
import com.example.gamedex.data.local.dao.UserDao;
import com.example.gamedex.data.local.entity.CustomTag;
import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.data.local.entity.GameCustomTagCrossRef;
import com.example.gamedex.data.local.entity.GameTagCrossRef;
import com.example.gamedex.data.local.entity.Tag;
import com.example.gamedex.data.local.entity.User;

@Database(
        entities = {
                Game.class,
                Tag.class,
                GameTagCrossRef.class,
                User.class,
                CustomTag.class,
                GameCustomTagCrossRef.class
        },
        version = 6, // Incrementar para incluir las nuevas entidades
        exportSchema = false
)
public abstract class GameDexDatabase extends RoomDatabase {

    public abstract GameDao gameDao();
    public abstract TagDao tagDao();
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

            // Crear índices para Tag
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_tags_name` ON `tags` (`name`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_tags_isSystemTag` ON `tags` (`isSystemTag`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_tags_createdAt` ON `tags` (`createdAt`)");

            // Crear índices para GameTagCrossRef
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_game_tag_cross_ref_gameId` ON `game_tag_cross_ref` (`gameId`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_game_tag_cross_ref_tagId` ON `game_tag_cross_ref` (`tagId`)");
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_game_tag_cross_ref_gameId_tagId` ON `game_tag_cross_ref` (`gameId`, `tagId`)");
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

    public static GameDexDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (GameDexDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    GameDexDatabase.class,
                                    "gamedex_database")
                            .addMigrations(MIGRATION_2_3, MIGRATION_3_4) // Solo las migraciones necesarias
                            .fallbackToDestructiveMigration() // Fallback si falla la migración
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}