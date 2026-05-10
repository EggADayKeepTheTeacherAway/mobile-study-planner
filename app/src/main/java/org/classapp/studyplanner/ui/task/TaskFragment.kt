package org.classapp.studyplanner.ui.task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import org.classapp.studyplanner.R
import org.classapp.studyplanner.data.local.database.AppDatabase
import org.classapp.studyplanner.data.local.entity.AssignmentWithCourse
import org.classapp.studyplanner.data.local.entity.Status
import org.classapp.studyplanner.data.repository.AssignmentRepository
import org.classapp.studyplanner.databinding.FragmentTaskBinding

class TaskFragment : Fragment() {

    private var _binding: FragmentTaskBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var assignmentRepository: AssignmentRepository
    private lateinit var adapter: TaskAdapter
    
    private var currentFilter = FilterType.THIS_WEEK

    enum class FilterType { THIS_WEEK, NEXT_WEEK, LATER }

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

        setupRecyclerView()
        setupToggle()
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
            val tasks = when (currentFilter) {
                FilterType.THIS_WEEK -> assignmentRepository.getThisWeekAssignments()
                FilterType.NEXT_WEEK -> assignmentRepository.getNextWeekAssignments()
                FilterType.LATER -> assignmentRepository.getLaterAssignments()
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
