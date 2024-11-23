package com.cs407.autocoursecalendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.autocoursecalendar.data.AppDatabase
import com.cs407.autocoursecalendar.data.Course
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CourseListFragment : Fragment() {


    private lateinit var courseRecyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private val courses = mutableListOf<Course>()
    private lateinit var adapter: CourseAdapter

    private val courseDatabase by lazy {
        AppDatabase.getDatabase(requireContext())
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_course_list, container, false)

        // Initialize views
        courseRecyclerView = view.findViewById(R.id.courseRecyclerView)
        fab = view.findViewById(R.id.fab)

        // Set up RecyclerView
        adapter = CourseAdapter(courses, onItemLongClick = { course -> showDeleteBottomSheet(course)}
        )

        courseRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        courseRecyclerView.adapter = adapter

        // Floating Action Button to create a new semester
        fab.setOnClickListener {
            val action = CourseListFragmentDirections.actionCourseListToCourseDetail()
            findNavController().navigate(action)
        }

        loadCourses()
        return view
    }

    private fun showDeleteBottomSheet(course: Course) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_delete, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        val deleteButton = bottomSheetView.findViewById<Button>(R.id.deleteButton)
        val cancelButton = bottomSheetView.findViewById<Button>(R.id.cancelButton)

        deleteButton.setOnClickListener {
            deleteCourse(course)
            bottomSheetDialog.dismiss()
        }

        cancelButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }


    private fun deleteCourse(course: Course) {
        //Remove from Database
        CoroutineScope(Dispatchers.IO).launch {
            courseDatabase.courseDao().delete(course)
        }

        //Remove from List
        courses.remove(course)

        //Notify Adapter
        adapter.notifyDataSetChanged()
    }
    private fun loadCourses() {
        // Example data (replace with database query)
        courses.add(Course(semesterId = 2024, courseCode = "407",
            courseName = "Mobile", instructor = "Mouna", location = "online",
            startTime = "2024-09-12", endTime = "2024-12-15",
            frequency = listOf(Weekday.TUESDAY, Weekday.THURSDAY)))
        courses.add(Course(semesterId = 2024, courseCode = "544",
            courseName = "BigData", instructor = "Tyler", location = "Agriculter",
            startTime = "2024-09-12", endTime = "2024-12-15",
            frequency = listOf(Weekday.MONDAY, Weekday.WEDNESDAY, Weekday.FRIDAY)))
        adapter.notifyDataSetChanged()
    }


}