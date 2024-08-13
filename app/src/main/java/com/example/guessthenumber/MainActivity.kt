package com.example.guessthenumber

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

object GameExtras {
    const val EXTRA_FIRST_GUESS = "com.example.guessthenumber.FIRST_GUESS"
    const val EXTRA_TARGET = "com.example.guessthenumber.TARGET_NUMBER"
    const val EXTRA_ATTEMPTS_LEFT = "com.example.guessthenumber.ATTEMPTS_LEFT"
}

class MainActivity : AppCompatActivity() {

    private lateinit var guessInput: EditText
    private lateinit var submitButton: Button
    private lateinit var attemptsTextView: TextView

    private var targetNumber: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        guessInput = findViewById(R.id.guessInput)
        submitButton = findViewById(R.id.submitButton)
        attemptsTextView = findViewById(R.id.titleTextView)

        targetNumber = (1..100).random()
        attemptsTextView.text = "Let's Play! Guess the number"

        submitButton.setOnClickListener {
            handleFirstGuess()
        }
    }

    private fun handleFirstGuess() {
        val guessText = guessInput.text.toString()
        if (guessText.isNotEmpty()) {
            val userGuess = guessText.toIntOrNull()
            if (userGuess != null && userGuess in 1..100) {
                navigateToResult(userGuess)
            } else {
                Toast.makeText(this, "Please enter a number between 1 and 100", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please enter a guess", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToResult(firstGuess: Int) {
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra(GameExtras.EXTRA_FIRST_GUESS, firstGuess)
            putExtra(GameExtras.EXTRA_TARGET, targetNumber)
            putExtra(GameExtras.EXTRA_ATTEMPTS_LEFT, 5)
        }
        startActivity(intent)
        finish()
    }
}