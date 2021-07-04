package com.example.orderfoodappforenterprise

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.GravityCompat
import kotlinx.android.synthetic.main.dialog_add_food.*
import kotlinx.android.synthetic.main.nav_header.*

class AddFood : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_add_food)

        navView_add_food.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.add_food -> Toast.makeText(applicationContext,"Add food", Toast.LENGTH_SHORT).show()
                R.id.home_page -> Toast.makeText(applicationContext,"Home page", Toast.LENGTH_SHORT).show()
                R.id.edit_profile -> Toast.makeText(applicationContext,"Edit profile", Toast.LENGTH_SHORT).show()
                R.id.sign_out -> Toast.makeText(applicationContext,"Sign out", Toast.LENGTH_SHORT).show()
                R.id.statistical -> Toast.makeText(applicationContext,"Statistical", Toast.LENGTH_SHORT).show()
            }
            true
        }

        menu_button_add_food.setOnClickListener {
            drawerLayout_add_food.openDrawer(GravityCompat.START)
        }

        back_button_nav.setOnClickListener {
            drawerLayout_add_food.closeDrawer(GravityCompat.END)
        }
    }
}