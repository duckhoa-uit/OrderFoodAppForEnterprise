package com.example.orderfoodappforenterprise.adapter

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class LoginAdapter(
    val items: ArrayList<Fragment>,
    activity: AppCompatActivity
): FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return items[position]
    }


}