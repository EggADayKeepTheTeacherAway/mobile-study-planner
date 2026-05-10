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

    enum class FilterType { THIS_WEEK, NEXT_WEEK, LATER }

    companion object {
        private const val ARG_SUBJECT_ID = "subject_id"

        fun newInstance(subjectId: Int = -1): TaskFragment {
            val fragment = TaskFragment()
            val args = Bundle()
            args.putInt(ARG_SUBJECT_ID, subjectId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subjectId = arguments?.getInt(ARG_SUBJECT_ID) ?: -1
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
            var tasks = when (currentFilter) {
                FilterType.THIS_WEEK -> assignmentRepository.getThisWeekAssignments(if (subjectId != -1) subjectId else null)
                FilterType.NEXT_WEEK -> assignmentRepository.getNextWeekAssignments(if (subjectId != -1) subjectId else null)
                FilterType.LATER -> assignmentRepository.getLaterAssignments(if (subjectId != -1) subjectId else null)
            }
            
            // Apply manual filters
            if (priorityFilter != null) {
                tasks = tasks.filter { it.assignment.priority == priorityFilter }
            }
            if (statusFilter != null) {
                if (statusFilter == Status.ASSIGNED) {
                    tasks = tasks.filter { it.assignment.status != Status.TURNED_IN }
                } else {
                    tasks = tasks.filter { it.assignment.status == statusFilter }
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
            fetchTasks() // Refresh list
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
