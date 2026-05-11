package org.classapp.studyplanner.ui.subject

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.classapp.studyplanner.data.local.entity.Course
import org.classapp.studyplanner.databinding.ItemSubjectBinding
import androidx.core.graphics.toColorInt

data class SubjectWithStats(
    val course: Course,
    val totalAssignments: Int,
    val completedAssignments: Int
)

class SubjectAdapter(private val onCourseClick: (Course) -> Unit) :
    ListAdapter<SubjectWithStats, SubjectAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(val binding: ItemSubjectBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSubjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val course = item.course

        holder.binding.tvSubjectName.text = course.courseName
        holder.binding.tvCompletedStatus.text = "${item.completedAssignments}/${item.totalAssignments} assignments completed"

        val remaining = item.totalAssignments - item.completedAssignments
        holder.binding.tvRemaining.text = "$remaining remaining"

        val progress = if (item.totalAssignments > 0) {
            (item.completedAssignments.toFloat() / item.totalAssignments * 100).toInt()
        } else {
            0
        }
        holder.binding.progressIndicator.progress = progress

        // Use course color
        try {
            val color = (course.courseColor ?: "#1dafa1").toColorInt()
            holder.binding.ivSubjectIcon.imageTintList = ColorStateList.valueOf(color)
            holder.binding.progressIndicator.setIndicatorColor(color)
            holder.binding.tvViewAssignments.setTextColor(color)

            // Light background for icon
            holder.binding.viewIconBg.backgroundTintList = ColorStateList.valueOf(color).withAlpha(30)
        } catch (e: Exception) {
            // Fallback
            holder.binding.ivSubjectIcon.imageTintList = ColorStateList.valueOf("#1dafa1".toColorInt())
            holder.binding.progressIndicator.setIndicatorColor("#1dafa1".toColorInt())
            holder.binding.tvViewAssignments.setTextColor("#1dafa1".toColorInt())
            holder.binding.viewIconBg.backgroundTintList = ColorStateList.valueOf("#1dafa1".toColorInt()).withAlpha(30)
        }

        holder.rootView().setOnClickListener { onCourseClick(course) }
    }

    private fun ViewHolder.rootView() = binding.root

    companion object DiffCallback : DiffUtil.ItemCallback<SubjectWithStats>() {
        override fun areItemsTheSame(oldItem: SubjectWithStats, newItem: SubjectWithStats): Boolean {
            return oldItem.course.id == newItem.course.id
        }

        override fun areContentsTheSame(oldItem: SubjectWithStats, newItem: SubjectWithStats): Boolean {
            return oldItem == newItem
        }
    }
}
