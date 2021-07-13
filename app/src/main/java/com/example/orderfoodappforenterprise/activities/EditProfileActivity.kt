package com.example.orderfoodappforenterprise.activities

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.orderfoodappforenterprise.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.schibstedspain.leku.LATITUDE
import com.schibstedspain.leku.LOCATION_ADDRESS
import com.schibstedspain.leku.LONGITUDE
import com.schibstedspain.leku.LocationPickerActivity
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_edit_profile.menu_button
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle

    private val PLACE_PICKER_REQUEST = 1
    private var providerEmail = ""
    private var providerID = ""

    private var curLat = 0.0
    private var curLon = 0.0
    private var curAddress = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        providerEmail = Firebase.auth.currentUser?.email.toString()

        loadData()

        //Sidebar menu
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView_profile.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.home_page -> startActivity(Intent(this, ProfileActivity::class.java))
                R.id.add_food -> startActivity(Intent(this, AddFoodActivity::class.java))
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

        ic_location.setOnClickListener {
            showMap()
        }

        update_button.setOnClickListener {
            updateData()
        }
    }

    private fun loadData() {
        val dbRef = FirebaseDatabase.getInstance()
            .getReference("Provider")
            .orderByChild("email")
            .equalTo(providerEmail)
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    providerID = data.key.toString()

                    name_inputText.setText(data.child("name").value as String)
                    email_inputText.setText(data.child("email").value as String)
                    phoneNumber_inputText.setText(data.child("phoneNumber").value as String)
                    address_inputText.setText(data.child("location").value as String)

                    //get current coordination base on address
                    convertLocationFromAddress(data.child("location").value as String)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun updateData() {
        val name = name_inputText.text.toString()
        val phoneNumber = phoneNumber_inputText.text.toString()
        val address = address_inputText.text.toString()

        val dbRef = FirebaseDatabase.getInstance().getReference("Provider/$providerID")
        dbRef.child("name").setValue(name)
        dbRef.child("phoneNumber").setValue(phoneNumber)
        dbRef.child("location").setValue(address)
        Toast.makeText(this, "Update successfully!", Toast.LENGTH_LONG).show()
    }

    private fun showMap() {
        val locationPickerIntent = LocationPickerActivity.Builder()
            .withLocation(curLat, curLon)
            .withGooglePlacesApiKey("AIzaSyCYq3nOFi5GoS36fOg7kO6QvP6NzJHxwRc")
            .withSearchZone("vi-VN")
            .build(this)

        startActivityForResult(locationPickerIntent, PLACE_PICKER_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == 1) {
                curLat = data.getDoubleExtra(LATITUDE, 0.0)
                curLon = data.getDoubleExtra(LONGITUDE, 0.0)
                curAddress = data.getStringExtra(LOCATION_ADDRESS).toString()

                if (curAddress.isEmpty())
                    convertLocationFromCoordination()

                address_inputText.setText(curAddress)
            }
        }
    }

    private fun convertLocationFromAddress(myLocation: String) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses: List<Address> = geocoder.getFromLocationName(myLocation, 1)
            val address: Address = addresses[0]
            curLat = address.latitude
            curLon = address.longitude
        } catch (e: Exception) {
            Toast.makeText(this, "Issue with gps, try later!", Toast.LENGTH_LONG).show()
        }
    }

    private fun convertLocationFromCoordination() {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses: List<Address> = geocoder.getFromLocation(curLat, curLon, 1)
            val address = addresses[0].getAddressLine(0)
            curAddress = address
        } catch (e: Exception) {
            Toast.makeText(this, "Issue with gps, try later!", Toast.LENGTH_LONG).show()
        }
    }
}