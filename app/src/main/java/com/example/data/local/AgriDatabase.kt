package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        NotificationEntity::class,
        EquipmentEntity::class,
        ProduceEntity::class,
        CompostEntity::class,
        WasteRequestEntity::class,
        RentalContractEntity::class,
        PaymentTransactionEntity::class,
        ForumPostEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AgriDatabase : RoomDatabase() {
    abstract fun agriDao(): AgriDao

    companion object {
        @Volatile
        private var INSTANCE: AgriDatabase? = null

        fun getDatabase(context: Context): AgriDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AgriDatabase::class.java,
                    "agri_shop_database.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
