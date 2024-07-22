package com.adnan.todoproject.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.adnan.todoproject.databinding.FragmentToDoDialogBinding
import com.adnan.todoproject.model.ToDoData
import com.google.android.material.textfield.TextInputEditText

class ToDoDialogFragment : DialogFragment() {
    private lateinit var binding: FragmentToDoDialogBinding
    private var listener: OnDialogNextBtnClickListener? = null
    private var toDoData: ToDoData? = null

    fun setListener(listener: OnDialogNextBtnClickListener) {
        this.listener = listener
    }

    companion object {
        const val TAG = "DialogFragment"
        @JvmStatic
        fun newInstance(taskId: String, task: String) =
            ToDoDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("taskId", taskId)
                    putString("task", task)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentToDoDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments != null) {
            toDoData = ToDoData(
                arguments?.getString("taskId").toString(),
                arguments?.getString("task").toString(),
                false
            )
            binding.todoEt.setText(toDoData?.task)
        }

        binding.todoClose.setOnClickListener {
            dismiss()
        }

        binding.todoNextBtn.setOnClickListener {
            val todoTask = binding.todoEt.text.toString()

            if (todoTask.isNotEmpty()) {
                if (toDoData == null) {
                    listener?.saveTask(todoTask, false, binding.todoEt)
                } else {
                    toDoData!!.task = todoTask
                    listener?.updateTask(toDoData!!, binding.todoEt)
                }
            } else {
                Toast.makeText(context, "Task cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    interface OnDialogNextBtnClickListener {
        fun saveTask(todoTask: String, isDone: Boolean, todoEdit: TextInputEditText)
        fun updateTask(toDoData: ToDoData, todoEdit: TextInputEditText)
    }
}