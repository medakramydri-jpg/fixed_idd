package com.example.nougatbora

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlin.jvm.java

@Database(entities = [Model::class, Quantities::class], version = 5 )
abstract class AppDatabase: RoomDatabase() {
    abstract fun quantitiesDao(): QuantitiesDAO
    abstract fun deliveryDao(): OurDAO
    object DatabaseProvider {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDb(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "delivery_db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}