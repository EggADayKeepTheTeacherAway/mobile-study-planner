package org.classapp.studyplanner.data.local.database

import androidx.room.TypeConverter
import org.classapp.studyplanner.data.local.entity.Priority
import org.classapp.studyplanner.data.local.entity.Status
import java.time.LocalDateTime

class Converters {

    @TypeConverter
    fun fromTimestamp(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun fromPriority(value: Priority?): String? {
        return value?.name
    }

    @TypeConverter
    fun toPriority(value: String?): Priority? {
        return value?.let { enumValueOf<Priority>(it) }
    }

    @TypeConverter
    fun fromStatus(value: Status?): String? {
        return value?.name
    }

    @TypeConverter
    fun toStatus(value: String?): Status? {
        return value?.let { enumValueOf<Status>(it) }
    }
}
