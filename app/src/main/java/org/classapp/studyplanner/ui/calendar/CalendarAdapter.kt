package org.classapp.studyplanner.ui.calendar

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.classapp.studyplanner.R
import org.classapp.studyplanner.data.local.entity.AssignmentWithCourse
import org.classapp.studyplanner.data.local.entity.Priority
import org.classapp.studyplanner.databinding.ItemCalendarDayBinding
import java.time.LocalDate

data class CalendarDay(
    val date: LocalDate?,
    val isCurrentMonth: Boolean,
    var isSelected: Boolean = false,
    val tasks: List<AssignmentWithCourse> = emptyList()
)

class CalendarAdapter(private val onDayClick: (LocalDate) -> Unit) :
    RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {

    private var days = listOf<CalendarDay>()

    class ViewHolder(val binding: ItemCalendarDayBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCalendarDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val day = days[position]
        
        if (day.date == null) {
            holder.binding.tvDay.text = ""
            holder.binding.viewSelected.visibility = View.INVISIBLE
            holder.binding.layoutDots.visibility = View.INVISIBLE
            holder.itemView.setOnClickListener(null)
            return
        }

        holder.binding.tvDay.text = day.date.dayOfMonth.toString()
        holder.binding.tvDay.setTextColor(
            if (day.isSelected) Color.WHITE 
            else if (day.isCurrentMonth) Color.BLACK 
            else ContextCompat.getColor(holder.itemView.context, R.color.inactive_grey)
        )

        holder.binding.viewSelected.visibility = if (day.isSelected) View.VISIBLE else View.INVISIBLE

        // Dots
        val priorities = day.tasks.map { it.assignment.priority }.distinct()
        holder.binding.dot1.visibility = View.GONE
        holder.binding.dot2.visibility = View.GONE
        holder.binding.dot3.visibility = View.GONE
        
        priorities.take(3).forEachIndexed { index, priority ->
            val dot = when (index) {
                0 -> holder.binding.dot1
                1 -> holder.binding.dot2
                else -> holder.binding.dot3
            }
            dot.visibility = View.VISIBLE
            val color = when (priority) {
                Priority.HIGH -> ContextCompat.getColor(holder.itemView.context, R.color.high_priority_text)
                Priority.MEDIUM -> ContextCompat.getColor(holder.itemView.context, R.color.medium_priority_text)
                else -> ContextCompat.getColor(holder.itemView.context, R.color.low_priority_text)
            }
            dot.backgroundTintList = ColorStateList.valueOf(color)
        }
        
        holder.binding.layoutDots.visibility = if (priorities.isNotEmpty()) View.VISIBLE else View.INVISIBLE

        holder.itemView.setOnClickListener {
            onDayClick(day.date)
        }
    }

    override fun getItemCount() = days.size

    fun submitList(newDays: List<CalendarDay>) {
        days = newDays
        notifyDataSetChanged()
    }
}