package com.adnan.todoproject.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.adnan.todoproject.databinding.TodoItemBinding
import com.adnan.todoproject.model.ToDoData

class TaskAdapter(private val list: MutableList<ToDoData>) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {
    private val TAG = "TaskAdapter"
    private var listener: TaskAdapterInterface? = null

    fun setListener(listener: TaskAdapterInterface) {
        this.listener = listener
    }

    class TaskViewHolder(val binding: TodoItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = TodoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val toDoData = list[position]
        with(holder.binding) {
            todoTask.text = toDoData.task
            latitudeText.text = toDoData.latitude
            longitudeText.text = toDoData.longitude
            doneCB.isChecked = toDoData.isDone
            Log.d(TAG, "onBindViewHolder: $toDoData")

            editTask.setOnClickListener {
                listener?.onEditItemClicked(toDoData, position)
            }
            deleteTask.setOnClickListener {
                listener?.onDeleteItemClicked(toDoData, position)
            }
            doneCB.setOnCheckedChangeListener { _, isChecked ->
                toDoData.isDone = isChecked
                listener?.onTaskStatusChanged(toDoData, position)
            }
        }
    }

    override fun getItemCount(): Int = list.size

    interface TaskAdapterInterface {
        fun onEditItemClicked(toDoData: ToDoData, position: Int)
        fun onDeleteItemClicked(toDoData: ToDoData, position: Int)
        fun onTaskStatusChanged(toDoData: ToDoData, position: Int)
    }
}