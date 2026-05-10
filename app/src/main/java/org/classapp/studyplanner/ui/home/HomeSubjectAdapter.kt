package org.classapp.studyplanner.ui.home

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.classapp.studyplanner.data.local.entity.Course
import org.classapp.studyplanner.databinding.ItemHomeSubjectBinding
import org.classapp.studyplanner.ui.subject.SubjectWithStats

class HomeSubjectAdapter(private val onCourseClick: (Course) -> Unit) :
    ListAdapter<SubjectWithStats, HomeSubjectAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(val binding: ItemHomeSubjectBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHomeSubjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val course = item.course

        holder.binding.tvSubjectName.text = course.courseName
        holder.binding.tvSubjectStats.text = "${item.completedAssignments}/${item.totalAssignments} done"

        val progress = if (item.totalAssignments > 0) {
            (item.completedAssignments.toFloat() / item.totalAssignments * 100).toInt()
        } else {
            0
        }
        holder.binding.progressSubject.progress = progress

        // Use course color
        try {
            val color = (course.courseColor ?: "#1dafa1").toColorInt()
            holder.binding.viewIconBg.backgroundTintList = ColorStateList.valueOf(color)
            holder.binding.progressSubject.setIndicatorColor(color)
        } catch (e: Exception) {
            // Fallback
        }

        holder.itemView.setOnClickListener { onCourseClick(course) }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SubjectWithStats>() {
        override fun areItemsTheSame(oldItem: SubjectWithStats, newItem: SubjectWithStats) = 
            oldItem.course.id == newItem.course.id

        override fun areContentsTheSame(oldItem: SubjectWithStats, newItem: SubjectWithStats) = 
            oldItem == newItem
    }
}