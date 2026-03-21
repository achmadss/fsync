package dev.achmad.fsync.util

import android.content.Context
import dev.achmad.fsync.R
import java.time.Duration
import java.time.Instant

fun Instant.toRelativeTime(context: Context): String {
    val duration = Duration.between(this, Instant.now())
    return when {
        duration.toMinutes() < 1 -> context.getString(R.string.relative_just_now)
        duration.toMinutes() == 1L -> context.getString(R.string.relative_a_minute_ago)
        duration.toMinutes() < 60 -> context.getString(R.string.relative_minutes_ago, duration.toMinutes())
        duration.toHours() == 1L -> context.getString(R.string.relative_an_hour_ago)
        duration.toHours() < 24 -> context.getString(R.string.relative_hours_ago, duration.toHours())
        duration.toDays() == 1L -> context.getString(R.string.relative_yesterday)
        duration.toDays() < 7 -> context.getString(R.string.relative_days_ago, duration.toDays())
        duration.toDays() == 1L -> context.getString(R.string.relative_a_week_ago)
        duration.toDays() < 30 -> context.getString(R.string.relative_weeks_ago, duration.toDays() / 7)
        duration.toDays() < 365 -> context.getString(R.string.relative_months_ago, duration.toDays() / 30)
        duration.toDays() == 365L -> context.getString(R.string.relative_a_year_ago)
        else -> context.getString(R.string.relative_years_ago, duration.toDays() / 365)
    }
}
