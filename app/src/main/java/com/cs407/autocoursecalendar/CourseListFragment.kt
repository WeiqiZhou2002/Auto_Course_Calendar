package com.cs407.autocoursecalendar

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.CalendarContract
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import java.text.SimpleDateFormat
import java.util.*

class CourseListFragment : Fragment() {

    private lateinit var courseRecyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private val courses = mutableListOf<Course>()
    private lateinit var adapter: CourseAdapter
    private lateinit var viewModel: CourseViewModel
    private val CALENDAR_PERMISSION_REQUEST_CODE = 100

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
            showAddCourseBottomSheet()
        }


        return view
    }

    private fun showAddCourseBottomSheet() {
        // Create Bottom Sheet Dialog
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_add_courses, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        // Get the buttons
        val manualAddButton = bottomSheetView.findViewById<Button>(R.id.manual)
        val autoAddButton = bottomSheetView.findViewById<Button>(R.id.auto)
        val addToCalendarButton = bottomSheetView.findViewById<Button>(R.id.addToCalendar)

        // Handle the Manual Add button click
        manualAddButton.setOnClickListener {
            val semesterId = arguments?.getLong("semesterId") ?: 0L
            val action = CourseListFragmentDirections.actionCourseListToCourseDetail(semesterId = semesterId)
            findNavController().navigate(action)
            bottomSheetDialog.dismiss()
        }

        // Handle the Auto Add button click
        autoAddButton.setOnClickListener {
            val semesterId = arguments?.getLong("semesterId") ?: 0L
            val action = CourseListFragmentDirections.actionCourseListToAutoCourse(semesterId)
            findNavController().navigate(action)
            bottomSheetDialog.dismiss()
        }



        // Handle the Cancel button click
        addToCalendarButton.setOnClickListener {
            requestCalendarPermissionIfNeeded()
            bottomSheetDialog.dismiss()
        }

        // Show the Bottom Sheet Dialog
        bottomSheetDialog.show()
    }

    private fun requestCalendarPermissionIfNeeded() {
        val permissions = arrayOf(
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR
        )
        val missingPermissions = permissions.filter {
            requireContext().checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            requestPermissions(missingPermissions.toTypedArray(), CALENDAR_PERMISSION_REQUEST_CODE)
        } else {
            // Permissions already granted
            showCalendarSelectionDialog { calendarId ->
                addCoursesToCalendar(calendarId)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.course_list_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_to_calendar -> {
                requestCalendarPermissionIfNeeded()
                return true
            }
            R.id.action_auto -> {
                val semesterId = arguments?.getLong("semesterId") ?: 0L
                val action = CourseListFragmentDirections.actionCourseListToAutoCourse(semesterId)
                findNavController().navigate(action)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CALENDAR_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permissions granted
                showCalendarSelectionDialog { calendarId ->
                    addCoursesToCalendar(calendarId)
                }
            } else {
                // Permissions denied
                Toast.makeText(
                    requireContext(),
                    "Calendar permissions are required to add courses to the calendar.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    private fun formatDate(millis: Long): String {
        val dateFormat = java.text.SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.format(Date(millis))
    }

    private fun addCoursesToCalendar(calendarId: Long) {
        lifecycleScope.launch {
            try {
                val semesterId = arguments?.getLong("semesterId") ?: return@launch
                val courses = viewModel.getCoursesBySemester(semesterId)
                // Fetch the actual semester start and end dates from the database
                val semesterStartDateString = viewModel.getSemesterStartDate(semesterId)
                val semesterEndDateString = viewModel.getSemesterEndDate(semesterId)

                // Convert these date strings to Date objects
                val semesterStartDate = parseDate(semesterStartDateString)
                val semesterEndDate = parseDate(semesterEndDateString)

                // Convert Date to milliseconds
                val semesterStartMillis = semesterStartDate.time
                val semesterEndMillis = semesterEndDate.time
//                val semesterStartMillis = System.currentTimeMillis() // Replace with actual semester start date
//                val semesterEndMillis = semesterStartMillis + 4 * 30 * 24 * 60 * 60 * 1000L // Example: 4 months

                if (courses.isEmpty()) {
                    Toast.makeText(requireContext(), "No courses to add to calendar.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val contentResolver = requireContext().contentResolver

                for (course in courses) {
                    for (day in course.frequency) {
                        val startMillis = computeEventTime(course.startTime, day, semesterStartMillis)
                        val endMillis = computeEventTime(course.endTime, day, semesterStartMillis)

                        val eventValues = ContentValues().apply {
                            put(CalendarContract.Events.CALENDAR_ID, calendarId)
                            put(CalendarContract.Events.TITLE, course.courseName)
                            put(CalendarContract.Events.DESCRIPTION, "Instructor: ${course.instructor}")
                            put(CalendarContract.Events.EVENT_LOCATION, course.location)
                            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                            put(CalendarContract.Events.DTSTART, startMillis)
                            put(CalendarContract.Events.DTEND, endMillis)

                            // Recurrence rule for weekly events during the semester
                            put(CalendarContract.Events.RRULE, "FREQ=WEEKLY;UNTIL=${formatDate(semesterEndMillis)}")
                        }

                        val uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, eventValues)
                        if (uri != null) {
                            Toast.makeText(
                                requireContext(),
                                "Course ${course.courseName} added to calendar.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to add courses to calendar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun parseDate(dateString: String): Date {
        val format = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())  // Adjust format to match your date format
        return format.parse(dateString) ?: throw IllegalArgumentException("Invalid date format")
    }


    private fun computeEventTime(time: String, day: Weekday, semesterStartMillis: Long): Long {
        try {
            // Parse the time in "h:mm a" format (e.g., "2:30 PM")
            val dateFormat = SimpleDateFormat("h:mm a", Locale.US)
            val timeDate = dateFormat.parse(time) ?: throw IllegalArgumentException("Invalid time format: $time")

            // Use the semester's start date to compute the event time
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = semesterStartMillis

            // Set the day of the week and time
            val calendarDay = when (day) {
                Weekday.MONDAY -> Calendar.MONDAY
                Weekday.TUESDAY -> Calendar.TUESDAY
                Weekday.WEDNESDAY -> Calendar.WEDNESDAY
                Weekday.THURSDAY -> Calendar.THURSDAY
                Weekday.FRIDAY -> Calendar.FRIDAY
                Weekday.SATURDAY -> Calendar.SATURDAY
                Weekday.SUNDAY -> Calendar.SUNDAY
            }
            calendar.set(Calendar.DAY_OF_WEEK, calendarDay)
            val timeCalendar = Calendar.getInstance()
            timeCalendar.time = timeDate
            calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
            calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))

            return calendar.timeInMillis
        } catch (e: Exception) {
            e.printStackTrace()
            throw IllegalArgumentException("Error computing event time: $time")
        }
    }



    private fun showCalendarSelectionDialog(onCalendarSelected: (Long) -> Unit) {
        val calendarIds = mutableListOf<Pair<Long, String>>()
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
        )

        val cursor = requireContext().contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Calendars._ID))
                val name = it.getString(it.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME))
                calendarIds.add(id to name)
            }
        }

        if (calendarIds.isEmpty()) {
            Toast.makeText(requireContext(), "No calendars found on this device.", Toast.LENGTH_SHORT).show()
            return
        }

        val items = calendarIds.map { it.second }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("Select Calendar")
            .setItems(items) { _, which ->
                onCalendarSelected(calendarIds[which].first)
            }
            .show()
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

    suspend fun getSemesterStartDate(semesterId: Long): String {
        return database.semesterDao().getSemesterStartDate(semesterId)
    }

    suspend fun getSemesterEndDate(semesterId: Long): String {
        return database.semesterDao().getSemesterEndDate(semesterId)
    }
}
