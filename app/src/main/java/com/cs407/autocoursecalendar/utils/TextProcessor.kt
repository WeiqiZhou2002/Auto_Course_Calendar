package com.cs407.autocoursecalendar.utils

import com.cs407.autocoursecalendar.data.Course
import com.cs407.autocoursecalendar.Weekday

object TextProcessor {
    fun processCourseText(input: String, semesterId: Long): List<Course> {
        val courses = mutableListOf<Course>()
        val courseBlocks = input.split("\n\n") // Split by blank lines to separate courses

        for (block in courseBlocks) {
            val lines = block.lines()
            val codeAndTitle = lines[0].split(": ")
            val code = codeAndTitle[0].trim()
            val title = codeAndTitle.getOrElse(1) { "" }.trim()

            // Variables to hold meeting details
            var instructor = "Unknown"
            var location = "Online"
            var startTime = ""
            var endTime = ""
            val frequency = mutableListOf<Weekday>()

            for (line in lines.drop(1)) {
                when {
                    line.contains("Weekly Meetings", true) -> Unit // Skip the label
                    line.startsWith("LEC") || line.startsWith("DIS") -> {
                        val parts = line.split(" ")
                        val type = parts[0] // LEC or DIS
                        val daysAndTime = parts.drop(1).joinToString(" ")
                        val daysRegex = Regex("[A-Za-z]+") // Match days
                        val timeRegex = Regex("\\d{1,2}:\\d{2} [AP]M")

                        // Map days to Weekday enum
                        val days = daysRegex.findAll(daysAndTime).mapNotNull { match ->
                            try {
                                Weekday.valueOf(match.value.uppercase())
                            } catch (e: IllegalArgumentException) {
                                null // Ignore invalid values
                            }
                        }.toList()

                        val times = timeRegex.findAll(daysAndTime).map { it.value }.toList()

                        if (times.size >= 2) {
                            startTime = times[0]
                            endTime = times[1]
                        }
                        if (days.isNotEmpty()) {
                            frequency.addAll(days)
                        }
                        if (daysAndTime.contains("Room")) {
                            location = daysAndTime.substringAfter("Room").trim()
                        }
                    }
                }
            }

            // Create and add a Course object to the list
            courses.add(
                Course(
                    semesterId = semesterId,
                    courseCode = code,
                    courseName = title,
                    instructor = instructor,
                    location = location,
                    startTime = startTime,
                    endTime = endTime,
                    frequency = frequency
                )
            )
        }
        return courses
    }


}