package com.santtuhyvarinen.habittracker.viewmodels

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.santtuhyvarinen.habittracker.R
import com.santtuhyvarinen.habittracker.managers.DatabaseManager
import com.santtuhyvarinen.habittracker.managers.HabitInfoManager
import com.santtuhyvarinen.habittracker.managers.IconManager
import com.santtuhyvarinen.habittracker.models.Habit
import com.santtuhyvarinen.habittracker.models.IconModel
import com.santtuhyvarinen.habittracker.models.WeekDaysSelectionModel
import com.santtuhyvarinen.habittracker.utils.CalendarUtil
import kotlinx.coroutines.launch

class HabitFormViewModel : ViewModel() {

    private var initialized = false

    var loading = false

    var habitId : Long = -1L
    val habitData : MutableLiveData<Habit> by lazy {
        MutableLiveData<Habit>()
    }

    val habitDataSaved : MutableLiveData<Long> by lazy {
        MutableLiveData<Long>()
    }

    lateinit var databaseManager : DatabaseManager
    lateinit var habitInfoManager : HabitInfoManager
    val iconManager = IconManager()

    var habitName = ""
    var priorityValue = 0
    var weekDaysSelectionModel = WeekDaysSelectionModel()
    var selectedIconModel : IconModel? = null


    fun initialize(context: Context, id : Long) {
        if(initialized) return

        databaseManager = DatabaseManager(context)
        habitInfoManager = HabitInfoManager(context)

        habitId = id

        if(isEditingExistingHabit()) {
            loading = true
            viewModelScope.launch {
                habitData.value = getHabit()
            }
        }

        iconManager.loadIcons(context)

        initialized = true
    }

    fun saveHabit(context: Context) {
        if(habitName.isEmpty()) {
            Toast.makeText(context, context.getString(R.string.error_name_empty), Toast.LENGTH_LONG).show()
            return
        }

        val habit : Habit
        if(isEditingExistingHabit()) {
            habit = habitData.value as Habit
        } else {
            habit = Habit()
        }

        habit.name = habitName

        val currentTime = System.currentTimeMillis()
        habit.creationDate = currentTime
        habit.modificationDate = currentTime

        habit.taskRecurrence = CalendarUtil.getRRuleFromWeekDaysSelectionModel(context, weekDaysSelectionModel)

        val selectedIconModel = selectedIconModel
        if(selectedIconModel == null) {
            habit.iconKey = null
        } else {
            habit.iconKey = selectedIconModel.key
        }

        habit.priority = priorityValue

        viewModelScope.launch {
            if(isEditingExistingHabit()) {
                updateHabit(habit)
                habitDataSaved.value = habitId
            } else {
                val id = insertHabit(habit)
                habitDataSaved.value = id
            }
        }
    }

    fun getRecurrenceHeader(context: Context) : String {
        return habitInfoManager.getRecurrenceHeader(context, weekDaysSelectionModel)
    }

    private suspend fun insertHabit(habit: Habit) : Long {
        return databaseManager.habitRepository.createHabit(habit)
    }

    private suspend fun updateHabit(habit: Habit) : Boolean {
        val rows = databaseManager.habitRepository.updateHabit(habit)
        return rows > 0
    }

    fun isEditingExistingHabit() : Boolean {
        return habitId >= 0
    }

    private suspend fun getHabit() : Habit? {
        return databaseManager.habitRepository.getHabitById(habitId)
    }
}