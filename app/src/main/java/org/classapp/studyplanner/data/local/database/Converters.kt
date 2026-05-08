package org.classapp.studyplanner.data.local.database

import androidx.room.TypeConverter
import org.classapp.studyplanner.data.local.entity.Priority
import org.classapp.studyplanner.data.local.entity.Status

class Converters {

    @TypeConverter
    fun fromPriority(value: Priority): String {
        return value.name
    }

    @TypeConverter
    fun toPriority(value: String): Priority {
        return enumValueOf(value)
    }

    @TypeConverter
    fun fromStatus(value: Status): String {
        return value.name
    }

    @TypeConverter
    fun toStatus(value: String): Status {
        return enumValueOf(value)
    }
}