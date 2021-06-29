package com.example.orderfoodappforenterprise.adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.orderfoodappforenterprise.R
import com.example.orderfoodappforenterprise.model.CommentItem
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.comment_item.view.*
import java.io.File

class CommentAdapter (
    private val commentList: MutableList<CommentItem>
): RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {
    private var name: String = ""

    class CommentViewHolder (itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        return CommentViewHolder (
            LayoutInflater.from(parent.context).inflate(
                R.layout.comment_item,
                parent,
                false
            )
        )
    }

    fun addComment(comment: CommentItem) {
        commentList.add(0, comment)
        notifyItemInserted(0)
    }

    fun deleteAll() {
        commentList.clear()
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val curComment = commentList[position]
        val dbRef = FirebaseDatabase.getInstance().getReference("Customer")
        dbRef.get().addOnSuccessListener {
            for(data in it.children) {
                if(data.child("email").value as String == curComment.customerEmail) {
                    name = if(data.child("fullName").value as String != "edit here")
                        data.child("fullName").value as String
                    else
                        "Unknown"

                    var imageName = data.child("email").value as String
                    imageName = imageName.replace(".", "_")

                    val storageRef = FirebaseStorage.getInstance().getReference("avatar_image/$imageName.jpg")
                    try {
                        val localFile = File.createTempFile("tempfile", ".jpg")
                        storageRef.getFile(localFile).addOnSuccessListener {
                            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                            holder.itemView.circleImageView.setImageBitmap(bitmap)
                        }
                    }
                    catch (e: Exception) {
                        e.printStackTrace()
                    }

                    holder.itemView.apply {
                        name_textView.text = name
                        time_textView.text = curComment.time
                        content_textView.text = curComment.comment
                    }

                    break
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return commentList.size
    }


}