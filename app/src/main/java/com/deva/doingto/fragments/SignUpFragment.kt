package com.deva.doingto.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.deva.doingto.R
import com.deva.doingto.databinding.FragmentSignUpBinding
import com.google.firebase.auth.FirebaseAuth

class SignUpFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController
    private lateinit var binding: FragmentSignUpBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)

        binding.textViewSignIn.setOnClickListener {
            navController.navigate(R.id.action_signUpFragment_to_signInFragment)
        }

        binding.nextBtn.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val pass = binding.passEt.text.toString()
            val verifypass = binding.verifyPassEt.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty() && verifypass.isNotEmpty()) {
                if (pass == verifypass) {
                    registerUser(email, pass)
                } else {
                    Toast.makeText(context, "Password does not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Empty Fields Are not Allowed!!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser(email: String, pass: String) {
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Registered Successfully", Toast.LENGTH_SHORT).show()
                navController.navigate(R.id.action_signUpFragment_to_homeFragment)
            } else {
                Toast.makeText(context, it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun init(view: View) {
        auth = FirebaseAuth.getInstance()
        navController = Navigation.findNavController(view)
    }
}