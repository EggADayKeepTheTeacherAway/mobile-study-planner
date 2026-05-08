package org.classapp.studyplanner.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.classapp.studyplanner.data.local.dao.AssignmentDao
import org.classapp.studyplanner.data.local.dao.CourseDao
import org.classapp.studyplanner.data.local.entity.Assignment
import org.classapp.studyplanner.data.local.entity.Course

@Database(entities = [Course::class, Assignment::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun courseDao(): CourseDao
    abstract fun assignmentDao(): AssignmentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}