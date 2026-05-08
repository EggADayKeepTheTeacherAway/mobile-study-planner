package org.classapp.studyplanner.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import org.classapp.studyplanner.data.local.entity.Assignment
import org.classapp.studyplanner.data.local.entity.AssignmentWithCourse
import java.time.LocalDateTime

@Dao
interface AssignmentDao {

    @Insert
    suspend fun insert(assignment: Assignment)

    @Query("SELECT * FROM assignments")
    suspend fun getAll(): List<AssignmentWithCourse>

    @Query("SELECT * FROM assignments WHERE id = :id")
    suspend fun getById(id: Int): AssignmentWithCourse?

    @Query("SELECT * FROM assignments WHERE courseId = :courseId")
    suspend fun getByCourseId(courseId: Int): List<AssignmentWithCourse>

    // Get assignments that deadline before given datetime
    @Query("SELECT * FROM assignments WHERE deadline <= :deadline")
    suspend fun getByDeadline(deadline: LocalDateTime): List<AssignmentWithCourse>

    // Same as above but with course filter
    @Query("""
        SELECT * FROM assignments
        WHERE courseId = :courseId
        AND deadline <= :deadline
    """)
    suspend fun gwtByCourseIdAndDeadline(
        courseId: Int,
        deadline: LocalDateTime
    ): List<AssignmentWithCourse>

    // For today, this week ,and next week assignments query by giving time range
    @Query("""
        SELECT * FROM assignments
        WHERE deadline BETWEEN :start AND :end
        ORDER BY deadline ASC
    """)
    suspend fun getBetween(
        start: LocalDateTime,
        end: LocalDateTime
    ): List<AssignmentWithCourse>

    // Later than a given date
    @Query("""
        SELECT * FROM assignments
        WHERE deadline > :date
        ORDER BY deadline ASC
    """)
    suspend fun getAssignmentsAfter(
        date: LocalDateTime
    ): List<AssignmentWithCourse>

    @Update
    suspend fun update(assignment: Assignment)

    @Delete
    suspend fun delete(assignment: Assignment)
}