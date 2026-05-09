package org.classapp.studyplanner

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

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
                    Toast.makeText(this, "Home selected", Toast.LENGTH_SHORT).show()
                    replaceFragment(HomeFragment())
                    true
                }

                R.id.navigation_calendar -> {
                    Toast.makeText(this, "Calendar selected", Toast.LENGTH_SHORT).show()
                    replaceFragment(CalendarFragment())
                    true
                }

                R.id.navigation_add -> {
                    Toast.makeText(this, "Add selected", Toast.LENGTH_SHORT).show()
                    replaceFragment(AddTaskFragment())
                    true
                }

                R.id.navigation_subjects -> {
                    Toast.makeText(this, "Subjects selected", Toast.LENGTH_SHORT).show()
                    replaceFragment(SubjectFragment())
                    true
                }

                R.id.navigation_tasks -> {
                    Toast.makeText(this, "Tasks selected", Toast.LENGTH_SHORT).show()
                    replaceFragment(TaskFragment())
                    true
                }

                else -> false
            }
        }
        replaceFragment(HomeFragment())
    }

    private fun replaceFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }
}