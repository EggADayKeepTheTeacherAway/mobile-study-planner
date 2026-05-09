package org.classapp.studyplanner.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class AssignmentWithCourse(

    @Embedded
    val assignment: Assignment,

    @Relation(
        parentColumn = "courseId",
        entityColumn = "id"
    )
    val course: Course
)
