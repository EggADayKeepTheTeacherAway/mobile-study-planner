package org.classapp.studyplanner.data.repository

import org.classapp.studyplanner.data.local.dao.AssignmentDao
import org.classapp.studyplanner.data.local.entity.Assignment
import org.classapp.studyplanner.data.local.entity.AssignmentWithCourse
import org.classapp.studyplanner.data.local.entity.Priority
import org.classapp.studyplanner.data.local.entity.Status
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

class AssignmentRepository(private val assignmentDao: AssignmentDao) {

    suspend fun createAssignment(
        courseId: Int,
        title: String,
        description: String?,
        deadline: LocalDateTime?,
        notification: Int?,
        priority: Priority?
    ): Long {
        if (deadline != null) {
            if (deadline <= LocalDateTime.now()) {
                throw IllegalArgumentException("Deadline must be after now")
            }
        }
        if (notification != null) {
            if (notification < 0) {
                throw IllegalArgumentException("Notification must be positive integer")
            }
        }
        return assignmentDao.insert(Assignment(
            courseId = courseId,
            title = title,
            description = description,
            deadline = deadline,
            notification = notification,
            priority = priority
        ))
    }

    suspend fun getAllAssignments(): List<AssignmentWithCourse> {
        return assignmentDao.getAll()
    }

    suspend fun getAssignmentsByCourse(
        courseId: Int,
        status: Status? = null
    ): List<AssignmentWithCourse> {
        return assignmentDao.getByCourseId(courseId, status)
    }

    suspend fun getThisWeekAssignments(
        courseId: Int? = null,
        status: Status? = null
    ): List<AssignmentWithCourse> {
        val today = LocalDate.now()
        val start = today.with(DayOfWeek.MONDAY).atStartOfDay()
        val end = today.with(DayOfWeek.SUNDAY).atTime(23, 59, 59)
        return assignmentDao.getBetween(start, end, courseId, status)
    }

    suspend fun getNextWeekAssignments(
        courseId: Int? = null,
        status: Status? = null
    ): List<AssignmentWithCourse> {
        val today = LocalDate.now()
        val start = today
            .with(DayOfWeek.MONDAY)
            .plusWeeks(1)
            .atStartOfDay()
        val end = today
            .with(DayOfWeek.SUNDAY)
            .plusWeeks(1)
            .atTime(23, 59, 59)
        return assignmentDao.getBetween(start, end, courseId, status)
    }

    suspend fun getLaterAssignments(
        courseId: Int? = null,
        status: Status? = null
    ): List<AssignmentWithCourse> {
        val today = LocalDate.now()
        val later = today
            .with(DayOfWeek.SUNDAY)
            .plusWeeks(1)
            .atTime(23, 59, 59)
        return assignmentDao.getAfter(later, courseId, status)
    }

    suspend fun getAssignmentsDeadlineBefore(
        date: LocalDateTime = LocalDate.now().atTime(23, 59, 59),
        courseId: Int? = null,
        status: Status? = null
    ): List<AssignmentWithCourse> {
        return assignmentDao.getByDeadline(date, courseId, status)
    }

    suspend fun updateAssignment(assignment: Assignment) {
        assignmentDao.update(assignment)
    }

    suspend fun markAllAsRead() {
        assignmentDao.markAllAsRead()
    }

    suspend fun markAsRead(id: Int) {
        assignmentDao.markAsRead(id)
    }
}