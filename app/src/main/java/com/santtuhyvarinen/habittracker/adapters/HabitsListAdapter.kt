package com.santtuhyvarinen.habittracker.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.santtuhyvarinen.habittracker.R
import com.santtuhyvarinen.habittracker.managers.IconManager
import com.santtuhyvarinen.habittracker.models.Habit

class HabitsListAdapter(private var context: Context) : RecyclerView.Adapter<HabitsListAdapter.ViewHolder>() {

    val iconManager = IconManager()
    init {
        iconManager.loadIcons(context)
    }

    var data : List<Habit> = ArrayList()

    var habitClickedListener : HabitClickedListener? = null
    interface HabitClickedListener {
        fun habitClicked(habit: Habit)
    }

    class ViewHolder(var layout : View) : RecyclerView.ViewHolder(layout) {
        val titleTextView : TextView = layout.findViewById(R.id.title)
        val iconImageView : ImageView = layout.findViewById(R.id.icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_habit, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val habitModel = data[position]
        holder.titleTextView.text = habitModel.name

        val iconKey = habitModel.iconKey
        if(iconKey != null)
        holder.iconImageView.setImageDrawable(iconManager.getIconByKey(iconKey))

        holder.iconImageView.imageTintList = ColorStateList.valueOf(Color.BLACK)

        holder.layout.setOnClickListener {
            habitClickedListener?.habitClicked(habitModel)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

}