package com.santtuhyvarinen.habittracker.managers

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.santtuhyvarinen.habittracker.database.repositories.TaskLogRepository
import com.santtuhyvarinen.habittracker.models.Habit
import com.santtuhyvarinen.habittracker.models.TaskModel
import com.santtuhyvarinen.habittracker.utils.CalendarUtil

class TaskManager {

    companion object {
        const val STATUS_SUCCESS = "success"
        const val STATUS_SKIPPED = "skip"
        const val STATUS_FAILED = "failed"
    }

    val tasks : MutableLiveData<ArrayList<TaskModel>> by lazy {
        MutableLiveData<ArrayList<TaskModel>>()
    }

    suspend fun generateTasks(context: Context, habits : List<Habit>, taskLogRepository: TaskLogRepository) {
        val taskList = ArrayList<TaskModel>()

        for(habit in habits) {
            val taskLogs = taskLogRepository.getTaskLogsByHabit(habit)
            
            if(taskLogs.isEmpty() && CalendarUtil.isRRULEToday(context, habit.taskRecurrence)) {
                taskList.add(TaskModel(habit))
            }
        }

        taskList.sortWith (compareByDescending<TaskModel> { it.habit.priority}.thenBy { it.habit.name} )

        tasks.value = taskList
    }
}