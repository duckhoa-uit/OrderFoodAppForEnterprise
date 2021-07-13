package com.example.orderfoodappforenterprise.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.example.orderfoodapp.fragments.LoginFragment
import com.example.orderfoodapp.fragments.SignUpFragment
import com.example.orderfoodappforenterprise.R
import com.example.orderfoodappforenterprise.adapter.LoginAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.coroutineScope

class MainActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var loginFragment: Fragment
    private lateinit var signupFragment: Fragment

    class KotlinConstantClass {
        companion object {
            var PROVIDER_ID = ""
            var DISHES_ID = ArrayList<String>()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        mAuth = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loginFragment = LoginFragment()
        signupFragment = SignUpFragment()

        val fragments = arrayListOf(loginFragment, signupFragment)
        val adapter = LoginAdapter(fragments, this)
        login_view_pager.adapter = adapter

        TabLayoutMediator(login_tab_layout, login_view_pager) { tab, position ->
            if (position == 0)
                tab.text = "Login"
            else
                tab.text = "Signup"
        }.attach()
    }

    override fun onStart() {
        super.onStart()
        val user = mAuth.currentUser
        Log.d("User", user.toString())
        if(user != null && user.isEmailVerified){
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
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

