package com.cs407.autocoursecalendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cs407.autocoursecalendar.data.Semester

class SemesterAdapter(
    private val semesters: List<Semester>,
    private val onItemClick: (Semester) -> Unit,
    private val onItemLongClick: (Semester) -> Unit
) : RecyclerView.Adapter<SemesterAdapter.SemesterViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SemesterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_semester, parent, false)
        return SemesterViewHolder(view)
    }

    override fun onBindViewHolder(holder: SemesterViewHolder, position: Int) {
        val semester = semesters[position]
        holder.bind(semester, onItemClick, onItemLongClick)
    }

    override fun getItemCount(): Int = semesters.size

    class SemesterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val semesterTitle: TextView = itemView.findViewById(R.id.SemesterTextView)
        private val semesterDates: TextView = itemView.findViewById(R.id.DateTextView)

        fun bind(
            semester: Semester,
            onItemClick: (Semester) -> Unit,
            onItemLongClick: (Semester) -> Unit
        ) {
            semesterTitle.text = "${semester.season} ${semester.year}"
            semesterDates.text = "${semester.startDate} - ${semester.endDate}"

            itemView.setOnClickListener { onItemClick(semester) }
            itemView.setOnLongClickListener {
                onItemLongClick(semester)
                true
            }
        }
    }
}
