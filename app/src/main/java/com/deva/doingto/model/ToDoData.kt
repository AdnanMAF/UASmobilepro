package com.deva.doingto.model

data class ToDoData(
    var taskId: String,
    var task: String,
    var isDone: Boolean,
    var latitude: String = "",
    var longitude: String = ""
)