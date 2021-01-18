package com.santtuhyvarinen.habittracker.utils

import com.santtuhyvarinen.habittracker.models.DateStatusModel
import com.santtuhyvarinen.habittracker.models.HabitWithTaskLogs
import org.joda.time.DateTime

class TaskUtil {
    companion object {

        const val STATUS_SUCCESS = "success"
        const val STATUS_SKIPPED = "skip"
        const val STATUS_FAILED = "failed"
        const val STATUS_NONE = "none"

        fun hasTaskLogForToday(habitWithTaskLogs: HabitWithTaskLogs) : Boolean {
            val currentTime = System.currentTimeMillis()
            val startTime = DateTime(currentTime).withTimeAtStartOfDay()
            val endTime = startTime.plusDays(1)

            val startTimeMillis = startTime.millis
            val endTimeMillis = endTime.millis

            for(taskLog in habitWithTaskLogs.taskLogs) {
                if(taskLog.timestamp in startTimeMillis..endTimeMillis) {
                    return true
                }
            }

            return false
        }

        //Returns an array for Habit that contains Habit task status from last seven days
        fun getDateStatusModelsForHabit(habitWithTaskLogs: HabitWithTaskLogs, fromDate : DateTime, days : Int) : Array<DateStatusModel> {
            val array = Array(days) { DateStatusModel("", STATUS_NONE) }

            var date = fromDate
            for(i in array.indices) {
                val dateStatusModel = array[i]
                dateStatusModel.date = CalendarUtil.getDateTextShort(date.millis)

                val startTime = DateTime(date).withTimeAtStartOfDay()
                val endTime = startTime.plusDays(1)
                val startTimeMillis = startTime.millis
                val endTimeMillis = endTime.millis

                for(taskLog in habitWithTaskLogs.taskLogs) {
                    if(taskLog.timestamp in startTimeMillis..endTimeMillis) {
                        dateStatusModel.status = taskLog.status
                        break
                    }
                }

                date = date.minusDays(1)
            }

            array.reverse()

            return array
        }
    }
}