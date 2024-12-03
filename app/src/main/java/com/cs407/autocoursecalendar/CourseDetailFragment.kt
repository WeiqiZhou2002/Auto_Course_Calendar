package com.cs407.autocoursecalendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.cs407.autocoursecalendar.data.AppDatabase
import com.cs407.autocoursecalendar.data.Course
import kotlinx.coroutines.launch
import java.util.*

class CourseDetailFragment : Fragment() {

    private lateinit var saveButton: Button
    private lateinit var buttonYear: Button
    private lateinit var buttonSeason: Button
    private lateinit var startDateInput: EditText
    private lateinit var endDateInput: EditText
    private lateinit var courseNameInput: EditText
    private lateinit var locationInput: EditText
    private lateinit var instructorInput: EditText
    private lateinit var addCourseButton: ImageButton
    private lateinit var btnMon: ToggleButton
    private lateinit var btnTue: ToggleButton
    private lateinit var btnWed: ToggleButton
    private lateinit var btnThu: ToggleButton
    private lateinit var btnFri: ToggleButton
    private lateinit var btnSat: ToggleButton
    private lateinit var btnSun: ToggleButton
    private lateinit var addToSemesterButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_course_to_calendar, container, false)

        // Initialize UI components
        courseNameInput = view.findViewById(R.id.courseName)
        locationInput = view.findViewById(R.id.location)
        instructorInput = view.findViewById(R.id.instructor)
        addCourseButton = view.findViewById(R.id.addCourseButton)
        btnMon = view.findViewById(R.id.btnMon)
        btnTue = view.findViewById(R.id.btnTue)
        btnWed = view.findViewById(R.id.btnWed)
        btnThu = view.findViewById(R.id.btnThu)
        btnFri = view.findViewById(R.id.btnFri)
        btnSat = view.findViewById(R.id.btnSat)
        btnSun = view.findViewById(R.id.btnSun)
        addToSemesterButton = view.findViewById(R.id.addToSemesterButton)

        // Set up button click listeners
        addCourseButton.setOnClickListener {
            clearInputs()
        }

        addToSemesterButton.setOnClickListener {
            saveCourseToSemester()
        }

        return view
    }

    private fun clearInputs() {
        courseNameInput.text.clear()
        locationInput.text.clear()
        instructorInput.text.clear()
        btnMon.isChecked = false
        btnTue.isChecked = false
        btnWed.isChecked = false
        btnThu.isChecked = false
        btnFri.isChecked = false
        btnSat.isChecked = false
        btnSun.isChecked = false
    }

    private fun saveCourseToSemester() {
        // Retrieve inputs from the UI
        val courseName = courseNameInput.text.toString()
        val location = locationInput.text.toString()
        val instructor = instructorInput.text.toString()
        val startTime = view?.findViewById<TextView>(R.id.startTime)?.text.toString()
        val endTime = view?.findViewById<TextView>(R.id.endTime)?.text.toString()

        // Validate that times are selected
        if (startTime == "Select Start Time" || endTime == "Select End Time") {
            Toast.makeText(requireContext(), "Please select valid start and end times", Toast.LENGTH_SHORT).show()
            return
        }

        // Collect selected weekdays
        val days = mutableListOf<Weekday>()
        if (btnMon.isChecked) days.add(Weekday.MONDAY)
        if (btnTue.isChecked) days.add(Weekday.TUESDAY)
        if (btnWed.isChecked) days.add(Weekday.WEDNESDAY)
        if (btnThu.isChecked) days.add(Weekday.THURSDAY)
        if (btnFri.isChecked) days.add(Weekday.FRIDAY)
        if (btnSat.isChecked) days.add(Weekday.SATURDAY)
        if (btnSun.isChecked) days.add(Weekday.SUNDAY)

        // Validate required fields
        if (courseName.isBlank() || location.isBlank() || instructor.isBlank() || days.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the semesterId (example: passed as a navigation argument or default for now)
        val semesterId = arguments?.getLong("semesterId") ?: 1L // Replace `1L` with a real value

        // Insert course into the database
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(requireContext())
                val course = Course(
                    semesterId = semesterId,
                    courseCode = "", // Optional, add a course code input if necessary
                    courseName = courseName,
                    instructor = instructor,
                    location = location,
                    startTime = startTime,
                    endTime = endTime,
                    frequency = days
                )

                db.courseDao().insertCourse(course)

                Toast.makeText(requireContext(), "Course added successfully!", Toast.LENGTH_SHORT).show()
                clearInputs()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error adding course: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


}
