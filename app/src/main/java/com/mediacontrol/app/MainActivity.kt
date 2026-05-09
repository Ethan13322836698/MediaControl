package com.mediacontrol.app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.mediacontrol.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var audioManager: AudioManager
    private lateinit var prefs: SharedPreferences
    private var isPlayingVisual = false

    companion object {
        const val PREF_NAME  = "prefs"
        const val KEY_STYLE  = "style"   // "material" | "apple"
        const val KEY_MODE   = "mode"    // "original" | "compact"
        const val KEY_SIZE   = "size"    // "normal" | "large"
        const val KEY_NIGHT  = "night"   // "system" | "light" | "dark"
        const val KEY_COLOR  = "color"   // "purple"|"blue"|"green"|"orange"|"red"|"teal"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        applyNightMode()
        applyTheme()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        applyLayout()
        setupButtons()
    }

    private fun applyTheme() {
        val style = prefs.getString(KEY_STYLE, "material") ?: "material"
        val color = prefs.getString(KEY_COLOR, "purple") ?: "purple"
        setTheme(when {
            style == "apple"   -> R.style.Theme_MediaControl_Apple
            color == "blue"    -> R.style.Theme_MediaControl_Blue
            color == "green"   -> R.style.Theme_MediaControl_Green
            color == "orange"  -> R.style.Theme_MediaControl_Orange
            color == "red"     -> R.style.Theme_MediaControl_Red
            color == "teal"    -> R.style.Theme_MediaControl_Teal
            else               -> R.style.Theme_MediaControl
        })
    }

    private fun applyNightMode() {
        AppCompatDelegate.setDefaultNightMode(when (
            prefs.getString(KEY_NIGHT, "system")
        ) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark"  -> AppCompatDelegate.MODE_NIGHT_YES
            else    -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        })
    }

    private fun applyLayout() {
        val mode = prefs.getString(KEY_MODE, "original") ?: "original"
        val size = prefs.getString(KEY_SIZE, "normal") ?: "normal"

        if (mode == "compact") {
            binding.btnPlay.visibility = View.GONE
            binding.btnPause.visibility = View.GONE
            binding.btnPlayPause.visibility = View.VISIBLE
        } else {
            binding.btnPlay.visibility = View.VISIBLE
            binding.btnPause.visibility = View.VISIBLE
            binding.btnPlayPause.visibility = View.GONE
        }

        if (size == "large") {
            enterFullscreen()
            enlargeButtons(mode)
        }
    }

    private fun enterFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun enlargeButtons(mode: String) {
        // Remove scroll, make container fill screen
        binding.scrollView.apply {
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            isFillViewport = true
        }
        binding.buttonContainer.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

        val fillParam = { margin: Int ->
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
            ).apply {
                setMargins(margin, margin / 2, margin, margin / 2)
            }
        }
        val m = resources.getDimensionPixelSize(R.dimen.large_btn_margin)

        if (mode == "compact") {
            binding.btnPlayPause.layoutParams = fillParam(m)
        } else {
            binding.btnPlay.layoutParams  = fillParam(m)
            binding.btnPause.layoutParams = fillParam(m)
        }
    }

    private fun setupButtons() {
        binding.btnPlay.setOnClickListener {
            animateButton(it)
            dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY)
        }
        binding.btnPause.setOnClickListener {
            animateButton(it)
            dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PAUSE)
        }
        binding.btnPlayPause.setOnClickListener {
            animateButton(it)
            isPlayingVisual = !isPlayingVisual
            dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            binding.btnPlayPause.setIconResource(
                if (isPlayingVisual) R.drawable.ic_pause else R.drawable.ic_play
            )
        }
        binding.btnSettings.setOnClickListener {
            SettingsBottomSheet().show(supportFragmentManager, "settings")
        }
    }

    private fun dispatchMediaKey(keyCode: Int) {
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
    }

    private fun animateButton(view: View) {
        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_scale))
    }

    fun restart() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
