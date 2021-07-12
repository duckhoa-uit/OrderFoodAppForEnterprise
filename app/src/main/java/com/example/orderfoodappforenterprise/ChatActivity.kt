package com.example.orderfoodappforenterprise

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.orderfoodappforenterprise.adapter.ChatAdapter
import com.example.orderfoodappforenterprise.model.ChatItem
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_chat.drawerLayout
import kotlinx.android.synthetic.main.activity_chat.menu_button
import kotlinx.android.synthetic.main.activity_chat.navView_profile
import kotlinx.android.synthetic.main.activity_edit_profile.*

class ChatActivity : AppCompatActivity() {

    private lateinit var chatAdapter: ChatAdapter
    lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        //Sidebar menu
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView_profile.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.home_page -> startActivity(Intent(this, ProfileActivity::class.java))
                R.id.add_food -> startActivity(Intent(this, AddFoodActivity::class.java))
                R.id.edit_profile -> startActivity(Intent(this, EditProfileActivity::class.java))
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
    }

    override fun onResume() {
        super.onResume()

        chatAdapter = ChatAdapter(mutableListOf())
        chat_recyclerView.adapter = chatAdapter

        val layoutManager = LinearLayoutManager(this)
        chat_recyclerView.layoutManager = layoutManager

        val currentEmail = Firebase.auth.currentUser?.email.toString()
        val hashMap = HashMap<String, ChatItem>()

        val dbRef = FirebaseDatabase.getInstance().getReference("Chat")
        dbRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                chatAdapter.deleteAll()
                for(data in snapshot.children) {
                    if(data.child("senderEmail").value as String == currentEmail
                    ||data.child("receiverEmail").value as String == currentEmail) {
                        val item = data.getValue(ChatItem::class.java)

                        var customerEmail = data.child("senderEmail").value as String
                        if(customerEmail == currentEmail)
                            customerEmail = data.child("receiverEmail").value as String

                        if (item != null) {
                            hashMap[customerEmail] = item
                        }
                    }
                }
                loadData(hashMap)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun convertDate(date: String, time: String): String {
        val arrayDate = date.split("/")
        val arrayTime = time.split(":")
        return arrayDate[2] + arrayDate[1] + arrayDate[0] + arrayTime[0] + arrayTime[1]
    }

    private fun loadData(hashMap: HashMap<String, ChatItem>) {
        val result = hashMap.toList().sortedBy { (_, value) -> convertDate(value.date, value.time)}.toMap()

        for (entry in result) {
            chatAdapter.addChat(entry.value)
        }
    }
}