package org.classapp.studyplanner.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import org.classapp.studyplanner.data.local.entity.Course

@Dao
interface CourseDao {

    @Insert
    suspend fun insert(course: Course)

    @Query("SELECT * FROM courses")
    suspend fun getAll(): List<Course>

    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getById(id: Int): Course?

    @Query("SELECT * FROM courses WHERE courseName = :name")
    suspend fun getByName(name: String): List<Course>

    @Query("SELECT * FROM courses WHERE courseCode = :code")
    suspend fun getByCode(code: String): Course?

    @Update
    suspend fun update(course: Course)

    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Delete
    suspend fun delete(course: Course)
}