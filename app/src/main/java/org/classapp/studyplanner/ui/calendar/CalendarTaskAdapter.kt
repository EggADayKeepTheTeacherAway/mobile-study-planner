package org.classapp.studyplanner.ui.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.classapp.studyplanner.data.local.entity.AssignmentWithCourse
import org.classapp.studyplanner.databinding.ItemCalendarTaskBinding
import java.time.format.DateTimeFormatter
import androidx.core.graphics.toColorInt

class CalendarTaskAdapter : ListAdapter<AssignmentWithCourse, CalendarTaskAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(val binding: ItemCalendarTaskBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCalendarTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val assignment = item.assignment
        val course = item.course

        holder.binding.tvTaskTitle.text = assignment.title
        holder.binding.tvSubjectName.text = course.courseName
        
        val color = try {
            (course.courseColor ?: "#1dafa1").toColorInt()
        } catch (_: Exception) {
            "#1dafa1".toColorInt()
        }
        holder.binding.viewSubjectColor.setBackgroundColor(color)

        assignment.deadline?.let {
            val formatter = DateTimeFormatter.ofPattern("hh:mm a")
            holder.binding.tvTime.text = it.format(formatter)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<AssignmentWithCourse>() {
        override fun areItemsTheSame(oldItem: AssignmentWithCourse, newItem: AssignmentWithCourse) = 
            oldItem.assignment.id == newItem.assignment.id

        override fun areContentsTheSame(oldItem: AssignmentWithCourse, newItem: AssignmentWithCourse) = 
            oldItem == newItem
    }
}