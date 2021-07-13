package com.example.orderfoodapp.fragments

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.Intent.getIntent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.orderfoodappforenterprise.NewProvider
import com.example.orderfoodappforenterprise.R
import com.example.orderfoodappforenterprise.activities.ProfileActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_login.*


class LoginFragment : Fragment() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var fusedLocationProvider: FusedLocationProviderClient


    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onResume() {
        super.onResume()
        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(context as Activity)
        requestPermission()

        firebaseAuth = FirebaseAuth.getInstance()

        mAuth = Firebase.auth
        val user = mAuth.currentUser

        if(user != null && user.isEmailVerified) {
            val intent = Intent(activity, ProfileActivity::class.java)
            startActivity(intent)
        }

        val emailPassed = SignUpFragment.KotlinConstantClass.COMPANION_OBJECT_EMAIL
        val passwordPassed = SignUpFragment.KotlinConstantClass.COMPANION_OBJECT_PASSWORD

        if(emailPassed.isNotEmpty() && passwordPassed.isNotEmpty()) {
            email_editText.setText(emailPassed)
            password_editText.setText(passwordPassed)

            //reset passed data
            SignUpFragment.KotlinConstantClass.COMPANION_OBJECT_EMAIL = ""
            SignUpFragment.KotlinConstantClass.COMPANION_OBJECT_PASSWORD = ""
        }


        //init loading dialog
        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_loading_login)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        login_button.setOnClickListener{
            dialog.show()
            loginUser()
        }

    }
    private fun loginUser() {
        val email: String = email_editText.text.toString()
        val password: String = password_editText.text.toString()
        when {
            TextUtils.isEmpty(email) -> {
                email_editText.error = "Email can't be empty"
                email_editText.requestFocus()
            }
            TextUtils.isEmpty(password) -> {
                password_editText.error = "Password can't be empty"
                password_editText.requestFocus()
            }
            else -> {
                mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
//                            val user = mAuth.currentUser
//                            if(user?.isEmailVerified == false) {
//                                user.sendEmailVerification()
//
//                                if(dialog.isShowing) {
//                                    dialog.dismiss()
//                                }
//
//                                Toast.makeText(requireActivity(), "Please check mail and verify your account!", Toast.LENGTH_LONG).show()
//                            }
//                            else {
                                //set delay for smooth animation
                                val handler = Handler()
                                handler.postDelayed({
                                    if(dialog.isShowing) {
                                        dialog.dismiss()
                                    }

                                    // Sign in success, update UI with the signed-in user's information
                                    Toast.makeText(requireActivity(), "User logged in successfully", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(requireActivity(), ProfileActivity::class.java)
                                    startActivity(intent)
                                }, 2500)
//                            }
                        } else {
                            if(dialog.isShowing) {
                                dialog.dismiss()
                            }
                            Toast.makeText(requireActivity(), "Login Error: " + task.exception, Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    private fun requestPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
            return
        }
    }


    private fun createProviderData(email: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Provider")
        val newCustomer = NewProvider(
            "",
            "",
            "",
            email,
            "",
            "",
            0
        )
        val key = dbRef.push().key.toString()
        dbRef.child(key).setValue(newCustomer)
    }

    private fun checkAccountExist(email: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Provider")
        val query = dbRef.orderByChild("email").equalTo(email)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    //set delay for smooth animation
                    val handler = Handler()
                    handler.postDelayed({
                        if(dialog.isShowing) {
                            dialog.dismiss()
                        }

                        Toast.makeText(requireActivity(), "User logged in successfully", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(requireContext(), ProfileActivity::class.java))
                    }, 2500)
                }
                else {
                    //set delay for smooth animation
                    val handler = Handler()
                    handler.postDelayed({
                        createProviderData(email)

                        if(dialog.isShowing) {
                            dialog.dismiss()
                        }

                        Toast.makeText(requireActivity(), "User logged in successfully", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(requireContext(), ProfileActivity::class.java))
                    }, 2500)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}