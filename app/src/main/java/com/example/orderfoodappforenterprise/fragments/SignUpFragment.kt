package com.example.orderfoodapp.fragments

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.orderfoodappforenterprise.R
import com.example.orderfoodappforenterprise.model.NewProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_signup.address_editText
import kotlinx.android.synthetic.main.fragment_signup.confirmPassword_editText
import kotlinx.android.synthetic.main.fragment_signup.email_editText
import kotlinx.android.synthetic.main.fragment_signup.password_editText
import kotlinx.android.synthetic.main.fragment_signup.phoneNumber_editText
import kotlinx.android.synthetic.main.fragment_signup.shopName_editText
import kotlinx.android.synthetic.main.fragment_signup.signup_button
import java.util.*

class SignUpFragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth
    private var lat = 0.0
    private var lon = 0.0

    class KotlinConstantClass {
        companion object {
            var COMPANION_OBJECT_EMAIL = ""
            var COMPANION_OBJECT_PASSWORD = ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = Firebase.auth

        signup_button.setOnClickListener(){
            createProvider()
        }
    }
    private fun createProvider(){
        val email : String = email_editText.text.toString()
        val password : String = password_editText.text.toString()
        val confirmPassword : String = confirmPassword_editText.text.toString()
        val name = shopName_editText.text.toString()
        val phoneNum = phoneNumber_editText.text.toString()
        val address = address_editText.text.toString()
        when {
            TextUtils.isEmpty(email) -> {
                email_editText.error = "Email can't be empty"
                email_editText.requestFocus()
            }
            TextUtils.isEmpty(password) -> {
                password_editText.error = "Password can't be empty"
                password_editText.requestFocus()
            }
            TextUtils.isEmpty(confirmPassword) -> {
                confirmPassword_editText.error = "You must confirm password"
                confirmPassword_editText.requestFocus()
            }
            TextUtils.isEmpty(name) -> {
                email_editText.error = "Name can't be empty"
                email_editText.requestFocus()
            }
            TextUtils.isEmpty(phoneNum) -> {
                password_editText.error = "Phone number can't be empty"
                password_editText.requestFocus()
            }
            TextUtils.isEmpty(address) -> {
                confirmPassword_editText.error = "Address can't be empty"
                confirmPassword_editText.requestFocus()
            }
            password != confirmPassword -> {
                confirmPassword_editText.error = "Your Confirm is not correct. Please confirm again"
                confirmPassword_editText.requestFocus()
            }
            else -> {
                //check if address valid or not
//                convertLocation(address)
//                if(lat == 0.0 && lon == 0.0) {
//                    Toast.makeText(requireContext(), "Invalid address, try again!", Toast.LENGTH_LONG).show()
//                    return
//                }
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            //send email verify
                            Firebase.auth.currentUser?.sendEmailVerification()

                            // Sign in success, update UI with the signed-in user's information
                            createProviderData(email, name, phoneNum, address)

                            KotlinConstantClass.COMPANION_OBJECT_EMAIL = email
                            KotlinConstantClass.COMPANION_OBJECT_PASSWORD = password

                            Toast.makeText(requireActivity(), "User sign up successfully, please check mail to verify account!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireActivity(), "Sign Up Error: " + task.exception,Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    private fun createProviderData(email: String, name: String, phoneNum: String, address: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Provider")
        val newProvider = NewProvider (
            email,
            address,
            name,
            phoneNum,
            "0"
        )
        val key = dbRef.push().key.toString()
        dbRef.child(key).setValue(newProvider)
    }
    private fun convertLocation(myLocation: String) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses: List<Address> = geocoder.getFromLocationName(myLocation, 1)
            val address: Address = addresses[0]
            lat = address.latitude
            lon = address.longitude
        }
        catch (e: Exception) {
            Toast.makeText(requireContext(), "Issue with gps, try later!", Toast.LENGTH_LONG).show()
            Log.i("exception", e.printStackTrace().toString())
        }
    }
}