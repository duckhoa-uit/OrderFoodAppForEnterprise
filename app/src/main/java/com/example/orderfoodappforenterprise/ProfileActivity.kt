package com.example.orderfoodappforenterprise

import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.orderfoodappforenterprise.adapter.DishAdapter
import com.example.orderfoodappforenterprise.model.Dish
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_profile.profile_picture
import kotlinx.android.synthetic.main.nav_header.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.File



class ProfileActivity : AppCompatActivity() {
    private var providerKey = ""
    private lateinit var dishAdapter: DishAdapter
    lateinit var toggle: ActionBarDrawerToggle
    private var providerEmail = Firebase.auth.currentUser?.email.toString()
    private var providerId = ""
    private var dishesId = mutableListOf<String>()
    private lateinit var mAuth: FirebaseAuth



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        mAuth = Firebase.auth
        val user = mAuth.currentUser

        GlobalScope.launch {
            val loadProviderId = async { loadProviderId(user?.email!!) }
            MainActivity.KotlinConstantClass.PROVIDER_ID = loadProviderId.await()

            val loadDishes = async { loadDishes() }
            loadDishes.await()
        }

        dishAdapter = DishAdapter(mutableListOf())
        allFood_recyclerView.adapter = dishAdapter
        val layoutManager = LinearLayoutManager(this)
        allFood_recyclerView.layoutManager = layoutManager

        //Sidebar menu
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView_profile.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.add_food -> startActivity(Intent(this, AddFoodActivity::class.java))
                R.id.edit_profile -> startActivity(Intent(this, EditProfileActivity::class.java))
                R.id.inbox -> startActivity(Intent(this, ChatActivity::class.java))
                R.id.sign_out -> {
                    Firebase.auth.signOut()
                    val i = Intent(this, MainActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(i)
                    Toast.makeText(applicationContext, "Sign out", Toast.LENGTH_SHORT).show()
                }
                R.id.statistical -> {
                    val intent = Intent(Intent(this, AnalyzeActivity::class.java))
                    startActivity(intent)
                }
            }
            true
        }

        menu_button.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

//        back_button_nav.setOnClickListener {
//            drawerLayout.closeDrawer(GravityCompat.END)
//        }

        displayProvider()

        println(providerId)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun displayProvider() {
        val customerEmail = Firebase.auth.currentUser?.email.toString()
        email_textView.text = customerEmail

        val dbRef = FirebaseDatabase.getInstance().getReference("Provider")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(data in snapshot.children) {
                    if(data.child("email").value as String == customerEmail) {
                        providerKey = data.key.toString()
                        name_textView.text = data.child("name").value as String
                        address_textView.text = data.child("location").value as String
                        displayAllFood()
                        break
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProfileActivity, "Cannot load customer's data, please try later!", Toast.LENGTH_LONG).show()
            }

        })

        val imgName = customerEmail.replace(".", "_")
        val storageRef = FirebaseStorage.getInstance().getReference("avatar_image/$imgName.jpg")
        try {
            val localFile = File.createTempFile("tempfile", ".jpg")
            storageRef.getFile(localFile).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                profile_picture.setImageBitmap(bitmap)
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun displayAllFood() {
        val dbRef = FirebaseDatabase.getInstance().getReference("Product")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dishAdapter.deleteAll()
                var count = 0
                for(data in snapshot.children) {
                    if(data.child("provider").value as String == providerKey) {
                        val item = data.getValue(Dish::class.java)
                        if (item != null) {
                            dishAdapter.addDish(item)
                            count++
                        }
                    }
                }
                post_amount.text = count.toString()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
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