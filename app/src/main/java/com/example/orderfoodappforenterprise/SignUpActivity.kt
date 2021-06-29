package com.example.orderfoodappforenterprise

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.example.orderfoodappforenterprise.model.NewProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.util.*

class SignUpActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private var lat = 0.0
    private var lon = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        mAuth = Firebase.auth

        signup_button.setOnClickListener(){
            createProvider()
        }
    }

    private fun createProvider() {
        val email = email_editText.text.toString()
        val password = password_editText.text.toString()
        val confirmPassword = confirmPassword_editText.text.toString()
        val name = shopName_editText.text.toString()
        val phoneNum = phoneNumber_editText.text.toString()
        val address = address_editText.text.toString()

        if(TextUtils.isEmpty(email)){
            email_editText.error = "Email can't be empty"
            email_editText.requestFocus()
        }
        else if(TextUtils.isEmpty(password)){
            password_editText.error = "Password can't be empty"
            password_editText.requestFocus()
        }
        else if(TextUtils.isEmpty(confirmPassword)){
            confirmPassword_editText.error = "You must confirm password"
            confirmPassword_editText.requestFocus()
        }
        else if(name.isEmpty()) {
            shopName_editText.error = "Name can't be empty"
            shopName_editText.requestFocus()
        }
        else if(phoneNum.isEmpty()) {
            phoneNumber_editText.error = "Phone number can't be empty"
            phoneNumber_editText.requestFocus()
        }
        else if(address.isEmpty()) {
            address_editText.error = "Address can't be empty"
            address_editText.requestFocus()
        }
        else if(password != confirmPassword){
            confirmPassword_editText.error = "Your Confirm is not correct. Please confirm again"
            confirmPassword_editText.requestFocus()
        }
        else{
            //check if address valid or not
            convertLocation(address)
            if(lat == 0.0 && lon == 0.0) {
                Toast.makeText(this, "Invalid address, try again!", Toast.LENGTH_LONG).show()
                return
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        //create provider data in database
                        createProviderData(email, name, phoneNum, address)
                        Toast.makeText(this, "User sign up successfully", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this,LoginActivity::class.java)
                        val bundle = Bundle()
                        bundle.putString("email", email)
                        bundle.putString("password", password)
                        intent.putExtras(bundle)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Sign Up Error: " + task.exception, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun createProviderData(email: String, name: String, phoneNum: String, address: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Provider")
        val newCustomer = NewProvider (
            email,
            address,
            name,
            phoneNum,
            "0"
        )
        val key = dbRef.push().key.toString()
        dbRef.child(key).setValue(newCustomer)
    }

    private fun convertLocation(myLocation: String) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses: List<Address> = geocoder.getFromLocationName(myLocation, 1)
            val address: Address = addresses[0]
            lat = address.latitude
            lon = address.longitude
        }
        catch (e: Exception) {
            Toast.makeText(this, "Issue with gps, try later!", Toast.LENGTH_LONG).show()
            Log.i("exception", e.printStackTrace().toString())
        }
    }


}