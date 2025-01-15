package com.example.kalamesazi

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.animation.*
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
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

    companion object {
        private const val TAG = "MainActivity"
    }

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
        Log.d(TAG, "Loading word for level: $level")
        wordToGuess = dbHelper.getWordForLevel(level) ?: "BOOK"
        Log.d(TAG, "Word to guess: $wordToGuess")
        glWordDisplay.columnCount = wordToGuess.length
    }

    private fun initializeCharacterButtons() {
        Log.d(TAG, "Initializing character buttons")
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
        Log.d(TAG, "Initializing word display")
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
        Log.d(TAG, "Character selected: ${button.text}")
        button.isEnabled = false
        currentGuess += button.text

        // Animate button click
        val clickAnimation = AlphaAnimation(1.0f, 0.0f)
        clickAnimation.duration = 500
        button.startAnimation(clickAnimation)

        updateWordDisplay()

        if (currentGuess.length == wordToGuess.length) {
            if (currentGuess == wordToGuess) {
                Log.d(TAG, "Correct guess: $currentGuess")
                winAnimation()
            } else {
                Log.d(TAG, "Incorrect guess: $currentGuess")
                loseLife()
            }
        }
    }

    private fun updateWordDisplay() {
        Log.d(TAG, "Updating word display")
        for (i in wordToGuess.indices) {
            val textView = glWordDisplay.getChildAt(i) as TextView
            textView.text = if (i < currentGuess.length) currentGuess[i].toString() else "_"
        }
    }

    private fun nextLevel() {
        Log.d(TAG, "Moving to next level")
        currentLevel++
        currentGuess = ""
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

    private fun loseLife() {
        Log.d(TAG, "Losing a life. Lives left: ${lives - 1}")
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
            Log.d(TAG, "Game over")
            loseAnimation()
        }
    }

    private fun winAnimation() {
        Log.d(TAG, "Playing win animation")
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
        Log.d(TAG, "Playing lose animation")
        val loseAnimation = AnimationUtils.loadAnimation(this, R.anim.lose_animation)
        glWordDisplay.children.forEach { view ->
            if (view is TextView) {
                view.text = "Game Over"
            }
        }
        glWordDisplay.startAnimation(loseAnimation)

        glCharacterButtons.children.forEach { it.isEnabled = false }
    }

    private fun saveGameState() {
        Log.d(TAG, "Saving game state. Lives: $lives, Level: $currentLevel")
        val sharedPreferences = getSharedPreferences("GamePreferences", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putInt("Lives", lives)
            putInt("Level", currentLevel)
            apply()
        }
    }

    private fun loadGameState() {
        Log.d(TAG, "Loading game state")
        val sharedPreferences = getSharedPreferences("GamePreferences", Context.MODE_PRIVATE)
        lives = sharedPreferences.getInt("Lives", 5)
        currentLevel = sharedPreferences.getInt("Level", 1)

        Log.d(TAG, "Loaded game state. Lives: $lives, Level: $currentLevel")

        for (i in 0 until llLives.childCount) {
            llLives.getChildAt(i).visibility = if (i < lives) ImageView.VISIBLE else ImageView.GONE
        }
    }
}
