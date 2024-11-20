package com.cs407.autocoursecalendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.autocoursecalendar.data.Semester
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SemesterListFragment : Fragment() {

    private lateinit var semesterRecyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private val semesters = mutableListOf<Semester>()
    private lateinit var adapter: SemesterAdapter

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
            val action = SemesterListFragmentDirections.actionSemesterListToCourseList()
            findNavController().navigate(action)
        }, onItemLongClick = { semester ->
            // Navigate to Semester Detail
            val action = SemesterListFragmentDirections.actionSemesterListToSemesterDetail()
            findNavController().navigate(action)
        })
        semesterRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        semesterRecyclerView.adapter = adapter

        // Floating Action Button to create a new semester
        fab.setOnClickListener {
            val action = SemesterListFragmentDirections.actionSemesterListToSemesterDetail()
            findNavController().navigate(action)
        }

        loadSemesters()
        return view
    }

    private fun loadSemesters() {
        // Example data (replace with database query)
        semesters.add(Semester(year = 2024, season = "Fall", startDate = "2024-09-01", endDate = "2024-12-31"))
        semesters.add(Semester(year = 2024, season = "Spring", startDate = "2024-01-01", endDate = "2024-05-15"))
        adapter.notifyDataSetChanged()
    }
}
