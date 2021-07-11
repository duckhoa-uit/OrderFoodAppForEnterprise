package com.example.orderfoodappforenterprise

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import kotlinx.android.synthetic.main.activity_food_detail.*
import kotlinx.android.synthetic.main.activity_food_detail.back_button
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.dialog_add_food.*
import kotlinx.android.synthetic.main.nav_header.*

class FoodDetail : AppCompatActivity() {
    private lateinit var providerEmail: String

    private var priceS: Double = 0.0
    private var priceM: Double = 0.0
    private var priceL: Double = 0.0

    private var numS: Long = 0
    private var numM: Long = 0
    private var numL: Long = 0

    private lateinit var commentAdapter: CommentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_detail)

        providerEmail = Firebase.auth.currentUser?.email.toString()
        val curDish = intent.getParcelableExtra<Dish>("curDish")

        if (curDish != null) {
            loadData(curDish)
            loadComment(curDish)
        }

        commentAdapter = CommentAdapter(mutableListOf())
        comment_recyclerView.adapter = commentAdapter

        val layoutManager1 = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        comment_recyclerView.layoutManager = layoutManager1
        val itemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        comment_recyclerView.addItemDecoration(itemDecoration)

        back_button.setOnClickListener() {
            finish()
        }

    }

    private fun loadData(curDish: Dish) {
        foodName_text.text = curDish.name
        rates_text.text = curDish.rated
        priceS_textView.setText(curDish.priceS.toString())
        priceM_textView.setText(curDish.priceM.toString())
        priceL_textView.setText(curDish.priceL.toString())
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
}