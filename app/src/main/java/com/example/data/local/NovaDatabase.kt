package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        MessageEntity::class,
        UserProfileEntity::class,
        ChatThreadEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NovaDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun chatThreadDao(): ChatThreadDao

    companion object {
        @Volatile
        private var INSTANCE: NovaDatabase? = null

        fun getDatabase(context: Context): NovaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NovaDatabase::class.java,
                    "novachat_local_cache.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
