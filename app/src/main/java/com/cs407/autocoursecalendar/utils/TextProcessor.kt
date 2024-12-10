package com.cs407.autocoursecalendar.utils

import com.cs407.autocoursecalendar.data.Course
import com.cs407.autocoursecalendar.Weekday

object TextProcessor {
    fun processCourseText(input: String, semesterId: Long): List<Course> {
        val courses = mutableListOf<Course>()
        val courseBlocks = input.split(Regex("(?=(?m)^\\s*[A-Za-z]+(?: [A-Za-z]+)* \\d{3}: )"))

        for (block in courseBlocks) {
            // Skip empty or blank blocks
            if (block.isBlank()) continue

            val lines = block.lines().filter { it.isNotBlank() }
            if (lines.isEmpty()) continue

            val codeAndTitle = lines[0].split(": ")
            val code = codeAndTitle[0].trim()
            val title = codeAndTitle.getOrElse(1) { "" }.trim()

            // Process each line that starts with LEC or DIS
            for (line in lines.drop(1)) {
                if (line.startsWith("LEC") || line.startsWith("DIS")) {
                    // Variables to hold meeting details for this specific LEC/DIS
                    var instructor = "Unknown"
                    var location = "Online"
                    var startTime = ""
                    var endTime = ""
                    val frequency = mutableListOf<Weekday>()

                    val parts = line.split(" ")
                    val type = parts[0] // LEC or DIS

                    // Extract times using regex
                    val timeRegex = Regex("\\d{1,2}:\\d{2} [AP]M")
                    val times = timeRegex.findAll(line).map { it.value }.toList()
                    if (times.size >= 2) {
                        startTime = times[0]
                        endTime = times[1]
                    }

                    // Identify day codes (e.g., M, TR, MWF)
                    // We'll assume day codes appear before times. For example: "LEC TR 2:30 PM - 3:45 PM ..."
                    val afterType = parts.drop(1)
                    // Find the segment composed of only letters that represents day codes
                    val dayCode = afterType.firstOrNull { seg -> seg.all { ch -> ch.isLetter() } } ?: ""

                    // Convert day code characters to weekdays
                    for (ch in dayCode) {
                        val day = Weekday.fromChar(ch)
                        if (day != null) {
                            frequency.add(day)
                        }
                    }

                    // Extract location: everything after the last time
                    if (times.size >= 2) {
                        location = line.substringAfter(endTime).trim()
                    }

                    // If frequency is empty, skip this course (asynchronous)
                    if (frequency.isEmpty()) {
                        continue
                    }

                    // Create a new Course for each LEC/DIS line
                    val modifiedTitle = "$title ($type)"
                    courses.add(
                        Course(
                            semesterId = semesterId,
                            courseCode = code,
                            courseName = modifiedTitle,
                            instructor = instructor,
                            location = location,
                            startTime = startTime,
                            endTime = endTime,
                            frequency = frequency
                        )
                    )
                }
            }
        }
        return courses
    }
}
