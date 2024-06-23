package com.example.hawkeyeapp.fragmentos

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hawkeyeapp.R
import com.example.hawkeyeapp.databinding.FragmentLoginBinding
import com.google.android.material.button.MaterialButton
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var sendOTPBtn: MaterialButton
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        sendOTPBtn = binding.buttonIngresar
        sendOTPBtn.setOnClickListener {
            val number = binding.editTextNum.text?.trim().toString()
            if (number.isNotEmpty()) {
                if (number.length == 9) {
                    val phoneNumber = "+51$number"
                    val options = PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(requireActivity())
                        .setCallbacks(callbacks)
                        .build()
                    PhoneAuthProvider.verifyPhoneNumber(options)
                } else {
                    Toast.makeText(context, "Please enter a correct number", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Please enter a number", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            if (e is FirebaseAuthInvalidCredentialsException) {
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
            } else if (e is FirebaseTooManyRequestsException) {
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
            }
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            val args = Bundle().apply {
                putString("OTP", verificationId)
                putString("phoneNumber", binding.editTextNum.text.toString())
            }
            Log.d("TAG", "Navigating to ConfirmaFragment")
            findNavController().navigate(R.id.action_loginFragment_to_confirmFragment, args)
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Authenticated Successfully", Toast.LENGTH_SHORT).show()
                    navigateToConfirmFragment()
                } else {
                    Log.d("TAG", "signInWithPhoneAuthCredential: ${task.exception.toString()}")
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Log.d("TAG", "Invalid credentials")
                    }
                }
            }
    }

    private fun navigateToConfirmFragment() {
        Log.d("TAG", "Navigating to ConfirmaFragment")
        findNavController().navigate(R.id.action_loginFragment_to_confirmFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
