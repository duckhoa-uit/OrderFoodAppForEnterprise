package com.example.orderfoodappforenterprise

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.orderfoodappforenterprise.ProfileActivity
import com.example.orderfoodappforenterprise.R
import com.example.orderfoodappforenterprise.SignUpActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = Firebase.auth


        val bundle = intent.extras
        if(bundle != null) {
            //if bundle passed from Sign up
            val newEmail = intent.getStringExtra("email").toString()
            val newPassword = intent.getStringExtra("password").toString()
            if(newEmail != "null" && newPassword != "null") {
                email_editText.setText(newEmail)
                password_editText.setText(newPassword)
            }
        }

        login_button.setOnClickListener(){
            loginUser()
        }

        signup_textView.setOnClickListener(){
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun loginUser() {
        val email: String = email_editText.text.toString()
        val password: String = password_editText.text.toString()
        if (TextUtils.isEmpty(email)) {
            email_editText.error = "Email can't be empty"
            email_editText.requestFocus()
        } else if (TextUtils.isEmpty(password)) {
            password_editText.error = "Password can't be empty"
            password_editText.requestFocus()
        } else {
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        //check if this provider exist in database of provider or not
                        checkProvider(email)
                    } else {
                        Toast.makeText(this, "Login Error: " + task.exception, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
        }
    }

    private fun checkProvider(email: String) {
        var isValid = false
        val dbRef = FirebaseDatabase.getInstance().getReference("Provider")
        dbRef.get().addOnSuccessListener {
            for(data in it.children) {
                if(data.child("email").value as String == email) {
                    isValid = true
                    break
                }
            }

            //handle when checked in database
            if(isValid) {
                Toast.makeText(this, "User logged in successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, ProfileActivity::class.java))
            }
            else {
                Toast.makeText(this, "Invalid user: ", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private suspend fun loadProviderId(providerEmail: String): String  = coroutineScope{
        var providerId = ""
        //get providerId
        val dbRef = FirebaseDatabase.getInstance().getReference("Provider").orderByChild("email").equalTo(providerEmail)
        dbRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(childBranch in snapshot.children){
                    println("childBranch.key.toString(): "+ childBranch.key.toString())
                    MainActivity.KotlinConstantClass.PROVIDER_ID = childBranch.key.toString()
                    providerId = childBranch.key.toString()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        return@coroutineScope providerId
    }

    private suspend fun loadDishes() = coroutineScope{
        val dbRef = FirebaseDatabase.getInstance().getReference("Product")

        dbRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                MainActivity.KotlinConstantClass.DISHES_ID.removeAll(MainActivity.KotlinConstantClass.DISHES_ID)

                for(childBranch in snapshot.children){
                    if (childBranch.child("provider").value.toString() == MainActivity.KotlinConstantClass.PROVIDER_ID){
                        MainActivity.KotlinConstantClass.DISHES_ID.add(
                            childBranch.child("id").value.toString()
                        )
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}