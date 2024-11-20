package com.cs407.autocoursecalendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cs407.autocoursecalendar.data.Semester

class SemesterDetailFragment : Fragment() {

    private lateinit var saveButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_semester, container, false)

        // Initialize save button
        saveButton = view.findViewById(R.id.saveButton)

        // Save logic
        saveButton.setOnClickListener {
            // Save semester logic here
            val action = SemesterDetailFragmentDirections.actionSemesterDetailToCourseList()
            findNavController().navigate(action)
        }

        return view
    }
}
