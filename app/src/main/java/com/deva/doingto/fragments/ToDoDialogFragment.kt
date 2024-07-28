package com.deva.doingto.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.deva.doingto.databinding.FragmentToDoDialogBinding
import com.deva.doingto.model.ToDoData
import com.google.android.material.textfield.TextInputEditText

class ToDoDialogFragment : DialogFragment(), GetLocationFragment.LocationUpdateListener {
    private lateinit var binding: FragmentToDoDialogBinding
    private var listener: OnDialogNextBtnClickListener? = null
    private var toDoData: ToDoData? = null

    fun setListener(listener: OnDialogNextBtnClickListener) {
        this.listener = listener
    }

    companion object {
        const val TAG = "DialogFragment"
        @JvmStatic
        fun newInstance(taskId: String, task: String, latitude: String = "", longitude: String = "") =
            ToDoDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("taskId", taskId)
                    putString("task", task)
                    putString("latitude", latitude)
                    putString("longitude", longitude)
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
                false,
                arguments?.getString("latitude").toString(),
                arguments?.getString("longitude").toString()
            )
            binding.todoEt.setText(toDoData?.task)
            binding.latitudeTextView.text = toDoData?.latitude
            binding.longitudeTextView.text = toDoData?.longitude
        }

        binding.addLocationButton.setOnClickListener {
            // val getLocation = GetLocationFragment()
            // getLocation.setLocationListener(this) // Set the listener
            // getLocation.show(childFragmentManager, GetLocationFragment.TAG)
            val latitude = binding.latitudeTextView.text.toString().toDoubleOrNull()
            val longitude = binding.longitudeTextView.text.toString().toDoubleOrNull()
            val getLocation = GetLocationFragment.newInstance(latitude, longitude)
            getLocation.setLocationListener(this) // Set the listener
            getLocation.show(childFragmentManager, GetLocationFragment.TAG)
        }

        binding.todoClose.setOnClickListener {
            dismiss()
        }

        binding.todoNextBtn.setOnClickListener {
            val todoTask = binding.todoEt.text.toString()
            val latitude = binding.latitudeTextView.text.toString()
            val longitude = binding.longitudeTextView.text.toString()

            if (todoTask.isNotEmpty()) {
                if (toDoData == null) {
                    listener?.saveTask(todoTask, false, latitude, longitude, binding.todoEt)
                } else {
                    toDoData!!.task = todoTask
                    toDoData!!.latitude = latitude
                    toDoData!!.longitude = longitude
                    listener?.updateTask(toDoData!!, binding.todoEt)
                }
            } else {
                Toast.makeText(context, "Task cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onLocationReceived(latitude: String, longitude: String) {
        binding.latitudeTextView.text = latitude
        binding.longitudeTextView.text = longitude
    }

    interface OnDialogNextBtnClickListener {
        fun saveTask(todoTask: String, isDone: Boolean, latitude: String, longitude: String, todoEdit: TextInputEditText)
        fun updateTask(toDoData: ToDoData, todoEdit: TextInputEditText)
    }
}