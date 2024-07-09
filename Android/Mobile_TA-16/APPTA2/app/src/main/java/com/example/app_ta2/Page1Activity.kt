package com.example.app_ta2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class Page1Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page1)

        // Temukan tombol berdasarkan ID
        val myButton: Button = findViewById(R.id.Btn1Page1)
        // Atur OnClickListener untuk tombol
        myButton.setOnClickListener {
            // Buat Intent yang mengarah ke SecondActivity
            val intent = Intent(this, Page2Activity::class.java)

            // Mulai Activity
            startActivity(intent)
        }

    }
}