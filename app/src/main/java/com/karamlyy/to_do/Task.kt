package com.karamlyy.to_do

data class Task(
    val id: Int,
    var title: String,
    var description: String,
    val addedTime: String,
    var isImportant: Boolean = false,
    var imageUri: String? = null

)