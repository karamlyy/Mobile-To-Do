package com.karamlyy.to_do

import android.net.Uri
import android.provider.Settings.Global.getString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class TaskAdapter(
    private val tasks: List<Task>,
    private val onEditTaskClick: (Task) -> Unit,
    private val onDeleteTaskClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task)
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.task_title)
        private val description: TextView = itemView.findViewById(R.id.task_description)
        private val editIcon: ImageView = itemView.findViewById(R.id.edit_task_icon)
        private val deleteIcon: ImageView = itemView.findViewById(R.id.delete_task_icon)
        private val addedTime: TextView = itemView.findViewById(R.id.addedTime)
        private val isImportant : TextView = itemView.findViewById(R.id.importantTask)
        private val image: ImageView = itemView.findViewById(R.id.task_image)

        fun bind(task: Task) {
            title.text = task.title
            description.text = task.description
            addedTime.text = task.addedTime
            if (task.imageUri != null) {
                Glide.with(itemView)
                    .load(Uri.parse(task.imageUri))
                    .into(image)
                image.visibility = View.VISIBLE
            } else {
                image.visibility = View.GONE
            }
            if (task.isImportant) {
                itemView.background = ContextCompat.getDrawable(itemView.context, R.drawable.important_note_background)
                isImportant.text = itemView.context.getString(R.string.label_important)
            } else {
                itemView.background = ContextCompat.getDrawable(itemView.context, R.drawable.ordinary_task_background)
                isImportant.setText("")
            }

            editIcon.setOnClickListener {
                onEditTaskClick(task)
            }
            deleteIcon.setOnClickListener {
                onDeleteTaskClick(task)
            }
        }
    }
}

