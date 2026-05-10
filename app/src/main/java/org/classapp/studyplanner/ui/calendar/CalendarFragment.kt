package org.classapp.studyplanner.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import org.classapp.studyplanner.R
import org.classapp.studyplanner.data.local.database.AppDatabase
import org.classapp.studyplanner.data.local.entity.AssignmentWithCourse
import org.classapp.studyplanner.data.repository.AssignmentRepository
import org.classapp.studyplanner.databinding.FragmentCalendarBinding
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private lateinit var assignmentRepository: AssignmentRepository
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var taskAdapter: CalendarTaskAdapter

    private var selectedDate: LocalDate = LocalDate.now()
    private var currentMonth: YearMonth = YearMonth.now()
    
    private var allAssignments: List<AssignmentWithCourse> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())
        assignmentRepository = AssignmentRepository(db.assignmentDao())

        setupRecyclerViews()
        setupListeners()
        
        loadData()
    }

    private fun setupRecyclerViews() {
        calendarAdapter = CalendarAdapter { date ->
            selectedDate = date
            updateCalendarGrid()
            updateTasksForSelectedDate()
        }
        binding.rvCalendar.layoutManager = GridLayoutManager(requireContext(), 7)
        binding.rvCalendar.adapter = calendarAdapter

        taskAdapter = CalendarTaskAdapter()
        binding.rvTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTasks.adapter = taskAdapter
    }

    private fun setupListeners() {
        binding.btnPrevMonth.setOnClickListener {
            currentMonth = currentMonth.minusMonths(1)
            updateCalendarGrid()
        }
        binding.btnNextMonth.setOnClickListener {
            currentMonth = currentMonth.plusMonths(1)
            updateCalendarGrid()
        }
    }

    private fun loadData() {
        viewLifecycleOwner.lifecycleScope.launch {
            allAssignments = assignmentRepository.getAllAssignments()
            updateCalendarGrid()
            updateTasksForSelectedDate()
        }
    }

    private fun updateCalendarGrid() {
        val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
        binding.tvCurrentMonth.text = currentMonth.format(monthTitleFormatter)

        val daysInMonth = currentMonth.lengthOfMonth()
        val firstOfMonth = currentMonth.atDay(1)
        val dayOfWeekOfFirst = firstOfMonth.dayOfWeek.value % 7 // 0=Sunday, 1=Monday...

        val calendarDays = mutableListOf<CalendarDay>()
        
        // Previous month days (placeholders)
        for (i in 0 until dayOfWeekOfFirst) {
            calendarDays.add(CalendarDay(null, false))
        }

        // Current month days
        for (i in 1..daysInMonth) {
            val date = currentMonth.atDay(i)
            val tasksForDay = allAssignments.filter { 
                it.assignment.deadline?.toLocalDate() == date 
            }
            calendarDays.add(CalendarDay(
                date = date,
                isCurrentMonth = true,
                isSelected = date == selectedDate,
                tasks = tasksForDay
            ))
        }

        calendarAdapter.submitList(calendarDays)
    }

    private fun updateTasksForSelectedDate() {
        val tasksForDay = allAssignments.filter { 
            it.assignment.deadline?.toLocalDate() == selectedDate 
        }
        taskAdapter.submitList(tasksForDay)

        val dateLabel = if (selectedDate == LocalDate.now()) getString(R.string.today) 
                        else selectedDate.format(DateTimeFormatter.ofPattern("MMM dd"))
        val assignmentCount = tasksForDay.size
        
        // Using a template for summary: "Date · X assignments"
        binding.tvSelectedDaySummary.text = String.format(Locale.getDefault(), "%s · %d %s", 
            dateLabel, 
            assignmentCount, 
            if (assignmentCount == 1) "assignment" else "assignments"
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}