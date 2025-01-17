package com.example.kalamesazi

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.*
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children

class GameActivity : AppCompatActivity() {

    private var currentGuess = ""
    private var lives = 5
    private var currentLevel = 1
    private lateinit var glWordDisplay: GridLayout
    private lateinit var glCharacterButtons: GridLayout
    private lateinit var llLives: LinearLayout
    private lateinit var dbHelper: WordsDbHelper
    private var wordToGuess: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        glWordDisplay = findViewById(R.id.glWordDisplay)
        glCharacterButtons = findViewById(R.id.glCharacterButtons)
        llLives = findViewById(R.id.llLives)
        dbHelper = WordsDbHelper(this)

        loadGameState()
        loadWordForLevel(currentLevel)
        initializeCharacterButtons()
        initializeWordDisplay()
        updateWordDisplay()
    }

    override fun onPause() {
        super.onPause()
        saveGameState()
    }

    private fun loadWordForLevel(level: Int) {
        wordToGuess = dbHelper.getWordForLevel(level) ?: "BOOK"
        if (wordToGuess.isNotEmpty()) {
            glWordDisplay.columnCount = wordToGuess.length
        }
    }

    private fun initializeCharacterButtons() {
        val characters = wordToGuess.toCharArray().toList().shuffled()
        glCharacterButtons.removeAllViews()
        characters.forEach { char ->
            val button = Button(this)
            button.text = char.toString()
            val params = GridLayout.LayoutParams().apply {
                setMargins(16, 16, 16, 16)
            }
            button.layoutParams = params
            button.setOnClickListener { onCharacterSelected(button) }
            glCharacterButtons.addView(button)
        }
    }

    private fun initializeWordDisplay() {
        glWordDisplay.removeAllViews()
        for (i in wordToGuess.indices) {
            val textView = TextView(this)
            textView.text = "_"
            textView.textSize = 24f
            val params = GridLayout.LayoutParams().apply {
                setMargins(8, 8, 8, 8)
            }
            textView.layoutParams = params
            glWordDisplay.addView(textView)
        }
    }

    private fun onCharacterSelected(button: Button) {
        button.isEnabled = false
        currentGuess += button.text

        val clickAnimation = AlphaAnimation(1.0f, 0.0f)
        clickAnimation.duration = 500
        button.startAnimation(clickAnimation)

        updateWordDisplay()

        if (currentGuess.length == wordToGuess.length) {
            if (currentGuess == wordToGuess) {
                winAnimation()
            } else {
                loseLife()
            }
        }
    }

    private fun updateWordDisplay() {
        for (i in wordToGuess.indices) {
            val textView = glWordDisplay.getChildAt(i) as TextView
            textView.text = if (i < currentGuess.length) currentGuess[i].toString() else "_"
        }
    }

    private fun nextLevel() {
        currentLevel++
        currentGuess = ""

        if (dbHelper.getWordForLevel(currentLevel) == null) {
            showCompletionDialog()
        } else {
            loadWordForLevel(currentLevel)
            initializeCharacterButtons()
            initializeWordDisplay()
            updateWordDisplay()

            val nextLevelAnimation = TranslateAnimation(0f, 0f, 0f, 1000f)
            nextLevelAnimation.duration = 1000
            glWordDisplay.startAnimation(nextLevelAnimation)

            Handler().postDelayed({
                glWordDisplay.clearAnimation()
            }, 1000)
        }
    }

    private fun showCompletionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Congratulations!")
            .setMessage("You've completed all the levels. Would you like to go to the main menu?")
            .setPositiveButton("Ok") { _, _ ->
                navigateToMainMenu()
            }
            .show()
    }

    private fun navigateToMainMenu() {
        lives = 5
        currentLevel = 1
        currentGuess = ""

        val intent = Intent(this, MainMenuActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun loseLife() {
        if (lives > 0) {
            lives--
            val lostLifeAnimation = AnimationUtils.loadAnimation(this, R.anim.heart_pulse)
            val heartToRemove = llLives.getChildAt(lives) as ImageView
            heartToRemove.startAnimation(lostLifeAnimation)
            llLives.removeViewAt(lives)

            currentGuess = ""
            initializeCharacterButtons()
            updateWordDisplay()
        } else {
            loseAnimation()
        }
    }

    private fun winAnimation() {
        val winAnimationSet = AnimationSet(true).apply {
            addAnimation(ScaleAnimation(1.0f, 2.0f, 1.0f, 2.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f))
            addAnimation(RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f))
            duration = 1000
        }
        glWordDisplay.startAnimation(winAnimationSet)

        Handler().postDelayed({
            nextLevel()
        }, 2000)
    }

    private fun loseAnimation() {
        val loseAnimation = AnimationSet(true).apply {
            addAnimation(ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f))
            addAnimation(AlphaAnimation(1.0f, 0.0f))
            duration = 1000
        }
        glWordDisplay.startAnimation(loseAnimation)

        glCharacterButtons.children.forEach { it.isEnabled = false }

        Handler().postDelayed({
            resetGame()
        }, 2000)
    }

    private fun resetGame() {
        lives = 5
        currentLevel = 1
        currentGuess = ""

        loadWordForLevel(currentLevel)
        initializeCharacterButtons()
        initializeWordDisplay()
        updateWordDisplay()
    }

    private fun saveGameState() {
        val sharedPreferences = getSharedPreferences("GamePreferences", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putInt("Lives", lives)
            putInt("Level", currentLevel)
            apply()
        }
    }

    private fun loadGameState() {
        val sharedPreferences = getSharedPreferences("GamePreferences", Context.MODE_PRIVATE)
        lives = sharedPreferences.getInt("Lives", 5)
        currentLevel = sharedPreferences.getInt("Level", 1)

        for (i in 0 until llLives.childCount) {
            llLives.getChildAt(i).visibility = if (i < lives) ImageView.VISIBLE else ImageView.GONE
        }
    }
}
