package org.classapp.studyplanner.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import org.classapp.studyplanner.data.local.database.AppDatabase
import org.classapp.studyplanner.data.local.entity.Status
import org.classapp.studyplanner.data.repository.AssignmentRepository
import org.classapp.studyplanner.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var assignmentRepository: AssignmentRepository
    private lateinit var adapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())
        assignmentRepository = AssignmentRepository(db.assignmentDao())

        setupRecyclerView()
        setupListeners()
        loadNotifications()
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter { item ->
            markAsRead(item.assignment.id)
        }
        binding.rvNotifications.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotifications.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        binding.btnMarkAllRead.setOnClickListener {
            markAllAsRead()
        }
    }

    private fun loadNotifications() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Fetch only unread assignments that aren't completed
            val allTasks = assignmentRepository.getAllAssignments()
            val unreadNotifications = allTasks
                .filter { it.assignment.status != Status.TURNED_IN && !it.assignment.isRead }
                .sortedBy { it.assignment.deadline }

            adapter.submitList(unreadNotifications)
            
            val unreadCount = unreadNotifications.size
            binding.tvUnreadCount.text = "$unreadCount unread"
        }
    }

    private fun markAsRead(id: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            assignmentRepository.markAsRead(id)
            loadNotifications()
        }
    }

    private fun markAllAsRead() {
        viewLifecycleOwner.lifecycleScope.launch {
            assignmentRepository.markAllAsRead()
            loadNotifications()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
