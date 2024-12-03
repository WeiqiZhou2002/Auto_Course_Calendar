package com.cs407.autocoursecalendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.autocoursecalendar.data.AppDatabase
import com.cs407.autocoursecalendar.data.Semester
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SemesterListFragment : Fragment() {

    private lateinit var semesterRecyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private val semesters = mutableListOf<Semester>()
    private lateinit var adapter: SemesterAdapter
    private lateinit var viewModel: SemesterViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_semester_list, container, false)

        // Initialize views
        semesterRecyclerView = view.findViewById(R.id.semesterRecyclerView)
        fab = view.findViewById(R.id.fab)

        // Set up RecyclerView
        adapter = SemesterAdapter(semesters, onItemClick = { semester ->
            // Navigate to Course List
            val action = SemesterListFragmentDirections.actionSemesterListToCourseList(semesterId = semester.id)
            findNavController().navigate(action)
        }, onItemLongClick = { semester ->
            // Navigate to Semester Detail
            val action = SemesterListFragmentDirections.actionSemesterListToSemesterDetail()
            findNavController().navigate(action)
        })
        semesterRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        semesterRecyclerView.adapter = adapter

        // Initialize ViewModel
        val database = AppDatabase.getDatabase(requireContext())
        viewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(SemesterViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return SemesterViewModel(database) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        )[SemesterViewModel::class.java]

        // Load semesters
        lifecycleScope.launch {
            val semesterList = viewModel.getSemesters()
            semesters.clear()
            semesters.addAll(semesterList)
            adapter.notifyDataSetChanged()
        }

        // Floating Action Button to create a new semester
        fab.setOnClickListener {
            val action = SemesterListFragmentDirections.actionSemesterListToSemesterDetail()
            findNavController().navigate(action)
        }

        return view
    }

    // ViewModel to handle database interactions
    class SemesterViewModel(private val database: AppDatabase) : ViewModel() {
        suspend fun getSemesters(): List<Semester> {
            return database.semesterDao().getAllSemesters()
        }
    }
}
