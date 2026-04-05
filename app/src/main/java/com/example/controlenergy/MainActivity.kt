package com.example.controlenergy

import android.content.res.ColorStateList
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var tvScore: TextView
    private lateinit var tvOperation: TextView
    private lateinit var btnOptions: List<MaterialButton>
    private lateinit var konfettiView: KonfettiView
    
    private lateinit var levelOverlay: View
    private lateinit var tvLevelAnnouncement: TextView
    private lateinit var tvLevelSubtext: TextView
    private lateinit var ivTrophy: ImageView

    private var score = 0
    private var correctAnswer = 0
    private var selectedDifficulty = "EASY"
    private var streak = 0
    private var currentLevel = 1

    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

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
        konfettiView = findViewById(R.id.konfettiView)
        levelOverlay = findViewById(R.id.levelOverlay)
        tvLevelAnnouncement = findViewById(R.id.tvLevelAnnouncement)
        tvLevelSubtext = findViewById(R.id.tvLevelSubtext)
        ivTrophy = findViewById(R.id.ivTrophy)

        selectedDifficulty = intent.getStringExtra("DIFFICULTY_LEVEL") ?: "EASY"

        showLevelAnnouncement(1, false)
    }

    private fun showLevelAnnouncement(level: Int, isLevelUp: Boolean) {
        levelOverlay.visibility = View.VISIBLE
        ivTrophy.visibility = if (isLevelUp) View.VISIBLE else View.GONE
        
        tvLevelAnnouncement.text = if (isLevelUp) "¡NIVEL COMPLETADO!" else "¡NIVEL $level!"
        tvLevelSubtext.text = if (isLevelUp) "¡Eres un genio! Siguiente nivel..." else "¡Prepárate para la Misión!"

        if (isLevelUp) {
            triggerConfetti()
            toneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 200)
            Handler(Looper.getMainLooper()).postDelayed({
                toneGenerator.startTone(ToneGenerator.TONE_DTMF_4, 200)
                toneGenerator.startTone(ToneGenerator.TONE_DTMF_7, 400)
            }, 200)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            levelOverlay.visibility = View.GONE
            setupGame()
        }, if (isLevelUp) 3000 else 2000)
    }

    private fun setupGame() {
        val difficulty = when (selectedDifficulty) {
            "HARD" -> when {
                score >= 15 -> "MULT"
                score >= 7 -> "SUB"
                else -> "SUM"
            }
            "MEDIUM" -> when {
                score >= 20 -> "MULT"
                score >= 10 -> "SUB"
                else -> "SUM"
            }
            else -> when { // EASY
                score >= 30 -> "SUB"
                else -> "SUM"
            }
        }

        val num1: Int
        val num2: Int

        val range = when (selectedDifficulty) {
            "HARD" -> 20
            "MEDIUM" -> 15
            else -> 10
        }

        when (difficulty) {
            "MULT" -> {
                num1 = Random.nextInt(2, if (selectedDifficulty == "HARD") 12 else 9)
                num2 = Random.nextInt(2, if (selectedDifficulty == "HARD") 12 else 9)
                correctAnswer = num1 * num2
                tvOperation.text = "$num1 × $num2"
            }
            "SUB" -> {
                num1 = Random.nextInt(5, range * 2)
                num2 = Random.nextInt(1, num1)
                correctAnswer = num1 - num2
                tvOperation.text = "$num1 - $num2"
            }
            else -> {
                num1 = Random.nextInt(1, range)
                num2 = Random.nextInt(1, range)
                correctAnswer = num1 + num2
                tvOperation.text = "$num1 + $num2"
            }
        }

        val options = mutableSetOf<Int>()
        options.add(correctAnswer)
        
        while (options.size < 4) {
            val offset = Random.nextInt(1, 10)
            val wrong = if (Random.nextBoolean()) correctAnswer + offset else correctAnswer - offset
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
            streak++
            tvScore.text = getString(R.string.score_label, score)
            button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.success_green))
            
            // Sonido de acierto (Doble tono alegre)
            toneGenerator.startTone(ToneGenerator.TONE_DTMF_1, 100)
            Handler(Looper.getMainLooper()).postDelayed({
                toneGenerator.startTone(ToneGenerator.TONE_DTMF_5, 100)
            }, 100)

            // Lógica de subir nivel cada 10 puntos
            if (score > 0 && score % 10 == 0) {
                currentLevel++
                Handler(Looper.getMainLooper()).postDelayed({
                    showLevelAnnouncement(currentLevel, true)
                }, 1000)
                return // Detenemos el flujo normal de setupGame()
            }

            if (streak % 5 == 0) {
                triggerConfetti()
                // Sonido especial de racha
                Handler(Looper.getMainLooper()).postDelayed({
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_PROMPT, 300)
                }, 200)
            }
        } else {
            streak = 0
            button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.error_red))
            
            // Sonido de error (Tono grave y largo)
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 300)

            btnOptions.find { it.text == correctAnswer.toString() }?.let { correctBtn ->
                correctBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.success_green))
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            setupGame()
        }, 1200)
    }

    private fun triggerConfetti() {
        val party = Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
            position = Position.Relative(0.5, 0.3),
            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100)
        )
        konfettiView.start(party)
    }

    override fun onDestroy() {
        super.onDestroy()
        toneGenerator.release()
    }
}
