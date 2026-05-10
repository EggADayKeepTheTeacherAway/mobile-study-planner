package org.classapp.studyplanner.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.classapp.studyplanner.R
import org.classapp.studyplanner.data.local.entity.AssignmentWithCourse
import org.classapp.studyplanner.databinding.ItemNotificationBinding
import java.time.format.DateTimeFormatter

class NotificationAdapter(private val onNotificationClick: (AssignmentWithCourse) -> Unit) : ListAdapter<AssignmentWithCourse, NotificationAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val assignment = item.assignment
        val course = item.course

        holder.binding.tvNotifyTitle.text = assignment.title + " due soon!"
        
        val deadline = assignment.deadline
        val subtitle = if (deadline != null) {
            val formatter = DateTimeFormatter.ofPattern("MMM dd, hh:mm a")
            "${course.courseName} · Due at ${deadline.format(formatter)}"
        } else {
            course.courseName
        }
        holder.binding.tvNotifySubtitle.text = subtitle

        holder.binding.tvNotifyTime.text = "Upcoming"

        // UI based on isRead status
        if (assignment.isRead) {
            holder.binding.viewUnread.visibility = View.GONE
            holder.binding.ivNotifyIcon.setImageResource(R.drawable.ic_check_circle_24)
            holder.binding.ivNotifyIcon.imageTintList = android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(holder.itemView.context, R.color.cgreen)
            )
            holder.binding.viewIconBg.backgroundTintList = android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(holder.itemView.context, R.color.low_priority_bg)
            )
        } else {
            holder.binding.viewUnread.visibility = View.VISIBLE
            holder.binding.ivNotifyIcon.setImageResource(R.drawable.ic_error_24)
            holder.binding.ivNotifyIcon.imageTintList = android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(holder.itemView.context, R.color.high_priority_text)
            )
            holder.binding.viewIconBg.backgroundTintList = android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(holder.itemView.context, R.color.high_priority_bg)
            )
        }

        holder.itemView.setOnClickListener {
            onNotificationClick(item)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<AssignmentWithCourse>() {
        override fun areItemsTheSame(oldItem: AssignmentWithCourse, newItem: AssignmentWithCourse) = 
            oldItem.assignment.id == newItem.assignment.id

        override fun areContentsTheSame(oldItem: AssignmentWithCourse, newItem: AssignmentWithCourse) = 
            oldItem.assignment.isRead == newItem.assignment.isRead && 
            oldItem.assignment.status == newItem.assignment.status
    }
}