package org.classapp.studyplanner.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "assignments",
    foreignKeys = [
        ForeignKey(
            entity = Course::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("courseId")]
)
data class Assignment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Foreign key
    val courseId: Int,

    val title: String,
    val description: String? = "",
    val deadline: LocalDateTime? = null,

    // E.g. notification = 1 mean one day before deadline will trigger notification
    val notification: Int? = null,

    val status: Status? = Status.ASSIGNED,
    val priority: Priority? = Priority.LOW,
    val isRead: Boolean = false
)