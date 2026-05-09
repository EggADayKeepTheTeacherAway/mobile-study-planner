package org.classapp.studyplanner.data.repository

import org.classapp.studyplanner.data.local.dao.CourseDao
import org.classapp.studyplanner.data.local.entity.Course

class CourseRepository(private val courseDao: CourseDao) {

    suspend fun createCourse(name: String, code: String, color: String? = null) {
        courseDao.insert(Course(courseName = name, courseCode = code, courseColor = color ?: "#1dafa1"))
    }

    suspend fun getCourses(): List<Course> {
        return courseDao.getAll()
    }

    suspend fun getCourseById(id: Int): Course? {
        return courseDao.getById(id)
    }

    suspend fun updateCourse(course: Course) {
        courseDao.update(course)
    }

    suspend fun removeCourse(course: Course) {
        courseDao.delete(course)
    }

    suspend fun removeCourse(id: Int) {
        courseDao.deleteById(id)
    }
}