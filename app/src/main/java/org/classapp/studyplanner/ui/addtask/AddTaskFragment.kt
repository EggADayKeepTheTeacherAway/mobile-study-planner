package org.classapp.studyplanner.ui.addtask

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.classapp.studyplanner.R
import org.classapp.studyplanner.data.local.database.AppDatabase
import org.classapp.studyplanner.data.local.entity.Course
import org.classapp.studyplanner.data.local.entity.Priority
import org.classapp.studyplanner.data.repository.AssignmentRepository
import org.classapp.studyplanner.data.repository.CourseRepository
import org.classapp.studyplanner.databinding.FragmentAddTaskBinding
import org.classapp.studyplanner.receiver.NotificationScheduler
import java.time.LocalDateTime
import java.util.Calendar
import java.util.Locale

class AddTaskFragment : Fragment() {

    private var _binding: FragmentAddTaskBinding? = null
    private val binding get() = _binding!!

    private var selectedPriority: Priority = Priority.LOW
    private val calendar = Calendar.getInstance()
    
    private lateinit var courseRepository: CourseRepository
    private lateinit var assignmentRepository: AssignmentRepository
    private var coursesList: List<Course> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val db = AppDatabase.getDatabase(requireContext())
        courseRepository = CourseRepository(db.courseDao())
        assignmentRepository = AssignmentRepository(db.assignmentDao())

        setupListeners()
        updatePriorityUI()
        loadCourses()
    }

    private fun loadCourses() {
        viewLifecycleOwner.lifecycleScope.launch {
            coursesList = courseRepository.getCourses()
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                coursesList.map { it.courseName }
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerSubject.adapter = adapter
            
            if (coursesList.isEmpty()) {
                Toast.makeText(requireContext(), "Please create a subject first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        binding.etDeadline.setOnClickListener {
            showDatePicker()
        }

        binding.etTime.setOnClickListener {
            showTimePicker()
        }

        binding.btnPriorityLow.setOnClickListener {
            selectedPriority = Priority.LOW
            updatePriorityUI()
        }

        binding.btnPriorityMedium.setOnClickListener {
            selectedPriority = Priority.MEDIUM
            updatePriorityUI()
        }

        binding.btnPriorityHigh.setOnClickListener {
            selectedPriority = Priority.HIGH
            updatePriorityUI()
        }

        binding.btnCreateAssignment.setOnClickListener {
            saveAssignment()
        }
    }

    private fun saveAssignment() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val selectedCourseIndex = binding.spinnerSubject.selectedItemPosition
        
        if (title.isEmpty()) {
            binding.etTitle.error = "Title is required"
            return
        }
        
        if (selectedCourseIndex < 0 || coursesList.isEmpty()) {
            Toast.makeText(requireContext(), "Please select a subject", Toast.LENGTH_SHORT).show()
            return
        }
        
        val courseId = coursesList[selectedCourseIndex].id
        
        // Convert calendar to LocalDateTime
        val deadline = LocalDateTime.of(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE)
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val notificationDays = if (binding.switchReminder.isChecked) 1 else null
                val newId = assignmentRepository.createAssignment(
                    courseId = courseId,
                    title = title,
                    description = description,
                    deadline = deadline,
                    notification = notificationDays,
                    priority = selectedPriority
                )
                
                if (notificationDays != null) {
                    val assignment = org.classapp.studyplanner.data.local.entity.Assignment(
                        id = newId.toInt(),
                        courseId = courseId,
                        title = title,
                        description = description,
                        deadline = deadline,
                        notification = notificationDays,
                        priority = selectedPriority
                    )
                    NotificationScheduler.scheduleNotification(requireContext(), assignment)
                }

                Toast.makeText(requireContext(), "Assignment created successfully", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            R.style.Theme_StudyPlanner_PickerDialog,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                
                val dateString = String.format(Locale.getDefault(), "%02d/%02d/%04d", month + 1, dayOfMonth, year)
                binding.etDeadline.setText(dateString)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            R.style.Theme_StudyPlanner_PickerDialog,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                
                val amPm = if (hourOfDay < 12) "AM" else "PM"
                val hour = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
                val timeString = String.format(Locale.getDefault(), "%02d:%02d %s", hour, minute, amPm)
                binding.etTime.setText(timeString)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
        timePickerDialog.show()
    }

    private fun updatePriorityUI() {
        // Reset all to unselected
        binding.btnPriorityLow.apply {
            background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_priority_unselected)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.inactive_grey))
        }
        binding.btnPriorityMedium.apply {
            background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_priority_unselected)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.inactive_grey))
        }
        binding.btnPriorityHigh.apply {
            background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_priority_unselected)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.inactive_grey))
        }

        // Highlight selected
        when (selectedPriority) {
            Priority.LOW -> {
                binding.btnPriorityLow.apply {
                    background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_priority_low)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.low_priority_text))
                }
            }
            Priority.MEDIUM -> {
                binding.btnPriorityMedium.apply {
                    background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_priority_medium)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.medium_priority_text))
                }
            }
            Priority.HIGH -> {
                binding.btnPriorityHigh.apply {
                    background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_priority_high)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.high_priority_text))
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
