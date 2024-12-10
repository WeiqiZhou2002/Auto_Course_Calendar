package com.cs407.autocoursecalendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cs407.autocoursecalendar.R
import com.cs407.autocoursecalendar.data.AppDatabase
import com.cs407.autocoursecalendar.data.Course
import com.cs407.autocoursecalendar.utils.TextProcessor
import kotlinx.coroutines.launch

class TextProcessorFragment : Fragment() {

    private lateinit var inputCourseText: EditText
    private lateinit var processButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragement_auto_course, container, false)

        inputCourseText = view.findViewById(R.id.input_course_text)
        processButton = view.findViewById(R.id.btn_process)

        processButton.setOnClickListener {
            val inputText = inputCourseText.text.toString()

            if (inputText.isBlank()) {
                Toast.makeText(requireContext(), "Input cannot be empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Process the text
            val semesterId = arguments?.getLong("semesterId") ?: 0L
            val courses = TextProcessor.processCourseText(inputText, semesterId)

            if (courses.isNotEmpty()) {
                // Save courses to the database
                val database = AppDatabase.getDatabase(requireContext())
                lifecycleScope.launch {
                    database.courseDao().insertCourses(courses)
                    Toast.makeText(requireContext(), "Courses successfully processed!", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp() // Navigate back
                }
            } else {
                Toast.makeText(requireContext(), "No valid courses found.", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}
