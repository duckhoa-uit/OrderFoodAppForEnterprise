package com.example.orderfoodappforenterprise

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.GravityCompat
import com.example.orderfoodappforenterprise.model.Dish
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.dialog_add_food.*
import java.lang.Exception

class AddFoodActivity : AppCompatActivity() {

    private var providerID = ""
    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_add_food)
//        setContentView(CanadaChart(this))

        navView_add_food.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.home_page -> startActivity(Intent(this, ProfileActivity::class.java))
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

        menu_button_add_food.setOnClickListener {
            drawerLayout_add_food.openDrawer(GravityCompat.START)
        }

        findProviderID()

        //set data for type of food combobox
        val listOption = arrayListOf("All food", "Western", "Drinking", "Asian")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOption)
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice)
        type_of_food_option.adapter = adapter

        browse_button.setOnClickListener {
            selectImage()
        }

        clear_button.setOnClickListener {
            clearText()
        }

        add_button.setOnClickListener {
            addFood()
        }

    }

    private fun findProviderID() {
        val providerEmail = Firebase.auth.currentUser?.email.toString()
        val dbRef = FirebaseDatabase.getInstance()
            .getReference("Provider")
            .orderByChild("email")
            .equalTo(providerEmail)
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(data in snapshot.children) {
                    providerID = data.key.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun clearText() {
        food_name_editText.text.clear()
        type_of_food_option.setSelection(-1)
        sale_editText.text.clear()

        price_s_size.text.clear()
        price_m_size.text.clear()
        price_l_size.text.clear()

        amount_s_size.text.clear()
        amount_m_size.text.clear()
        amount_l_size.text.clear()

        image_food.setImageDrawable(null)
        description_edit_text.text.clear()
    }

    private fun addFood() {
        try {
            val name = food_name_editText.text.toString()
            val type =  type_of_food_option.selectedItem.toString()
            val sale = sale_editText.text.toString().toLong()

            val priceS = price_s_size.text.toString().toDouble()
            val priceM = price_m_size.text.toString().toDouble()
            val priceL = price_l_size.text.toString().toDouble()

            val amountS = amount_s_size.text.toString().toLong()
            val amountM = amount_m_size.text.toString().toLong()
            val amountL = amount_l_size.text.toString().toLong()

            val description = description_edit_text.text.toString()

            if(name.isEmpty() || type.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please full fill all information", Toast.LENGTH_LONG).show()
            }
            else {
                val dbRef = FirebaseDatabase.getInstance().getReference("Product")
                val key = dbRef.push().key

                val newDish = Dish(
                    key!!,
                    name,
                    priceS,
                    priceM,
                    priceL,
                    "0",
                    type,
                    description,
                    sale,
                    amountS,
                    0L,
                    amountM,
                    0L,
                    amountL,
                    0L,
                    providerID
                )

                dbRef.child(key).setValue(newDish)
                uploadImage(key)
                Toast.makeText(this, "Add to menu successfully!", Toast.LENGTH_LONG).show()
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error, please check all information again!", Toast.LENGTH_LONG).show()
        }
    }

    private fun selectImage() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 100 && resultCode == RESULT_OK) {
            imageUri = data?.data!!
            image_food.setImageURI(imageUri)
        }
    }

    private fun uploadImage(id: String) {
        val storageRef = FirebaseStorage.getInstance().getReference("dish_image/$id.jpg")
        storageRef.putFile(imageUri)
    }
}

