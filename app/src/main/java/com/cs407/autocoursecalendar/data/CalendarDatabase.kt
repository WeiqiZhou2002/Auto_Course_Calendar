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
    val startDate: String, // MM/dd/yyyy
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

    @Delete
    suspend fun delete(semester: Semester)

    @Query("SELECT startDate FROM semester WHERE id = :semesterId")
    suspend fun getSemesterStartDate(semesterId: Long): String

    @Query("SELECT endDate FROM semester WHERE id = :semesterId")
    suspend fun getSemesterEndDate(semesterId: Long): String


}

// DAO: CourseDao
@Dao
interface CourseDao {
    @Insert
    suspend fun insertCourse(course: Course): Long

    @Query("SELECT * FROM course WHERE semesterId = :semesterId")
    suspend fun getCoursesBySemester(semesterId: Long): List<Course>

    @Delete
    suspend fun delete(course: Course)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<Course>)

}

// Database
@Database(entities = [Semester::class, Course::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun semesterDao(): SemesterDao
    abstract fun courseDao(): CourseDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

