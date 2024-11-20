package com.cs407.autocoursecalendar.data

import android.content.Context
import androidx.room.*
import androidx.room.TypeConverter
import com.cs407.autocoursecalendar.Weekday

// Entity: Semester
@Entity(tableName = "semester")
data class Semester(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val year: Int,
    val season: String, // e.g., "Spring", "Fall"
    val startDate: String, // ISO 8601 date format (e.g., "2024-09-01")
    val endDate: String
)

class Converters {
    @TypeConverter
    fun fromWeekdayList(list: List<Weekday>): String {
        return list.joinToString(",") { it.name }
    }

    @TypeConverter
    fun toWeekdayList(data: String): List<Weekday> {
        return data.split(",").map { Weekday.valueOf(it) }
    }
}

// Entity: Course
@Entity(
    tableName = "course",
    foreignKeys = [
        ForeignKey(
            entity = Semester::class,
            parentColumns = ["id"],
            childColumns = ["semesterId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Course(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val semesterId: Long,
    val courseCode: String,
    val courseName: String,
    val instructor: String,
    val location: String,
    val startTime: String, // Format: "10:00" (24-hour time format)
    val endTime: String,   // Format: "12:00" (24-hour time format)
    val frequency: List<Weekday> // E.g., ["Monday", "Wednesday", "Friday"]
)

// DAO: SemesterDao
@Dao
interface SemesterDao {
    @Insert
    suspend fun insertSemester(semester: Semester): Long

    @Query("SELECT * FROM semester")
    suspend fun getAllSemesters(): List<Semester>
}

// DAO: CourseDao
@Dao
interface CourseDao {
    @Insert
    suspend fun insertCourse(course: Course): Long

    @Query("SELECT * FROM course WHERE semesterId = :semesterId")
    suspend fun getCoursesBySemester(semesterId: Long): List<Course>
}

// Database
@Database(entities = [Semester::class, Course::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun semesterDao(): SemesterDao
    abstract fun courseDao(): CourseDao
}

// Database Provider
object DatabaseProvider {
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        if (INSTANCE == null) {
            synchronized(AppDatabase::class) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
            }
        }
        return INSTANCE!!
    }
}

