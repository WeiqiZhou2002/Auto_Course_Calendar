package com.cs407.autocoursecalendar

import android.content.ContentValues
import android.os.Bundle
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.autocoursecalendar.data.AppDatabase
import com.cs407.autocoursecalendar.data.Course
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.util.TimeZone

class CourseListFragment : Fragment() {

    private lateinit var courseRecyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private val courses = mutableListOf<Course>()
    private lateinit var adapter: CourseAdapter
    private lateinit var viewModel: CourseViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.fragment_course_list, container, false)

        // Initialize views
        courseRecyclerView = view.findViewById(R.id.courseRecyclerView)
        fab = view.findViewById(R.id.fab)

        // Set up RecyclerView
        adapter = CourseAdapter(courses, onItemLongClick = { course -> showDeleteBottomSheet(course) })
        courseRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        courseRecyclerView.adapter = adapter

        // Initialize ViewModel
        val database = AppDatabase.getDatabase(requireContext())
        viewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(CourseViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return CourseViewModel(database) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        )[CourseViewModel::class.java]

        // Load courses
        lifecycleScope.launch {
            val semesterId = arguments?.getLong("semesterId") ?: 0L
            val courseList = viewModel.getCoursesBySemester(semesterId)
            courses.clear()
            courses.addAll(courseList)
            adapter.notifyDataSetChanged()
        }

        // Floating Action Button to create a new course
        fab.setOnClickListener {
            val semesterId = arguments?.getLong("semesterId") ?: 0L
            val action = CourseListFragmentDirections.actionCourseListToCourseDetail(semesterId=semesterId)
            findNavController().navigate(action)
        }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate the menu from the resource file
        inflater.inflate(R.menu.course_list_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_to_calendar -> {
                addCoursesToCalendar()
                return true
            }
            R.id.action_auto -> {
                // Navigate to TextProcessorFragment
                val semesterId = arguments?.getLong("semesterId") ?: 0L
                val action = CourseListFragmentDirections.actionCourseListToAutoCourse(semesterId)
                findNavController().navigate(action)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addCoursesToCalendar() {
        lifecycleScope.launch {
            try {
                val semesterId = arguments?.getLong("semesterId") ?: return@launch
                val courses = viewModel.getCoursesBySemester(semesterId)

                if (courses.isEmpty()) {
                    Toast.makeText(requireContext(), "No courses to add to calendar.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val contentResolver = requireContext().contentResolver

                for (course in courses) {
                    for (day in course.frequency) {
                        val eventValues = ContentValues().apply {
                            put(CalendarContract.Events.CALENDAR_ID, 1) // Replace with user's calendar ID
                            put(CalendarContract.Events.TITLE, course.courseName)
                            put(CalendarContract.Events.DESCRIPTION, "Instructor: ${course.instructor}")
                            put(CalendarContract.Events.EVENT_LOCATION, course.location)
                            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)

                            // Compute start and end times based on the day and time
                            val startMillis = computeEventTime(course.startTime, day, semesterId)
                            val endMillis = computeEventTime(course.endTime, day, semesterId)

                            put(CalendarContract.Events.DTSTART, startMillis)
                            put(CalendarContract.Events.DTEND, endMillis)
                            put(CalendarContract.Events.RRULE, "FREQ=WEEKLY") // Repeats weekly
                        }

                        val uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, eventValues)
                        if (uri != null) {
                            Toast.makeText(requireContext(), "Course ${course.courseName} added to calendar.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to add courses to calendar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun computeEventTime(time: String, day: Weekday, semesterId: Long): Long {
        // Logic to calculate the exact start or end time in milliseconds
        // Combine the date for the semester with the time string (e.g., "10:00")
        // Convert to milliseconds
        return 0L // Replace with actual computation logic
    }


    private fun showDeleteBottomSheet(course: Course) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_delete, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        val deleteButton = bottomSheetView.findViewById<Button>(R.id.deleteButton)
        val cancelButton = bottomSheetView.findViewById<Button>(R.id.cancelButton)

        deleteButton.setOnClickListener {
            lifecycleScope.launch {
                viewModel.deleteCourse(course)
                courses.remove(course)
                adapter.notifyDataSetChanged()
            }
            bottomSheetDialog.dismiss()
        }

        cancelButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }
}

class CourseViewModel(private val database: AppDatabase) : ViewModel() {

    suspend fun getCoursesBySemester(semesterId: Long): List<Course> {
        return database.courseDao().getCoursesBySemester(semesterId)
    }

    suspend fun deleteCourse(course: Course) {
        database.courseDao().delete(course)
    }
}
