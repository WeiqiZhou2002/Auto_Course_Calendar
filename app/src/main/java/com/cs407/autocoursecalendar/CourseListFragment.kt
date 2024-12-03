package com.cs407.autocoursecalendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
