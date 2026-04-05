package com.example.controlenergy

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val rgAge = findViewById<RadioGroup>(R.id.rgAge)
        val btnPlay = findViewById<MaterialButton>(R.id.btnPlay)

        btnPlay.setOnClickListener {
            val selectedId = rgAge.checkedRadioButtonId
            val difficulty = when (selectedId) {
                R.id.rbAge5_7 -> "EASY"
                R.id.rbAge8_10 -> "MEDIUM"
                R.id.rbAge11_12 -> "HARD"
                else -> "EASY"
            }

            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("DIFFICULTY_LEVEL", difficulty)
            startActivity(intent)
        }
    }
}