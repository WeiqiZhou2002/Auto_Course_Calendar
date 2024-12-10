package com.cs407.autocoursecalendar

enum class Weekday {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;
    companion object {
        fun fromString(day: String): Weekday? {
            return when (day.uppercase()) {
                "M", "MON", "MONDAY" -> MONDAY
                "T", "TUE", "TUESDAY" -> TUESDAY
                "W", "WED", "WEDNESDAY" -> WEDNESDAY
                "R", "THU", "THURSDAY" -> THURSDAY
                "F", "FRI", "FRIDAY" -> FRIDAY
                "SAT", "SATURDAY" -> SATURDAY
                "SUN", "SUNDAY" -> SUNDAY
                else -> null
            }
        }

        fun fromChar(ch: Char): Weekday? {
            // Single-character codes
            return when (ch.uppercaseChar()) {
                'M' -> MONDAY
                'T' -> TUESDAY
                'W' -> WEDNESDAY
                'R' -> THURSDAY
                'F' -> FRIDAY
                else -> null
            }
        }
    }
}