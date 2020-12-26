package com.santtuhyvarinen.habittracker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.santtuhyvarinen.habittracker.R
import com.santtuhyvarinen.habittracker.adapters.IconSelectionAdapter
import com.santtuhyvarinen.habittracker.models.Habit
import com.santtuhyvarinen.habittracker.models.IconModel
import com.santtuhyvarinen.habittracker.viewmodels.HabitFormViewModel
import com.santtuhyvarinen.habittracker.views.WeekDayPickerView
import kotlinx.android.synthetic.main.fragment_habit_form.*

class HabitFormFragment : Fragment() {

    private val args : HabitFormFragmentArgs by navArgs()

    private lateinit var habitFormViewModel : HabitFormViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_habit_form, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        habitFormViewModel = ViewModelProvider(this).get(HabitFormViewModel::class.java)
        habitFormViewModel.initialize(requireContext(), args.habitId)

        //If editing a habit, load the existing habit values to correct fields
        val habitObserver = Observer<Habit> { habit ->
            if(habit != null) {
                habitFormViewModel.loading = false
                updateHabitValues(habit)
                updateLoadingProgressVisibility()
            }
        }
        habitFormViewModel.habitData.observe(viewLifecycleOwner, habitObserver)

        //Navigate to HabitView after saving the habit
        val saveHabitObserver = Observer<Long> { id ->
            if(id >= 0) {
                val action = HabitFormFragmentDirections.actionToHabitViewFragment(id)
                findNavController().navigate(action)
            }
        }
        habitFormViewModel.habitDataSaved.observe(viewLifecycleOwner, saveHabitObserver)

        habitNameEditText.requestFocus()
        habitNameEditText.setOnEditorActionListener { _, actionId, keyEvent ->
            if(actionId == EditorInfo.IME_ACTION_DONE) {
                habitNameEditText.clearFocus()
            }
            return@setOnEditorActionListener false
        }

        //WeekDayPicker
        weekDayPicker.weekDaySelectedListener = object : WeekDayPickerView.WeekDaySelectedListener {
            override fun weekDaySelected(index: Int, selected: Boolean) {
                habitFormViewModel.selectedWeekDayButtons[index] = selected
                updateWeekDayHeader()
            }
        }

        for(i in habitFormViewModel.selectedWeekDayButtons.indices) {
            weekDayPicker.setWeekDayButtonSelected(i, habitFormViewModel.selectedWeekDayButtons[i])
        }

        //Priority SeekBar
        habitPrioritySeekBar.max = habitFormViewModel.habitInfoManager.getMaxPriorityLevel()
        habitPrioritySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekbar: SeekBar, progress: Int, fromUser: Boolean) {
                habitFormViewModel.priorityValue = progress
                updatePriorityHeader()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

        //IconPicker
        iconPickerView.iconManager = habitFormViewModel.iconManager
        iconPickerView.setIconSelectedListener(object : IconSelectionAdapter.IconSelectedListener {
            override fun iconSelected(iconModel: IconModel?) {
                habitFormViewModel.selectedIconModel = iconModel
            }
        })
        iconPickerView.setSelectedIcon(habitFormViewModel.selectedIconModel)

        //Save button
        saveHabitButton.text = if(habitFormViewModel.isEditingExistingHabit()) getString(R.string.save_changes) else getString(R.string.create_habit)
        saveHabitButton.setOnClickListener {
            habitFormViewModel.habitName = habitNameEditText.text.toString()

            habitFormViewModel.saveHabit(requireContext())
        }

        updatePriorityHeader()
        updateWeekDayHeader()
        updateLoadingProgressVisibility()
    }

    private fun updateLoadingProgressVisibility() {
        progress.apply {
            visibility = if(habitFormViewModel.loading) View.VISIBLE else View.GONE
        }
        scrollView.apply {
            visibility = if(habitFormViewModel.loading) View.GONE else View.VISIBLE
        }
        saveHabitButton.apply {
            visibility = if(habitFormViewModel.loading) View.GONE else View.VISIBLE
        }
    }

    private fun updatePriorityHeader() {
        val currentPriority = habitFormViewModel.habitInfoManager.getCurrentPriorityLevelText(habitFormViewModel.priorityValue)
        habitPriorityHeader.text = getString(R.string.habit_priority_header, currentPriority)
    }

    private fun updateWeekDayHeader() {
        if(habitFormViewModel.isEveryDaySelectedOrNotSelected()) {
            weekDayPickerHeader.text = getString(R.string.habit_repeat_every_day)
        } else {
            //Show selected week days in to the header
            weekDayPickerHeader.text = getString(R.string.habit_repeat_days, habitFormViewModel.getWeekDaysSelectedText(requireContext()))
        }
    }

    private fun updateHabitValues(habit: Habit) {
        habitNameEditText.setText(habit.name)
        habitPrioritySeekBar.progress = habit.priority

        val selectedModel = habitFormViewModel.iconManager.getIconModelByKey(habit.iconKey)
        habitFormViewModel.selectedIconModel = selectedModel
        iconPickerView.setSelectedIcon(selectedModel)
    }
}