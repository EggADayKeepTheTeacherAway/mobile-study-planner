package org.classapp.studyplanner.ui.task

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.classapp.studyplanner.R
import org.classapp.studyplanner.data.local.entity.AssignmentWithCourse
import org.classapp.studyplanner.data.local.entity.Priority
import org.classapp.studyplanner.data.local.entity.Status
import org.classapp.studyplanner.databinding.ItemTaskBinding
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class TaskAdapter(private val onTaskStatusChanged: (AssignmentWithCourse, Boolean) -> Unit) :
    ListAdapter<AssignmentWithCourse, TaskAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val assignment = item.assignment
        val course = item.course

        holder.binding.tvTaskTitle.text = assignment.title
        holder.binding.tvSubjectName.text = course.courseName
        
        // Handle completion state
        val isCompleted = assignment.status == Status.TURNED_IN
        holder.binding.cbComplete.isChecked = isCompleted
        
        if (isCompleted) {
            holder.binding.tvTaskTitle.paintFlags = holder.binding.tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.binding.tvTaskTitle.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.inactive_grey))
        } else {
            holder.binding.tvTaskTitle.paintFlags = holder.binding.tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.binding.tvTaskTitle.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
        }

        // Priority dot
        val priorityColor = when (assignment.priority) {
            Priority.HIGH -> ContextCompat.getColor(holder.itemView.context, R.color.high_priority_text)
            Priority.MEDIUM -> ContextCompat.getColor(holder.itemView.context, R.color.medium_priority_text)
            else -> ContextCompat.getColor(holder.itemView.context, R.color.low_priority_text)
        }
        holder.binding.viewPriority.backgroundTintList = android.content.res.ColorStateList.valueOf(priorityColor)

        // Deadline text
        assignment.deadline?.let { deadline ->
            val date = deadline.toLocalDate()
            val today = LocalDate.now()
            
            val deadlineText = when (date) {
                today -> holder.itemView.context.getString(R.string.today)
                today.plusDays(1) -> holder.itemView.context.getString(R.string.tomorrow)
                else -> date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            }
            holder.binding.tvDeadline.text = deadlineText
        }

        // Listener for checkbox
        holder.binding.cbComplete.setOnClickListener {
            onTaskStatusChanged(item, holder.binding.cbComplete.isChecked)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<AssignmentWithCourse>() {
        override fun areItemsTheSame(oldItem: AssignmentWithCourse, newItem: AssignmentWithCourse): Boolean {
            return oldItem.assignment.id == newItem.assignment.id
        }

        override fun areContentsTheSame(oldItem: AssignmentWithCourse, newItem: AssignmentWithCourse): Boolean {
            return oldItem == newItem
        }
    }
}
