package com.example.easyimagepicker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import stanley.du_foster.easyimagepicker.EasyImagePicker

class MainActivity : AppCompatActivity() {

    lateinit var easyImagePicker: EasyImagePicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        easyImagePicker = EasyImagePicker(this)


        easyImagePicker.pickImageUri().observe(this, {
            Log.v("112233", "")
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        easyImagePicker.onDestroy()
    }
}