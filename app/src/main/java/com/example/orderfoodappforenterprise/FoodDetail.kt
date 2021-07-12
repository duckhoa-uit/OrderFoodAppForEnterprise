package com.example.orderfoodappforenterprise

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.orderfoodappforenterprise.adapter.CommentAdapter
import com.example.orderfoodappforenterprise.model.CommentItem
import com.example.orderfoodappforenterprise.model.Dish
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_food_detail.*
import kotlinx.android.synthetic.main.activity_food_detail.back_button
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.dialog_add_food.*
import kotlinx.android.synthetic.main.nav_header.*
import java.io.File
import java.lang.Exception

class FoodDetail : AppCompatActivity() {
    private lateinit var providerEmail: String

    private lateinit var commentAdapter: CommentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_detail)

        providerEmail = Firebase.auth.currentUser?.email.toString()
        val curDish = intent.getParcelableExtra<Dish>("curDish")

        if (curDish != null) {
            //load food image
            val storageRef = FirebaseStorage.getInstance().getReference("dish_image/${curDish.id}.jpg")
            try {
                val localFile = File.createTempFile("tempfile", ".jpg")
                storageRef.getFile(localFile).addOnSuccessListener {
                    val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                    food_image.setImageBitmap(bitmap)
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
            }

            loadData(curDish)
            loadComment(curDish)
        }

        commentAdapter = CommentAdapter(mutableListOf())
        comment_recyclerView.adapter = commentAdapter

        val layoutManager1 = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        comment_recyclerView.layoutManager = layoutManager1
        val itemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        comment_recyclerView.addItemDecoration(itemDecoration)

        edit_button.setOnClickListener {
            enableEdit()
        }

        btnUpdate.setOnClickListener {
            updateDish(curDish!!)
        }

        back_button.setOnClickListener {
            finish()
        }

    }

    private fun loadData(curDish: Dish) {
        foodName_text.text = curDish.name
        rates_text.text = curDish.rated

        priceS_textView.setText(curDish.priceS.toString())
        priceM_textView.setText(curDish.priceM.toString())
        priceL_textView.setText(curDish.priceL.toString())

        numSsold_textView.setText(curDish.amountSsold.toString())
        numMsold_textView.setText(curDish.amountMsold.toString())
        numLsold_textView.setText(curDish.amountLsold.toString())

        numSleft_textView.setText(curDish.amountS.toString())
        numMleft_textView.setText(curDish.amountM.toString())
        numLleft_textView.setText(curDish.amountL.toString())

        saleOff_textView.setText(curDish.salePercent.toString())
        description_textView.setText(curDish.description)
    }

    private fun loadComment(curDish: Dish) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Comment/${curDish.id}")
        dbRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                commentAdapter.deleteAll()
                for(data in snapshot.children) {
                    val comment = data.getValue(CommentItem::class.java)
                    if (comment != null) {
                        commentAdapter.addComment(comment)
                    }
                }

                if(commentAdapter.itemCount == 0)
                    comment_textView.text = "No comments yet!"
                else
                    comment_textView.text = "Comments"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FoodDetail, "Cannot load comments!", Toast.LENGTH_LONG).show()
            }

        })
    }

    private fun updateDish(curDish: Dish) {
        try {
            val newNumS = numSleft_textView.text.toString().toLong()
            val newNumM = numMleft_textView.text.toString().toLong()
            val newNumL = numLleft_textView.text.toString().toLong()

            val newPriceS = priceS_textView.text.toString().toDouble()
            val newPriceM = priceM_textView.text.toString().toDouble()
            val newPriceL = priceL_textView.text.toString().toDouble()

            val newSaleOff = saleOff_textView.text.toString().toLong()
            val newDes = description_textView.text.toString()

            val dbRef = FirebaseDatabase.getInstance().getReference("Product/${curDish.id}")

            dbRef.child("amountS").setValue(newNumS)
            dbRef.child("amountM").setValue(newNumM)
            dbRef.child("amountL").setValue(newNumL)

            dbRef.child("priceS").setValue(newPriceS)
            dbRef.child("priceM").setValue(newPriceM)
            dbRef.child("priceL").setValue(newPriceL)

            dbRef.child("salePercent").setValue(newSaleOff)
            dbRef.child("description").setValue(newDes)

            disableEdit()
            Toast.makeText(this, "Update successfully!", Toast.LENGTH_LONG).show()
        }
        catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Please full fill information", Toast.LENGTH_LONG).show()
        }

    }

    private fun enableEdit() {
        foodName_text.isEnabled = true
        numSleft_textView.isEnabled = true
        numMleft_textView.isEnabled = true
        numLleft_textView.isEnabled = true
        priceS_textView.isEnabled = true
        priceM_textView.isEnabled = true
        priceL_textView.isEnabled = true
        saleOff_textView.isEnabled = true
        description_textView.isEnabled = true
        btnUpdate.visibility = View.VISIBLE
    }

    private fun disableEdit() {
        foodName_text.isEnabled = false
        numSleft_textView.isEnabled = false
        numMleft_textView.isEnabled = false
        numLleft_textView.isEnabled = false
        priceS_textView.isEnabled = false
        priceM_textView.isEnabled = false
        priceL_textView.isEnabled = false
        saleOff_textView.isEnabled = false
        description_textView.isEnabled = false
        btnUpdate.visibility = View.GONE
    }
}