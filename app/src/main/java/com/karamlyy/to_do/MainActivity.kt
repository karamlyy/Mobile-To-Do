package com.karamlyy.to_do

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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

        loadTasks()
        updateEmptyTasksVisibility()

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
    private fun updateEmptyTasksVisibility() {
        if (tasks.isEmpty()) {
            binding.emptyTasksTextView.visibility = View.VISIBLE
        } else {
            binding.emptyTasksTextView.visibility = View.GONE
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
                val formattedMinute = String.format("%02d", dialogBinding.taskTimePicker.minute)
                val taskTime = "${dialogBinding.taskTimePicker.hour}:$formattedMinute"

                if (taskTitle.isNotEmpty()) {
                    val newTask = Task(tasks.size + 1, taskTitle, taskDescription, taskTime)
                    tasks.add(newTask)
                    taskAdapter.notifyItemInserted(tasks.size - 1)
                    Toast.makeText(this, "Task added!", Toast.LENGTH_SHORT).show()
                    updateEmptyTasksVisibility()

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
    private fun getSharedPreferences(): SharedPreferences {
        return getSharedPreferences("com.karamlyy.to_do.Tasks", MODE_PRIVATE)
    }

    private fun saveTasks() {
        val sharedPreferences = getSharedPreferences()
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val tasksJson = gson.toJson(tasks)
        editor.putString("tasks", tasksJson)
        editor.apply()
    }

    private fun loadTasks() {
        val sharedPreferences = getSharedPreferences()
        val gson = Gson()
        val tasksJson = sharedPreferences.getString("tasks", null)

        if (tasksJson != null) {
            val type = object : TypeToken<List<Task>>() {}.type
            tasks.clear()
            tasks.addAll(gson.fromJson(tasksJson, type))
        }
    }

    private fun deleteTask(task: Task) {
        tasks.remove(task)
        taskAdapter.notifyDataSetChanged()
        updateEmptyTasksVisibility()


    }

    override fun onStop() {
        super.onStop()
        saveTasks()
    }
}


