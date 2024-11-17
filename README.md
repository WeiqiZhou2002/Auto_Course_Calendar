
# Course Schedule Application

## Overview
The **Course Schedule Application** is an Android app that helps students manage their academic schedules by allowing them to add semesters, parse course schedules (manually or automatically), and integrate these schedules into their calendar.

## Features
- Add new semesters with year, season, and start/end dates.
- Add courses using either:
  - **Auto Mode**: Paste a course schedule to parse course details automatically.
  - **Manual Mode**: Enter course details (course name, location, time, and days) manually.
- View parsed course schedules.
- Export course schedules to a calendar.

## Project Folder Structure
```plaintext
com.cs407.autocoursecalendar/
├── data/                            # Data-related classes
│   ├── CalendarDatabase.kt          # Room database configuration
│   ├── Course.kt                    # Course entity
│   ├── Semester.kt                  # Semester entity
│   ├── Weekday.kt                   # Weekday enum
│   └── CourseRepository.kt          # Repository for data operations
├── utils/                           # Utility classes
│   └── TextProcessor.kt             # Text parsing logic for Auto Mode
├── MainActivity.kt                  # Main activity for navigation
├── SemesterFragment.kt              # Handles semester-related UI
└── CourseFragment.kt                # Handles course input and display
```

## Wireframe
The app flow based on the wireframe:
1. **Landing Page (MainActivity)**: Displays semester list (e.g., Fall 2024).
2. **Add Semester (SemesterFragment)**: Allows adding new semester details.
3. **Auto Mode (CourseFragment)**: Paste course schedules to process automatically.
4. **Manual Mode (CourseFragment)**: Manually input course details.

## Technologies Used
- **Kotlin**: For Android development.
- **Room Database**: For local data storage.
- **Type Converters**: To handle complex data types like `List<Weekday>`.
- **Coroutines**: For asynchronous database operations.

## Database Schema
### Semester Table
- **id**: Primary key (auto-generated).
- **year**: Semester year (e.g., 2024).
- **season**: Semester season (e.g., Fall).
- **startDate**: Start date of the semester.
- **endDate**: End date of the semester.

### Course Table
- **id**: Primary key (auto-generated).
- **semesterId**: Foreign key linking to the Semester table.
- **courseCode**: Course code (e.g., CS407).
- **courseName**: Name of the course.
- **instructor**: Instructor of the course.
- **location**: Location of the class.
- **startTime**: Start time of the class.
- **endTime**: End time of the class.
- **frequency**: List of weekdays when the class occurs (uses `Weekday` enum).

## Installation
1. Clone this repository:
   ```bash
   git clone <repository-url>
   ```
2. Open the project in Android Studio.
3. Sync Gradle files and build the project.
4. Run the app on an emulator or a physical device.

## Usage
1. **Add Semester**:
   - Navigate to the Semester page and add semester details.
2. **Add Courses**:
   - Use Auto Mode to paste and parse a schedule.
   - Use Manual Mode to input course details.
3. **Export to Calendar**:
   - Once courses are added, integrate the schedule into your device calendar.

## Contributing
Contributions are welcome! Fork the repository, make changes, and submit a pull request.

## License
This project is licensed under the MIT License.

## Contact
For questions or feedback, please contact [your email or GitHub profile].
