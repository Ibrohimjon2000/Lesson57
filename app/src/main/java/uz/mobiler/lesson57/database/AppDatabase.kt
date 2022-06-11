package uz.mobiler.lesson57.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import uz.mobiler.lesson57.database.dao.MusicModelDao
import uz.mobiler.lesson57.database.entity.MusicModel

@Database(entities = [MusicModel::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun musicDao(): MusicModelDao

    companion object {
        private var appDatabase: AppDatabase? = null

        @Synchronized
        fun getInstance(context: Context): AppDatabase {
            if (appDatabase == null) {
                appDatabase = Room.databaseBuilder(context, AppDatabase::class.java, "my_db")
                    .allowMainThreadQueries()
                    .build()
            }
            return appDatabase!!
        }
    }
}