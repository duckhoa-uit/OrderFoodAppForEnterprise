package com.example.orderfoodappforenterprise

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.orderfoodappforenterprise.adapter.DishAdapter
import com.example.orderfoodappforenterprise.model.Dish
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
import java.io.File



class ProfileActivity : AppCompatActivity() {
    private var providerKey = ""
    private lateinit var dishAdapter: DishAdapter
    lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        dishAdapter = DishAdapter(mutableListOf())
        allFood_recyclerView.adapter = dishAdapter
        val layoutManager = GridLayoutManager(this,2)
        allFood_recyclerView.layoutManager = layoutManager

        //Sidebar menu
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navView_profile.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.add_food -> Toast.makeText(applicationContext,"Add food", Toast.LENGTH_SHORT).show()
                R.id.home_page -> Toast.makeText(applicationContext,"Home page", Toast.LENGTH_SHORT).show()
                R.id.edit_profile -> Toast.makeText(applicationContext,"Edit profile", Toast.LENGTH_SHORT).show()
                R.id.sign_out -> Toast.makeText(applicationContext,"Sign out", Toast.LENGTH_SHORT).show()
                R.id.statistical -> Toast.makeText(applicationContext,"Statistical", Toast.LENGTH_SHORT).show()
            }
            true
        }

        menu_button.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)

        }

        back_button_nav.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
        }

        displayProvider()
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
                for(data in snapshot.children) {
                    if(data.child("provider").value as String == providerKey) {
                        val item = data.getValue(Dish::class.java)
                        if (item != null) {
                            dishAdapter.addDish(item)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}