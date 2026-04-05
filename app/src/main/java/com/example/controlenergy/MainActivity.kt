package com.example.controlenergy

import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var tvScore: TextView
    private lateinit var tvOperation: TextView
    private lateinit var btnOptions: List<MaterialButton>

    private var score = 0
    private var correctAnswer = 0

    // Colores originales de los botones para resetearlos
    private val buttonColors = listOf(
        R.color.btn_blue,
        R.color.btn_purple,
        R.color.btn_orange,
        R.color.btn_yellow
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvScore = findViewById(R.id.tvScore)
        tvOperation = findViewById(R.id.tvOperation)

        btnOptions = listOf(
            findViewById(R.id.btnOption1),
            findViewById(R.id.btnOption2),
            findViewById(R.id.btnOption3),
            findViewById(R.id.btnOption4)
        )

        setupGame()
    }

    private fun setupGame() {
        val num1 = Random.nextInt(1, 10)
        val num2 = Random.nextInt(1, 10)
        correctAnswer = num1 + num2

        tvOperation.text = "$num1 + $num2"

        val options = mutableSetOf(correctAnswer)
        while (options.size < 4) {
            val wrong = correctAnswer + Random.nextInt(-5, 5)
            if (wrong != correctAnswer && wrong > 0) options.add(wrong)
        }

        val shuffledOptions = options.shuffled()

        for (i in btnOptions.indices) {
            val button = btnOptions[i]
            button.text = shuffledOptions[i].toString()
            // Resetear color al original de la paleta
            button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, buttonColors[i]))
            button.isEnabled = true
            button.setOnClickListener {
                checkAnswer(shuffledOptions[i], button)
            }
        }
    }

    private fun checkAnswer(selectedAnswer: Int, button: MaterialButton) {
        btnOptions.forEach { it.isEnabled = false }

        if (selectedAnswer == correctAnswer) {
            score++
            tvScore.text = getString(R.string.score_label, score)
            button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.success_green))
        } else {
            button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.error_red))
            // Resaltar la respuesta correcta
            btnOptions.find { it.text == correctAnswer.toString() }?.let { correctBtn ->
                correctBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.success_green))
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            setupGame()
        }, 1200)
    }
}
