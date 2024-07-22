package com.adnan.todoproject.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.adnan.todoproject.R
import com.adnan.todoproject.adapter.TaskAdapter
import com.adnan.todoproject.databinding.FragmentHomeBinding
import com.adnan.todoproject.model.ToDoData
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment(), ToDoDialogFragment.OnDialogNextBtnClickListener, TaskAdapter.TaskAdapterInterface {
    private val TAG = "HomeFragment"
    private lateinit var binding: FragmentHomeBinding
    private lateinit var database: DatabaseReference
    private var frag: ToDoDialogFragment? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var authId: String
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var toDoItemList: MutableList<ToDoData>
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        init()
        getTaskFromFirebase()

        binding.addTaskBtn.setOnClickListener {
            if (frag != null) {
                childFragmentManager.beginTransaction().remove(frag!!).commit()
            }
            frag = ToDoDialogFragment()
            frag!!.setListener(this)
            frag!!.show(childFragmentManager, ToDoDialogFragment.TAG)
        }

        binding.logoutBtn.setOnClickListener {
            auth.signOut()
            Toast.makeText(context, "Logged Out Successfully", Toast.LENGTH_SHORT).show()
            navController.navigate(R.id.action_homeFragment_to_splashFragment)
        }
    }

    private fun getTaskFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                toDoItemList.clear()
                for (taskSnapshot in snapshot.children) {
                    val taskId = taskSnapshot.key ?: continue
                    val task = taskSnapshot.child("task").value.toString()
                    val isDone = taskSnapshot.child("isDone").value as? Boolean ?: false
                    val todoTask = ToDoData(taskId, task, isDone)
                    toDoItemList.add(todoTask)
                }
                Log.d(TAG, "onDataChange: $toDoItemList")
                taskAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun init() {
        auth = FirebaseAuth.getInstance()
        authId = auth.currentUser?.uid ?: return
        val databaseUrl = "https://todoproject-c099d-default-rtdb.asia-southeast1.firebasedatabase.app/"
        database = Firebase.database(databaseUrl).reference.child("Tasks").child(authId)
        binding.mainRecyclerView.setHasFixedSize(true)
        binding.mainRecyclerView.layoutManager = LinearLayoutManager(context)
        toDoItemList = mutableListOf()
        taskAdapter = TaskAdapter(toDoItemList)
        taskAdapter.setListener(this)
        binding.mainRecyclerView.adapter = taskAdapter
    }

    override fun saveTask(task: String, isDone: Boolean, todoEt: TextInputEditText) {
        val key = database.push().key
        if (key == null) {
            Log.w(TAG, "Couldn't get push key for tasks")
            return
        }
        val toDoTask = ToDoData(key, task, isDone)
        database.child(key).setValue(toDoTask).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Task Added Successfully", Toast.LENGTH_SHORT).show()
                todoEt.text = null
            } else {
                Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
        frag?.dismiss()
    }

    override fun updateTask(toDoData: ToDoData, todoEt: TextInputEditText) {
        val map = mapOf(
            "task" to toDoData.task,
            "isDone" to toDoData.isDone
        )
        database.child(toDoData.taskId).updateChildren(map).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Task Updated Successfully", Toast.LENGTH_SHORT).show()
                todoEt.text = null
            } else {
                Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
        frag?.dismiss()
    }

    override fun onEditItemClicked(toDoData: ToDoData, position: Int) {
        if (frag != null) {
            childFragmentManager.beginTransaction().remove(frag!!).commit()
        }
        frag = ToDoDialogFragment.newInstance(toDoData.taskId, toDoData.task)
        frag!!.setListener(this)
        frag!!.show(childFragmentManager, ToDoDialogFragment.TAG)
    }

    override fun onDeleteItemClicked(toDoData: ToDoData, position: Int) {
        database.child(toDoData.taskId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onTaskStatusChanged(toDoData: ToDoData, position: Int) {
        val map = mapOf("isDone" to toDoData.isDone)
        database.child(toDoData.taskId).updateChildren(map).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Task Status Updated Successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}