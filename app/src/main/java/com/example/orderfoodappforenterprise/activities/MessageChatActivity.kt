package com.example.orderfoodappforenterprise.activities

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.orderfoodappforenterprise.R
import com.example.orderfoodappforenterprise.adapter.MessageAdapter
import com.example.orderfoodappforenterprise.model.ChatItem
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.android.synthetic.main.activity_message.avatar_image
import kotlinx.android.synthetic.main.activity_message.back_button
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MessageChatActivity : AppCompatActivity() {
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var currentEmail: String
    private lateinit var customerEmail: String

    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        currentEmail = Firebase.auth.currentUser?.email.toString()
        customerEmail = intent.getStringExtra("customerEmail").toString()

        messageAdapter = MessageAdapter(mutableListOf())
        message_recyclerView.adapter = messageAdapter
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        message_recyclerView.layoutManager = layoutManager

        loadData(customerEmail)

        back_button.setOnClickListener() {
            finish()
        }

        send_button.setOnClickListener() {
            val message = message_editText.text.toString()
            if(message.isNotEmpty())
                sendMessage(currentEmail, customerEmail, message)
        }

        photo_button.setOnClickListener() {
            selectImage()
        }
    }

    private fun loadData(customerEmail: String) {
        //load image
        val imageName = customerEmail.replace(".", "_")
        val storageRef = FirebaseStorage.getInstance().getReference("avatar_image/$imageName.jpg")
        try {
            val localFile = File.createTempFile("tempfile", ".jpg")
            storageRef.getFile(localFile).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                avatar_image.setImageBitmap(bitmap)
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
        }

        //load name
        val dbRefName = FirebaseDatabase.getInstance().getReference("Customer")
        dbRefName.get().addOnSuccessListener {
            for(data in it.children) {
                if(data.child("email").value as String == customerEmail) {
                    name_textView.text = data.child("fullName").value as String
                    break
                }
            }
        }

        //load chat message
        val dbRef = FirebaseDatabase.getInstance().getReference("Chat")
        dbRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messageAdapter.deleteAll()
                for(data in snapshot.children) {
                    if((data.child("senderEmail").value as String == currentEmail && data.child("receiverEmail").value as String == customerEmail)
                    || (data.child("receiverEmail").value as String == currentEmail && data.child("senderEmail").value as String == customerEmail)) {
                        val item = data.getValue(ChatItem::class.java)
                        if (item != null) {
                            messageAdapter.addMessage(item)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun sendMessage(senderEmail: String, receiverEmail: String, message: String) {
        val curTime = Calendar.getInstance().time
        val sdfDate = SimpleDateFormat("dd/MM/yyyy")
        val sdfTime = SimpleDateFormat("HH:mm")

        val date = sdfDate.format(curTime)
        val time = sdfTime.format(curTime)

        val chat = ChatItem(
            senderEmail,
            receiverEmail,
            "",
            message,
            date,
            time
        )

        val dbRef = FirebaseDatabase.getInstance().getReference("Chat")
        dbRef.push().setValue(chat)
        message_editText.text.clear()
    }

    private fun sendImage(senderEmail: String, receiverEmail: String) {
        val curTime = Calendar.getInstance().time
        val sdfDate = SimpleDateFormat("dd/MM/yyyy")
        val sdfTime = SimpleDateFormat("HH:mm")

        val date = sdfDate.format(curTime)
        val time = sdfTime.format(curTime)

        val dbRef = FirebaseDatabase.getInstance().getReference("Chat")
        val key = dbRef.push().key.toString()

        val chat = ChatItem(
            senderEmail,
            receiverEmail,
            key,
            "",
            date,
            time
        )

        //upload image to firebase storage
        val storageRef = FirebaseStorage.getInstance().getReference("chat_image/$key.jpg")
        storageRef.putFile(imageUri).addOnSuccessListener {
            //upload image path to firebase database
            dbRef.child(key).setValue(chat)
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
            sendImage(currentEmail, customerEmail)
        }
    }

}