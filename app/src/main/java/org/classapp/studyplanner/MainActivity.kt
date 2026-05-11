package org.classapp.studyplanner

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.classapp.studyplanner.ui.addtask.AddTaskFragment
import org.classapp.studyplanner.ui.calendar.CalendarFragment
import org.classapp.studyplanner.ui.home.HomeFragment
import org.classapp.studyplanner.ui.subject.SubjectFragment
import org.classapp.studyplanner.ui.task.TaskFragment

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Notifications disabled. You won't receive deadline reminders.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        checkNotificationPermission()

        bottomNavigation = findViewById(R.id.bottom_navigation)

        val navItemStates = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
        )

        val navItemColors = intArrayOf(
            ContextCompat.getColor(this, R.color.white),
            ContextCompat.getColor(this, R.color.black)
        )

        val navTintList = ColorStateList(navItemStates, navItemColors)
        bottomNavigation.itemIconTintList = navTintList
        bottomNavigation.itemActiveIndicatorHeight = bottomNavigation.itemActiveIndicatorWidth

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.navigation_calendar -> {
                    replaceFragment(CalendarFragment())
                    true
                }
                R.id.navigation_add -> {
                    replaceFragment(AddTaskFragment())
                    true
                }
                R.id.navigation_subjects -> {
                    replaceFragment(SubjectFragment())
                    true
                }
                R.id.navigation_tasks -> {
                    replaceFragment(TaskFragment())
                    true
                }
                else -> false
            }
        }
        replaceFragment(HomeFragment())
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun replaceFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }
}