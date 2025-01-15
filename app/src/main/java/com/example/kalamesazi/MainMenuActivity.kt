package com.example.kalamesazi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.kalamesazi.GameActivity

class MainMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        val buttonNavigate: Button = findViewById(R.id.strtButton)

        buttonNavigate.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }
    }
}