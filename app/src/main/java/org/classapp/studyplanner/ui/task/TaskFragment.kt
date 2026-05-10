package org.classapp.studyplanner.ui.task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import org.classapp.studyplanner.R
import org.classapp.studyplanner.data.local.database.AppDatabase
import org.classapp.studyplanner.data.local.entity.AssignmentWithCourse
import org.classapp.studyplanner.data.local.entity.Priority
import org.classapp.studyplanner.data.local.entity.Status
import org.classapp.studyplanner.data.repository.AssignmentRepository
import org.classapp.studyplanner.data.repository.CourseRepository
import org.classapp.studyplanner.databinding.FragmentTaskBinding
import org.classapp.studyplanner.receiver.NotificationScheduler

class TaskFragment : Fragment() {

    private var _binding: FragmentTaskBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var assignmentRepository: AssignmentRepository
    private lateinit var courseRepository: CourseRepository
    private lateinit var adapter: TaskAdapter
    
    private var currentFilter = FilterType.THIS_WEEK
    private var subjectId: Int = -1
    private var priorityFilter: Priority? = null
    private var statusFilter: Status? = null
    private var filterUpcoming: Boolean = false

    enum class FilterType { THIS_WEEK, NEXT_WEEK, LATER }

    companion object {
        private const val ARG_SUBJECT_ID = "subject_id"
        private const val ARG_FILTER_UPCOMING = "filter_upcoming"

        fun newInstance(subjectId: Int = -1, filterUpcoming: Boolean = false): TaskFragment {
            val fragment = TaskFragment()
            val args = Bundle()
            args.putInt(ARG_SUBJECT_ID, subjectId)
            args.putBoolean(ARG_FILTER_UPCOMING, filterUpcoming)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subjectId = arguments?.getInt(ARG_SUBJECT_ID) ?: -1
        filterUpcoming = arguments?.getBoolean(ARG_FILTER_UPCOMING) ?: false
        
        if (filterUpcoming) {
            currentFilter = FilterType.THIS_WEEK // Default starting point, but logic will override
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())
        assignmentRepository = AssignmentRepository(db.assignmentDao())
        courseRepository = CourseRepository(db.courseDao())

        setupRecyclerView()
        setupToggle()
        setupFilter()
        
        if (subjectId != -1) {
            binding.btnBack.visibility = View.VISIBLE
            binding.btnBack.setOnClickListener {
                parentFragmentManager.popBackStack()
            }
            viewLifecycleOwner.lifecycleScope.launch {
                val course = courseRepository.getCourseById(subjectId)
                binding.tvTitle.text = course?.courseName ?: getString(R.string.tasks)
                binding.tvSubtitle.text = getString(R.string.track_deadlines)
            }
        }
        
        fetchTasks()
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter { item, isChecked ->
            updateTaskStatus(item, isChecked)
        }
        binding.rvTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTasks.adapter = adapter
    }

    private fun setupToggle() {
        binding.tvToggleThisWeek.setOnClickListener {
            updateFilter(FilterType.THIS_WEEK)
        }
        binding.tvToggleNextWeek.setOnClickListener {
            updateFilter(FilterType.NEXT_WEEK)
        }
        binding.tvToggleLater.setOnClickListener {
            updateFilter(FilterType.LATER)
        }
    }

    private fun setupFilter() {
        binding.tvFilter.setOnClickListener {
            showFilterDialog()
        }
    }

    private fun showFilterDialog() {
        val options = arrayOf("All", "High Priority", "Medium Priority", "Low Priority", "Completed", "Incomplete")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Filter Tasks")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> { // All
                    priorityFilter = null
                    statusFilter = null
                }
                1 -> { // High
                    priorityFilter = Priority.HIGH
                    statusFilter = null
                }
                2 -> { // Medium
                    priorityFilter = Priority.MEDIUM
                    statusFilter = null
                }
                3 -> { // Low
                    priorityFilter = Priority.LOW
                    statusFilter = null
                }
                4 -> { // Completed
                    priorityFilter = null
                    statusFilter = Status.TURNED_IN
                }
                5 -> { // Incomplete
                    priorityFilter = null
                    statusFilter = Status.ASSIGNED
                }
            }
            fetchTasks()
        }
        builder.show()
    }

    private fun updateFilter(type: FilterType) {
        currentFilter = type
        
        // Reset styles
        listOf(binding.tvToggleThisWeek, binding.tvToggleNextWeek, binding.tvToggleLater).forEach {
            it.background = null
            it.setTextColor(ContextCompat.getColor(requireContext(), R.color.inactive_grey))
            it.setTypeface(null, android.graphics.Typeface.NORMAL)
        }

        // Apply selected style
        val selectedView = when (type) {
            FilterType.THIS_WEEK -> binding.tvToggleThisWeek
            FilterType.NEXT_WEEK -> binding.tvToggleNextWeek
            FilterType.LATER -> binding.tvToggleLater
        }
        
        selectedView.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_toggle_selected)
        selectedView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        selectedView.setTypeface(null, android.graphics.Typeface.BOLD)

        fetchTasks()
    }

    private fun fetchTasks() {
        viewLifecycleOwner.lifecycleScope.launch {
            var tasks = if (filterUpcoming) {
                // Fetch assignments due in the next 7 days
                val next7Days = java.time.LocalDateTime.now().plusDays(7)
                assignmentRepository.getAssignmentsDeadlineBefore(next7Days)
                    .filter { it.assignment.deadline?.isAfter(java.time.LocalDateTime.now().minusMinutes(1)) ?: true }
            } else {
                when (currentFilter) {
                    FilterType.THIS_WEEK -> assignmentRepository.getThisWeekAssignments(if (subjectId != -1) subjectId else null)
                    FilterType.NEXT_WEEK -> assignmentRepository.getNextWeekAssignments(if (subjectId != -1) subjectId else null)
                    FilterType.LATER -> assignmentRepository.getLaterAssignments(if (subjectId != -1) subjectId else null)
                }
            }
            
            // If subjectId is set (detail page), further filter by subject
            if (subjectId != -1) {
                tasks = tasks.filter { it.course.id == subjectId }
            }
            if (priorityFilter != null) {
                tasks = tasks.filter { it.assignment.priority == priorityFilter }
            }
            if (statusFilter != null) {
                tasks = if (statusFilter == Status.ASSIGNED) {
                    tasks.filter { it.assignment.status != Status.TURNED_IN }
                } else {
                    tasks.filter { it.assignment.status == statusFilter }
                }
            }
            
            adapter.submitList(tasks)
            updateSummary(tasks)
        }
    }

    private fun updateSummary(tasks: List<AssignmentWithCourse>) {
        val total = tasks.size
        val completed = tasks.count { it.assignment.status == Status.TURNED_IN }
        binding.tvSummary.text = getString(R.string.completed_format, completed, total)
    }

    private fun updateTaskStatus(item: AssignmentWithCourse, isChecked: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            val updatedAssignment = item.assignment.copy(
                status = if (isChecked) Status.TURNED_IN else Status.WORKING
            )
            assignmentRepository.updateAssignment(updatedAssignment)
            
            if (isChecked) {
                NotificationScheduler.cancelNotification(requireContext(), item.assignment.id)
            } else if (item.assignment.notification != null) {
                NotificationScheduler.scheduleNotification(requireContext(), updatedAssignment)
            }

            fetchTasks() // Refresh list
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
