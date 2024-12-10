package com.cs407.autocoursecalendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cs407.autocoursecalendar.data.Course

class CourseAdapter(
    private val courses: List<Course>,
    private val onItemLongClick: (Course) -> Unit
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courses[position]
        holder.bind(course, onItemLongClick)

    }

    override fun getItemCount(): Int = courses.size

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val courseTitle: TextView = itemView.findViewById(R.id.titleTextView)
        private val timeText: TextView = itemView.findViewById(R.id.timeTextView)
        private val locationText: TextView = itemView.findViewById(R.id.locationTextView)
        fun bind(
            course: Course,
            onItemLongClick: (Course) -> Unit
        ) {
            courseTitle.text = " ${course.courseCode} ${course.courseName}"
            timeText.text = "${course.startTime} - ${course.endTime}"
            locationText.text="${course.location}"
            itemView.setOnLongClickListener {
                onItemLongClick(course)
                true
            }
        }
    }
}
