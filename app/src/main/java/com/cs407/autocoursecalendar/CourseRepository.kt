package com.cs407.autocoursecalendar

import com.cs407.autocoursecalendar.data.AppDatabase
import com.cs407.autocoursecalendar.utils.TextProcessor

class CourseRepository(private val database: AppDatabase) {

    suspend fun saveCoursesToDatabase(input: String, semesterId: Long) {
        val courses = TextProcessor.processCourseText(input, semesterId)
        val courseDao = database.courseDao()

        for (course in courses) {
            courseDao.insertCourse(course)
        }
    }
}