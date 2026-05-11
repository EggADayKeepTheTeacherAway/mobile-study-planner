package org.classapp.studyplanner.ui.subject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import org.classapp.studyplanner.R
import org.classapp.studyplanner.data.local.database.AppDatabase
import org.classapp.studyplanner.data.local.entity.Status
import org.classapp.studyplanner.data.repository.AssignmentRepository
import org.classapp.studyplanner.data.repository.CourseRepository
import org.classapp.studyplanner.databinding.FragmentSubjectBinding
import org.classapp.studyplanner.ui.task.TaskFragment

class SubjectFragment : Fragment() {

    private var _binding: FragmentSubjectBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var courseRepository: CourseRepository
    private lateinit var assignmentRepository: AssignmentRepository
    private lateinit var adapter: SubjectAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubjectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())
        courseRepository = CourseRepository(db.courseDao())
        assignmentRepository = AssignmentRepository(db.assignmentDao())

        setupRecyclerView()
        setupListeners()
        fetchData()
    }

    private fun setupRecyclerView() {
        adapter = SubjectAdapter { course ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_container, TaskFragment.newInstance(course.id))
                .addToBackStack(null)
                .commit()
        }
        binding.rvSubjects.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSubjects.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnAddSubject.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_container, AddSubjectFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun fetchData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val courses = courseRepository.getCourses()
            val subjectWithStats = courses.map { course ->
                val assignments = assignmentRepository.getAssignmentsByCourse(course.id)
                val completed = assignments.count { it.assignment.status == Status.TURNED_IN }
                SubjectWithStats(
                    course = course,
                    totalAssignments = assignments.size,
                    completedAssignments = completed
                )
            }
            
            adapter.submitList(subjectWithStats)

            val totalSubjects = subjectWithStats.size
            val totalAssignments = subjectWithStats.sumOf { it.totalAssignments }
            binding.tvSubjectSummary.text = getString(
                R.string.subject_summary_format,
                totalSubjects,
                if (totalSubjects == 1) "subject" else "subjects",
                totalAssignments,
                if (totalAssignments == 1) "assignment" else "assignments"
            )
            
            if (subjectWithStats.isEmpty()) {
                binding.rvSubjects.visibility = View.GONE
            } else {
                binding.rvSubjects.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
