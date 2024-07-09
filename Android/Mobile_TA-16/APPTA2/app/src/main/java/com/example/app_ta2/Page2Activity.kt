package com.example.app_ta2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class Page2Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page2)

        // Temukan tombol berdasarkan ID
        val myButton: Button = findViewById(R.id.btn1Page2)
        // Atur OnClickListener untuk tombol
        myButton.setOnClickListener {
            // Buat Intent yang mengarah ke SecondActivity
            val intent = Intent(this, MainActivity::class.java)

            // Mulai Activity
            startActivity(intent)
        }

    }
}