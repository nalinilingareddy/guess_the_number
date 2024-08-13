package com.example.guessthenumber

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class ResultActivity : AppCompatActivity() {

    private lateinit var resultTextView: TextView
    private lateinit var attemptsTextView: TextView
    private lateinit var firstGuessTextView: TextView
    private lateinit var newGuessInput: EditText
    private lateinit var submitGuessButton: Button
    private lateinit var restartButton: Button
    private lateinit var hintTextView: TextView

    private var attemptsLeft: Int = 5
    private var targetNumber: Int = -1
    private var firstGuess: Int = -1

    private val notificationPermissionRequestCode = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        resultTextView = findViewById(R.id.resultTextView)
        attemptsTextView = findViewById(R.id.attemptsTextView)
        firstGuessTextView = findViewById(R.id.firstGuessTextView)
        newGuessInput = findViewById(R.id.newGuessInput)
        submitGuessButton = findViewById(R.id.submitGuessButton)
        restartButton = findViewById(R.id.restartButton)
        hintTextView = findViewById(R.id.hintTextView)

        createNotificationChannel()

        firstGuess = intent.getIntExtra(GameExtras.EXTRA_FIRST_GUESS, -1)
        attemptsLeft = intent.getIntExtra(GameExtras.EXTRA_ATTEMPTS_LEFT, 5)
        targetNumber = intent.getIntExtra(GameExtras.EXTRA_TARGET, -1)

        if (firstGuess == targetNumber) {
            displayCongratulations()
        } else {
            attemptsLeft--
            provideHint(firstGuess)
            updateUI()
        }

        submitGuessButton.setOnClickListener {
            handleGuess()
        }

        restartButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        requestNotificationPermission()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Game Notifications"
            val descriptionText = "Channel for game notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("game_notifications", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateUI() {
        attemptsTextView.text = "Attempts left: $attemptsLeft"
        firstGuessTextView.text = "Your first guess was: $firstGuess"
    }

    private fun handleGuess() {
        val guessText = newGuessInput.text.toString()
        if (guessText.isNotEmpty()) {
            val guess = guessText.toIntOrNull()
            if (guess != null && guess in 1..100) {
                attemptsLeft--
                when {
                    guess == targetNumber -> displayCongratulations()
                    attemptsLeft > 0 -> provideHint(guess)
                    else -> displayGameOver()
                }
                updateUI()
            } else {
                Toast.makeText(this, "Please enter a valid number between 1 and 100", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please enter a guess", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayCongratulations() {
        val attemptsUsed = 5 - attemptsLeft
        resultTextView.text = "Congratulations! You guessed it right within $attemptsUsed attempts!"
        newGuessInput.visibility = View.GONE
        submitGuessButton.visibility = View.GONE
        hintTextView.text = ""
        sendNotification("Congratulations! You won the game in $attemptsUsed attempts!", attemptsUsed)
    }

    private fun provideHint(guess: Int) {
        attemptsTextView.text = "Attempts left: $attemptsLeft"
        val difference = guess - targetNumber
        hintTextView.text = when {
            difference > 0 && difference <= 2 -> "Very close, just a bit lower!"
            difference > 2 && difference <= 10 -> "Close, but a bit lower."
            difference > 10 -> "Too high! Try a much lower number."
            difference < 0 && difference >= -2 -> "Very close, just a bit higher!"
            difference < -2 && difference >= -10 -> "Close, but a bit higher."
            difference < -10 -> "Too low! Try a much higher number."
            else -> "Keep trying, you're getting there!"
        }
    }

    private fun displayGameOver() {
        resultTextView.text = "Game Over!"
        hintTextView.text = "The correct number was: $targetNumber"
        newGuessInput.visibility = View.GONE
        submitGuessButton.visibility = View.GONE
        sendNotification("Game Over! The correct number was $targetNumber. Please try again.", attemptsLeft)
    }

    private fun sendNotification(message: String, attempts: Int) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

            requestNotificationPermission()
            return
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, "game_notifications")
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("Guess the Number")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(0, builder.build())
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), notificationPermissionRequestCode)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == notificationPermissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
