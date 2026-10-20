package com.mpazpro3.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ContenidoEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase(){

    abstract fun contenidoDao(): ContenidoDao

    companion object{
        @Volatile
        private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase =
            instancia ?: synchronized(this){
                instancia?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mpazpro3.db"
                ).build().also { instancia = it }
            }
    }
}