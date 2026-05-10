package org.classapp.studyplanner.ui.subject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.classapp.studyplanner.R
import org.classapp.studyplanner.data.local.database.AppDatabase
import org.classapp.studyplanner.data.repository.CourseRepository
import org.classapp.studyplanner.databinding.AddSubjectBinding

class AddSubjectFragment : Fragment() {

    private var _binding: AddSubjectBinding? = null
    private val binding get() = _binding!!

    private var selectedColorHex: String = "#FF5252" // Default to subject_red
    private lateinit var colorViews: List<ImageView>
    private lateinit var courseRepository: CourseRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AddSubjectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())
        courseRepository = CourseRepository(db.courseDao())

        setupColorPicker()
        setupListeners()
    }

    private fun setupColorPicker() {
        colorViews = listOf(
            binding.colorRed, binding.colorBlue, binding.colorGreen, binding.colorYellow,
            binding.colorPurple, binding.colorOrange, binding.colorPink, binding.colorTeal
        )

        val hexColors = listOf(
            "#FF5252", "#448AFF", "#4CAF50", "#FFEB3B",
            "#9C27B0", "#FF9800", "#E91E63", "#009688"
        )

        colorViews.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                selectColor(imageView, hexColors[index])
            }
        }

        // Select first color by default
        selectColor(binding.colorRed, hexColors[0])
    }

    private fun selectColor(view: ImageView, hexColor: String) {
        selectedColorHex = hexColor
        
        // Clear checkmark from all
        colorViews.forEach { it.setImageDrawable(null) }
        
        // Add checkmark to selected
        view.setImageResource(R.drawable.ic_check_24)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnCreateSubject.setOnClickListener {
            createSubject()
        }
    }

    private fun createSubject() {
        val name = binding.etSubjectName.text.toString().trim()
        val code = binding.etSubjectCode.text.toString().trim()

        if (name.isEmpty()) {
            binding.etSubjectName.error = "Name is required"
            return
        }

        if (code.isEmpty()) {
            binding.etSubjectCode.error = "Code is required"
            return
        }

        lifecycleScope.launch {
            try {
                courseRepository.createCourse(name, code, selectedColorHex)
                Toast.makeText(requireContext(), "Subject created successfully", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
