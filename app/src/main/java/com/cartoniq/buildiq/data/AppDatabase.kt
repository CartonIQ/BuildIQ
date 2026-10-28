package com.cartoniq.buildiq.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [ProjectEntity::class, PriceEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Safe non-destructive column additions to projects
                db.execSQL("ALTER TABLE projects ADD COLUMN clientName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE projects ADD COLUMN clientAddress TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE projects ADD COLUMN clientPhone TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE projects ADD COLUMN clientEmail TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE projects ADD COLUMN notes TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE projects ADD COLUMN laborType TEXT NOT NULL DEFAULT 'NONE'")
                db.execSQL("ALTER TABLE projects ADD COLUMN laborRate REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE projects ADD COLUMN laborHours REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE projects ADD COLUMN laborCost REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE projects ADD COLUMN taxRate REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE projects ADD COLUMN taxAmount REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE projects ADD COLUMN contingencyRate REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE projects ADD COLUMN contingencyAmount REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE projects ADD COLUMN markupRate REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE projects ADD COLUMN markupAmount REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE projects ADD COLUMN currencySymbol TEXT NOT NULL DEFAULT '$'")
                db.execSQL("ALTER TABLE projects ADD COLUMN itemsJson TEXT NOT NULL DEFAULT '[]'")

                // Safe non-destructive column additions to prices
                db.execSQL("ALTER TABLE prices ADD COLUMN label TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE prices ADD COLUMN unit TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE prices ADD COLUMN category TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE prices ADD COLUMN isCustom INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun get(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "buildiq.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
