package com.example.data

import androidx.room.migration.Migration

/**
 * Every schema change must bump AppDatabase.version and add a Migration here, so user data
 * survives app updates. Without a matching migration Room fails loudly instead of wiping data.
 *
 * Example for a future version 2:
 *
 * val MIGRATION_1_2 = object : Migration(1, 2) {
 *     override fun migrate(db: SupportSQLiteDatabase) {
 *         db.execSQL("ALTER TABLE expenses ADD COLUMN paymentMethod TEXT NOT NULL DEFAULT ''")
 *     }
 * }
 */
object DatabaseMigrations {
    val ALL: Array<Migration> = arrayOf()
}
