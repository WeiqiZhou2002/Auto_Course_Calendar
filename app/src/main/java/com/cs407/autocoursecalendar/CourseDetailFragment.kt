package com.cs407.autocoursecalendar

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.cs407.autocoursecalendar.data.AppDatabase
import com.cs407.autocoursecalendar.data.Course
import kotlinx.coroutines.launch
import java.util.*

class CourseDetailFragment : Fragment() {

    private lateinit var courseNameInput: EditText
    private lateinit var locationInput: EditText
    private lateinit var instructorInput: EditText
    private lateinit var startTimeTextView: TextView
    private lateinit var endTimeTextView: TextView
    private lateinit var selectedWeekdaysTextView: TextView
    private lateinit var addToSemesterButton: Button

    private val selectedWeekdays = mutableListOf<Weekday>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_course_to_calendar, container, false)

        // Initialize UI components
        courseNameInput = view.findViewById(R.id.courseName)
        locationInput = view.findViewById(R.id.location)
        instructorInput = view.findViewById(R.id.instructor)
        startTimeTextView = view.findViewById(R.id.startTime)
        endTimeTextView = view.findViewById(R.id.endTime)
        selectedWeekdaysTextView = view.findViewById(R.id.selectedWeekdays)
        addToSemesterButton = view.findViewById(R.id.addToSemesterButton)

        // Set up time pickers
        startTimeTextView.setOnClickListener {
            showTimePicker { time ->
                startTimeTextView.text = time
            }
        }

        endTimeTextView.setOnClickListener {
            showTimePicker { time ->
                endTimeTextView.text = time
            }
        }

        // Set up weekdays selection
        selectedWeekdaysTextView.setOnClickListener {
            showWeekdaySelectionDialog()
        }

        // Add course to semester button listener
        addToSemesterButton.setOnClickListener {
            saveCourseToSemester()
        }

        return view
    }

    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePicker = TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
            onTimeSelected(formattedTime)
        }, hour, minute, true)

        timePicker.show()
    }

    private fun showWeekdaySelectionDialog() {
        val weekdays = Weekday.values()
        val weekdayNames = weekdays.map { it.name.lowercase().replaceFirstChar { it.uppercase() } }.toTypedArray()
        val selectedItems = BooleanArray(weekdays.size) { selectedWeekdays.contains(weekdays[it]) }

        AlertDialog.Builder(requireContext())
            .setTitle("Select Weekdays")
            .setMultiChoiceItems(weekdayNames, selectedItems) { _, which, isChecked ->
                if (isChecked) {
                    selectedWeekdays.add(weekdays[which])
                } else {
                    selectedWeekdays.remove(weekdays[which])
                }
            }
            .setPositiveButton("OK") { _, _ ->
                updateSelectedWeekdaysText()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateSelectedWeekdaysText() {
        if (selectedWeekdays.isEmpty()) {
            selectedWeekdaysTextView.text = "Select Weekdays"
        } else {
            selectedWeekdaysTextView.text = selectedWeekdays.joinToString(", ") { it.name }
        }
    }

    private fun saveCourseToSemester() {
        val courseName = courseNameInput.text.toString()
        val location = locationInput.text.toString()
        val instructor = instructorInput.text.toString()
        val startTime = startTimeTextView.text.toString()
        val endTime = endTimeTextView.text.toString()

        // Validate inputs
        if (courseName.isBlank() || location.isBlank() || instructor.isBlank()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        if (startTime == "Select Start Time" || endTime == "Select End Time") {
            Toast.makeText(requireContext(), "Please select valid start and end times", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedWeekdays.isEmpty()) {
            Toast.makeText(requireContext(), "Please select at least one weekday", Toast.LENGTH_SHORT).show()
            return
        }

        // Placeholder semester ID (replace with actual logic to get semester ID)
        val semesterId = arguments?.getLong("semesterId") ?: 1L

        // Save to database
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(requireContext())
                val course = Course(
                    semesterId = semesterId,
                    courseCode = "", // Add logic for course code if necessary
                    courseName = courseName,
                    instructor = instructor,
                    location = location,
                    startTime = startTime,
                    endTime = endTime,
                    frequency = selectedWeekdays // Save multiple selected weekdays
                )

                db.courseDao().insertCourse(course)
                Toast.makeText(requireContext(), "Course added successfully!", Toast.LENGTH_SHORT).show()
                clearInputs()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error saving course: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearInputs() {
        courseNameInput.text.clear()
        locationInput.text.clear()
        instructorInput.text.clear()
        startTimeTextView.text = "Select Start Time"
        endTimeTextView.text = "Select End Time"
        selectedWeekdays.clear()
        updateSelectedWeekdaysText()
    }
}
