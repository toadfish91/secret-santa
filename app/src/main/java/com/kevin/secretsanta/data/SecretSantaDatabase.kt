package com.kevin.secretsanta.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [GroupEntity::class, ParticipantEntity::class, AssignmentEntity::class, ExclusionEntity::class],
    version = 4,
    exportSchema = false
)
abstract class SecretSantaDatabase : RoomDatabase() {
    abstract fun groupDao(): GroupDao
    abstract fun participantDao(): ParticipantDao
    abstract fun assignmentDao(): AssignmentDao
    abstract fun exclusionDao(): ExclusionDao

    companion object {
        @Volatile private var INSTANCE: SecretSantaDatabase? = null

        // v1 → v2: added exclusions table
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS exclusions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        groupId INTEGER NOT NULL,
                        participantIdA INTEGER NOT NULL,
                        participantIdB INTEGER NOT NULL,
                        FOREIGN KEY (groupId) REFERENCES groups(id) ON DELETE CASCADE,
                        FOREIGN KEY (participantIdA) REFERENCES participants(id) ON DELETE CASCADE,
                        FOREIGN KEY (participantIdB) REFERENCES participants(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_exclusions_groupId ON exclusions(groupId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_exclusions_participantIdA ON exclusions(participantIdA)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_exclusions_participantIdB ON exclusions(participantIdB)")
            }
        }

        // v2 → v3: added twoWay column to exclusions
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE exclusions ADD COLUMN twoWay INTEGER NOT NULL DEFAULT 1")
            }
        }

        // v3 → v4: budget + previousGroupId on groups; sourceParticipantId on participants
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `groups` ADD COLUMN budgetDollars INTEGER")
                db.execSQL("ALTER TABLE `groups` ADD COLUMN previousGroupId INTEGER")
                db.execSQL("ALTER TABLE participants ADD COLUMN sourceParticipantId INTEGER")
            }
        }

        fun getInstance(context: Context): SecretSantaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SecretSantaDatabase::class.java,
                    "secret_santa.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
