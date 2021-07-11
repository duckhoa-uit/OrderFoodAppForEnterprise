package com.example.orderfoodappforenterprise

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.orderfoodappforenterprise.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        mAuth = Firebase.auth
        val user = mAuth.currentUser

        if(user == null){
            startActivity(Intent(this, LoginActivity::class.java))
        }
        else {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        //startActivity(Intent(this, LoginActivity::class.java))
    }
}

