package org.classapp.studyplanner.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import org.classapp.studyplanner.data.local.entity.Assignment
import org.classapp.studyplanner.data.local.entity.AssignmentWithCourse
import org.classapp.studyplanner.data.local.entity.Status
import java.time.LocalDateTime

@Dao
interface AssignmentDao {

    @Insert
    suspend fun insert(assignment: Assignment): Long

    @Query("SELECT * FROM assignments")
    suspend fun getAll(): List<AssignmentWithCourse>

    @Query("SELECT * FROM assignments WHERE id = :id")
    suspend fun getById(id: Int): AssignmentWithCourse?

    @Query("""
        SELECT * FROM assignments
        WHERE courseId = :courseId
        AND (:status IS NULL OR status = :status)
    """)
    suspend fun getByCourseId(courseId: Int, status: Status? = null): List<AssignmentWithCourse>

    // Get assignments that deadline before given datetime
    @Query("""
        SELECT * FROM assignments
        WHERE deadline <= :deadline
        AND (:courseId IS NULL OR courseId = :courseId)
        AND (:status IS NULL OR status = :status)
        ORDER BY deadline ASC
    """)
    suspend fun getByDeadline(
        deadline: LocalDateTime,
        courseId: Int? = null,
        status: Status? = null
    ): List<AssignmentWithCourse>

    // For today, this week ,and next week assignments query by giving time range
    @Query("""
        SELECT * FROM assignments
        WHERE deadline BETWEEN :start AND :end
        AND (:courseId IS NULL OR courseId = :courseId)
        AND (:status IS NULL OR status = :status)
        ORDER BY deadline ASC
    """)
    suspend fun getBetween(
        start: LocalDateTime,
        end: LocalDateTime,
        courseId: Int? = null,
        status: Status? = null
    ): List<AssignmentWithCourse>

    // Later than a given date
    @Query("""
        SELECT * FROM assignments
        WHERE deadline > :date
        AND (:courseId IS NULL OR courseId = :courseId)
        AND (:status IS NULL OR status = :status)
        ORDER BY deadline ASC
    """)
    suspend fun getAfter(
        date: LocalDateTime,
        courseId: Int? = null,
        status: Status? = null
    ): List<AssignmentWithCourse>

    @Update
    suspend fun update(assignment: Assignment)

    @Query("DELETE FROM assignments WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Delete
    suspend fun delete(assignment: Assignment)

    @Query("UPDATE assignments SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("UPDATE assignments SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)
}