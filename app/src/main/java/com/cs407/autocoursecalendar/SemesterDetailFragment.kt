package com.cs407.autocoursecalendar

import android.app.AlertDialog
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
import com.cs407.autocoursecalendar.data.Semester
import kotlinx.coroutines.launch
import java.util.Calendar
import com.cs407.autocoursecalendar.data.AppDatabase

class SemesterDetailFragment : Fragment() {

    private lateinit var saveButton: Button
    private lateinit var buttonYear: Button
    private lateinit var buttonSeason: Button
    private lateinit var startDateInput: EditText
    private lateinit var endDateInput: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_semester, container, false)

        // Initialize buttons and texts
        saveButton = view.findViewById(R.id.saveButton)
        buttonYear = view.findViewById(R.id.buttonYear)
        buttonSeason = view.findViewById(R.id.buttonSeason)
        startDateInput = view.findViewById(R.id.startDateInput)
        endDateInput = view.findViewById(R.id.endDateInput)

        // set year/season buttons to select
        setupYearButton()
        setupSeasonButton()

        // Save logic
        saveButton.setOnClickListener {
            // semester information
            val year = buttonYear.text.toString().toIntOrNull()
            val season = buttonSeason.text.toString()
            val startDate = startDateInput.text.toString()
            val endDate = endDateInput.text.toString()

            if (year != null && season.isNotBlank() && startDate.isNotBlank() && endDate.isNotBlank()) {
                val semester = Semester(
                    year = year,
                    season = season,
                    startDate = startDate,
                    endDate = endDate
                )

                lifecycleScope.launch {
                    val db = AppDatabase.getDatabase(requireContext())
                    db.semesterDao().insertSemester(semester)
                    Toast.makeText(requireContext(), "Semester added!", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressed()
                }
            } else {
                Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
            }

            // navigate to course list
            findNavController().navigate(R.id.action_semesterDetailFragment_to_courseListFragment)
        }

        return view
    }

    private fun setupYearButton() {
        buttonYear.setOnClickListener {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val years = (currentYear - 5..currentYear + 5).map { it.toString() }.toTypedArray()

            AlertDialog.Builder(requireContext())
                .setTitle("Select Year")
                .setItems(years) { _, which ->
                    buttonYear.text = years[which]
                }
                .show()
        }
    }

    private fun setupSeasonButton() {
        buttonSeason.setOnClickListener {
            val seasons = arrayOf("Spring", "Summer", "Fall", "Winter")

            AlertDialog.Builder(requireContext())
                .setTitle("Select Season")
                .setItems(seasons) { _, which ->
                    buttonSeason.text = seasons[which]
                }
                .show()
        }
    }
}
