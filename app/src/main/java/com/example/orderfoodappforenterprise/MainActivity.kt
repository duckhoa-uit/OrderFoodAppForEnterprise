package com.example.orderfoodappforenterprise

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.orderfoodappforenterprise.LoginActivity
import com.example.orderfoodappforenterprise.model.Dish
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth

    class KotlinConstantClass {
        companion object {
            var PROVIDER_ID = ""
            var DISHES_ID = ArrayList<String>()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        mAuth = Firebase.auth
        val user = mAuth.currentUser

        startActivity(Intent(this, LoginActivity::class.java))
    }

    private suspend fun loadProviderId(providerEmail: String): String  = coroutineScope{
        var providerId = ""
        //get providerId
        val dbRef = FirebaseDatabase.getInstance().getReference("Provider").orderByChild("email").equalTo(providerEmail)
        dbRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(childBranch in snapshot.children){
                    println("childBranch.key.toString(): "+ childBranch.key.toString())
                    KotlinConstantClass.PROVIDER_ID = childBranch.key.toString()
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
                KotlinConstantClass.DISHES_ID.removeAll(KotlinConstantClass.DISHES_ID)

                for(childBranch in snapshot.children){
                    if (childBranch.child("provider").value.toString() == KotlinConstantClass.PROVIDER_ID){
                        KotlinConstantClass.DISHES_ID.add(
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

