package com.karamlyy.to_do

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.karamlyy.to_do.databinding.ActivityMainBinding
import com.karamlyy.to_do.databinding.AddTaskDialogBinding



class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val tasks = mutableListOf<Task>()
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        taskAdapter = TaskAdapter(tasks,
            { task ->
                showEditTaskDialog(task)
            }, { task ->
                deleteTask(task)
            })

        binding.recyclerView.adapter = taskAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        binding.addTaskButton.setOnClickListener {
            showAddTaskDialog()
        }
    }

    private fun showAddTaskDialog() {
        val dialogBinding = AddTaskDialogBinding.inflate(layoutInflater)

        AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setTitle("Add Task")
            .setPositiveButton("Add") { _, _ ->
                val taskTitle = dialogBinding.titleTaskInput.text.toString()
                val taskDescription = dialogBinding.taskDescriptionInput.text.toString()
                val taskTime = "${dialogBinding.taskTimePicker.hour}:${dialogBinding.taskTimePicker.minute}"

                if (taskTitle.isNotEmpty()) {
                    val newTask = Task(tasks.size + 1, taskTitle, taskDescription, taskTime)
                    tasks.add(newTask)
                    taskAdapter.notifyItemInserted(tasks.size - 1)
                    Toast.makeText(this, "Task added!", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(this, "Task title cannot be empty!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditTaskDialog(task: Task) {
        val dialogBinding = AddTaskDialogBinding.inflate(layoutInflater)
        dialogBinding.titleTaskInput.setText(task.title)
        dialogBinding.taskDescriptionInput.setText(task.description)
        dialogBinding.taskTimePicker.hour = task.time.split(":")[0].toInt()
        dialogBinding.taskTimePicker.minute = task.time.split(":")[1].toInt()

        AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setTitle("Edit Task")
            .setPositiveButton("Update") { _, _ ->
                val taskTitle = dialogBinding.titleTaskInput.text.toString()
                val taskDescription = dialogBinding.taskDescriptionInput.text.toString()
                val taskTime = "${dialogBinding.taskTimePicker.hour}:${dialogBinding.taskTimePicker.minute}"

                if (taskTitle.isNotEmpty()) {
                    task.title = taskTitle
                    task.description = taskDescription
                    task.time = taskTime
                    taskAdapter.notifyDataSetChanged()
                    Toast.makeText(this, "Task updated!", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(this, "Task title cannot be empty!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteTask(task: Task) {
        tasks.remove(task)
        taskAdapter.notifyDataSetChanged()
    }
}