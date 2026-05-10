package org.classapp.studyplanner.ui.home

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
import org.classapp.studyplanner.data.local.entity.Status
import org.classapp.studyplanner.data.repository.AssignmentRepository
import org.classapp.studyplanner.data.repository.CourseRepository
import org.classapp.studyplanner.databinding.FragmentHomeBinding
import org.classapp.studyplanner.ui.subject.SubjectWithStats
import org.classapp.studyplanner.ui.task.TaskFragment
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var assignmentRepository: AssignmentRepository
    private lateinit var courseRepository: CourseRepository

    private lateinit var deadlineAdapter: HomeTaskAdapter
    private lateinit var subjectAdapter: HomeSubjectAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())
        assignmentRepository = AssignmentRepository(db.assignmentDao())
        courseRepository = CourseRepository(db.courseDao())

        setupRecyclerViews()
        setupListeners()
        loadData()
    }

    private fun setupRecyclerViews() {
        // Upcoming Deadlines (Compact Home Version)
        deadlineAdapter = HomeTaskAdapter()
        binding.rvUpcomingDeadlines.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUpcomingDeadlines.adapter = deadlineAdapter

        // Subjects (Compact Grid Version)
        subjectAdapter = HomeSubjectAdapter { course ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_container, TaskFragment.newInstance(course.id))
                .addToBackStack(null)
                .commit()
        }
        binding.rvHomeSubjects.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvHomeSubjects.adapter = subjectAdapter
    }

    private fun setupListeners() {
        binding.btnNotifications.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_container, NotificationsFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.btnSeeAllDeadlines.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_container, TaskFragment.newInstance(filterUpcoming = true))
                .addToBackStack(null)
                .commit()
        }

        binding.btnManageSubjects.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.frame_container,
                    org.classapp.studyplanner.ui.subject.SubjectFragment()
                )
                .addToBackStack(null)
                .commit()
        }
    }

    private fun loadData() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Load stats
            val allTasks = assignmentRepository.getAllAssignments()
            val total = allTasks.size
            val completed = allTasks.count { it.assignment.status == Status.TURNED_IN }

            val progress = if (total > 0) (completed * 100 / total) else 0
            binding.progressWeekly.progress = progress
            binding.tvProgressPercent.text = String.format(Locale.getDefault(), "%d%%", progress)
            binding.tvProgressSummary.text = String.format(
                Locale.getDefault(),
                "%d of %d assignments completed",
                completed,
                total
            )

            // Upcoming Deadlines (Show next 3 incomplete tasks regardless of read status)
            val allIncomplete = allTasks
                .filter { it.assignment.status != Status.TURNED_IN }
                .sortedBy { it.assignment.deadline }
            
            val upcomingForDisplay = allIncomplete.take(3)
            deadlineAdapter.submitList(upcomingForDisplay)

            // Subjects with Stats
            val courses = courseRepository.getCourses()
            val subjectWithStats = courses.map { course ->
                val assignments = assignmentRepository.getAssignmentsByCourse(course.id)
                val compCount = assignments.count { it.assignment.status == Status.TURNED_IN }
                SubjectWithStats(course, assignments.size, compCount)
            }
            subjectAdapter.submitList(subjectWithStats)

            // Notifications Badge (Only count unread assignments that aren't completed)
            val unreadCount = allTasks.count { 
                it.assignment.status != Status.TURNED_IN && !it.assignment.isRead 
            }
            
            if (unreadCount > 0) {
                binding.tvNotificationBadge.visibility = View.VISIBLE
                binding.tvNotificationBadge.text = if (unreadCount > 9) "9+" else unreadCount.toString()
            } else {
                binding.tvNotificationBadge.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
