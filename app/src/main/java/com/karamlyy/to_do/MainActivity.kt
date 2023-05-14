package com.karamlyy.to_do

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.karamlyy.to_do.databinding.ActivityMainBinding
import com.karamlyy.to_do.databinding.AddTaskDialogBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val tasks = mutableListOf<Task>()
    private lateinit var taskAdapter: TaskAdapter
    private var selectedImageUri: Uri? = null

    companion object {
        private const val IMAGE_REQUEST_CODE = 100
    }

    @RequiresApi(Build.VERSION_CODES.O)
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


    @RequiresApi(Build.VERSION_CODES.O)
    private fun showAddTaskDialog() {
        val dialogBinding = AddTaskDialogBinding.inflate(layoutInflater)

        dialogBinding.selectImageButton.setOnClickListener {
            selectImageFromGallery()
        }
        AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setTitle("${getString(R.string.label_add_task_button)}")
            .setPositiveButton("${getString(R.string.label_add)}") { _, _ ->
                val taskTitle = dialogBinding.titleTaskInput.text.toString()
                val taskDescription = dialogBinding.taskDescriptionInput.text.toString()
                val isImportant = dialogBinding.importantCheckBox.isChecked

                if (taskTitle.isNotEmpty()) {
                    val currentDateTime = LocalDateTime.now()
                    val addedTime =
                        currentDateTime.format(DateTimeFormatter.ofPattern("dd-MMMM-yyyy  HH:mm"))

                    val newTask = Task(tasks.size + 1, taskTitle, taskDescription, addedTime, isImportant, selectedImageUri?.toString())

                    tasks.add(newTask)
                    taskAdapter.notifyItemInserted(tasks.size - 1)
                    Toast.makeText(
                        this,
                        "${getString(R.string.label_task_added_toast)}",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateEmptyTasksVisibility()

                    // Reset selectedImageUri
                    selectedImageUri = null

                } else {
                    Toast.makeText(
                        this,
                        "${getString(R.string.label_task_is_empty)}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("${getString(R.string.label_cancel)}", null)
            .show()
    }

    private fun showEditTaskDialog(task: Task) {
        val dialogBinding = AddTaskDialogBinding.inflate(layoutInflater)
        dialogBinding.titleTaskInput.setText(task.title)
        dialogBinding.taskDescriptionInput.setText(task.description)
        dialogBinding.importantCheckBox.isChecked = task.isImportant

        // Set selectedImageUri to the current task's image if it exists
        selectedImageUri = if (task.imageUri != null) Uri.parse(task.imageUri) else null

        dialogBinding.selectImageButton.setOnClickListener {
            selectImageFromGallery()
        }
        AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setTitle("${getString(R.string.label_edit_task)}")
            .setPositiveButton("${getString(R.string.label_update)}") { _, _ ->
                val taskTitle = dialogBinding.titleTaskInput.text.toString()
                val taskDescription = dialogBinding.taskDescriptionInput.text.toString()
                val isImportant = dialogBinding.importantCheckBox.isChecked

                if (taskTitle.isNotEmpty()) {
                    task.title = taskTitle
                    task.description = taskDescription

                    task.isImportant = isImportant
                    task.imageUri = selectedImageUri?.toString()

                    taskAdapter.notifyDataSetChanged()
                    Toast.makeText(
                        this,
                        "${getString(R.string.label_task_updated)}",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Reset selectedImageUri
                    selectedImageUri = null

                } else {
                    Toast.makeText(
                        this,
                        "${getString(R.string.label_task_is_empty)}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("${getString(R.string.label_cancel)}", null)
            .show()
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data
            // Handle the image URI as needed
        }
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
        val position = tasks.indexOf(task)
        tasks.remove(task)
        taskAdapter.notifyItemRemoved(position)
        updateEmptyTasksVisibility()

        val snackbar = Snackbar.make(
            binding.coordinatorLayout,
            "${getString(R.string.label_task_deleted)}",
            Snackbar.LENGTH_SHORT
        )
        snackbar.setAction("${getString(R.string.label_undo)}") {
            tasks.add(position, task)
            taskAdapter.notifyItemInserted(position)
            updateEmptyTasksVisibility()
        }
        snackbar.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                if (event != DISMISS_EVENT_ACTION) {
                    // The Snackbar was dismissed without pressing the action button
                    // Remove the task from the list permanently
                    saveTasks()
                }
            }
        })
        snackbar.show()
    }

    override fun onStop() {
        super.onStop()
        saveTasks()
    }
}


