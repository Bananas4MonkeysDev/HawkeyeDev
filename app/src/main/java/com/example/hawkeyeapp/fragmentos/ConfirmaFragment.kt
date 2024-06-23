package com.example.hawkeyeapp.fragmentos

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hawkeyeapp.databinding.FragmentConfirmaBinding
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.TimeUnit
import android.os.Handler
import android.os.Looper
import com.example.hawkeyeapp.R

class ConfirmaFragment : Fragment() {
    private var _binding: FragmentConfirmaBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var OTP: String
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private lateinit var phoneNumber: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfirmaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        arguments?.let {
            OTP = it.getString("OTP") ?: ""
            resendToken = it.getParcelable("resendToken")
            phoneNumber = it.getString("phoneNumber") ?: ""
        }

        addTextChangeListeners()
        resendOTPTvVisibility()

        binding.resendTextView.setOnClickListener {
            resendVerificationCode()
            resendOTPTvVisibility()
        }

        binding.buttonIngresar.setOnClickListener {
            val typedOTP = binding.otpEditText1.text.toString() + binding.otpEditText2.text.toString() +
                    binding.otpEditText3.text.toString() + binding.otpEditText4.text.toString() +
                    binding.otpEditText5.text.toString() + binding.otpEditText6.text.toString()

            if (typedOTP.isNotEmpty() && typedOTP.length == 6) {
                val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(OTP, typedOTP)
                signInWithPhoneAuthCredential(credential)
            } else {
                Toast.makeText(context, "Please Enter Correct OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addTextChangeListeners() {
        val editTexts = listOf(binding.otpEditText1, binding.otpEditText2, binding.otpEditText3,
            binding.otpEditText4, binding.otpEditText5, binding.otpEditText6)

        editTexts.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (s.toString().length == 1 && index < editTexts.size - 1) {
                        editTexts[index + 1].requestFocus()
                    } else if (s.toString().isEmpty() && index > 0) {
                        editTexts[index - 1].requestFocus()
                    }
                }
            })
        }
    }

    private fun resendOTPTvVisibility() {
        binding.resendTextView.visibility = View.INVISIBLE
        binding.resendTextView.isEnabled = false

        Handler(Looper.getMainLooper()).postDelayed({
            binding.resendTextView.visibility = View.VISIBLE
            binding.resendTextView.isEnabled = true
        }, 6000)
    }

    private fun resendVerificationCode() {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(callbacks)
            .apply {
                resendToken?.let { setForceResendingToken(it) }
            }
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            if (e is FirebaseAuthInvalidCredentialsException) {
                Log.d("TAG", "onVerificationFailed: ${e}")
            } else if (e is FirebaseTooManyRequestsException) {
                Log.d("TAG", "onVerificationFailed: ${e}")
            }
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            OTP = verificationId
            resendToken = token
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).addOnCompleteListener(requireActivity()) { task ->
            if (task.isSuccessful) {
                checkUserRegistration()
            } else {
                Log.d("TAG", "signInWithPhoneAuthCredential: ${task.exception}")
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    Log.d("TAG", "Invalid credentials")
                }
            }
        }
    }

    private fun checkUserRegistration() {
        val uid = auth.currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance().getReference("Pasajeros")
        database.child(uid).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                navigateToHomeFragment()
            } else {
                navigateToCompleteRegistrationFragment()
            }
        }.addOnFailureListener {
            Log.d("TAG", "Error getting data: ${it.message}")
        }
    }

    private fun navigateToHomeFragment() {
        Log.d("TAG", "Navigating to HomeFragment")
        findNavController().navigate(R.id.homeFragment)
    }

    private fun navigateToCompleteRegistrationFragment() {
        Log.d("TAG", "Navigating to CompletarRegistroFragment")
        findNavController().navigate(R.id.completarRegistroFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
