package com.mediacontrol.app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
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
    private var isLargeMode = false

    companion object {
        const val PREF_NAME = "prefs"
        const val KEY_MODE  = "mode"
        const val KEY_SIZE  = "size"
        const val KEY_NIGHT = "night"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        applyNightMode()
        setTheme(R.style.Theme_MediaControl_Apple)
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        isLargeMode = prefs.getString(KEY_SIZE, "normal") == "large"
        applyLayout()
        setupButtons()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && isLargeMode) hideSystemBars()
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

        if (mode == "compact") {
            binding.originalContainer.visibility = View.GONE
            binding.btnPlayPause.visibility = View.VISIBLE
        } else {
            binding.originalContainer.visibility = View.VISIBLE
            binding.btnPlayPause.visibility = View.GONE
        }

        styleApple(mode)

        if (isLargeMode) {
            hideSystemBars()
            binding.root.post { enlargeButtons(mode) }
        }
    }

    // ── Apple: iOS Control Center style ──────────────────────────────────────
    private fun styleApple(mode: String) {
        val dp = resources.displayMetrics.density
        val isDark = isDarkMode()

        val bgColor   = if (isDark) Color.parseColor("#000000") else Color.parseColor("#F2F2F7")
        val cardColor = if (isDark) Color.parseColor("#1C1C1E") else Color.parseColor("#FFFFFF")
        val pauseIconColor = if (isDark) Color.parseColor("#EBEBF5") else Color.parseColor("#3C3C43")

        binding.root.setBackgroundColor(bgColor)
        binding.buttonContainer.setBackgroundColor(bgColor)

        val circlePx = (180 * dp).toInt()
        val gap      = (28 * dp).toInt()

        binding.originalContainer.orientation = LinearLayout.HORIZONTAL
        binding.originalContainer.gravity = android.view.Gravity.CENTER

        binding.btnPause.apply {
            val lp = LinearLayout.LayoutParams(circlePx, circlePx)
            lp.setMargins(0, 0, gap / 2, 0)
            layoutParams = lp
            backgroundTintList = android.content.res.ColorStateList.valueOf(cardColor)
            iconTint = android.content.res.ColorStateList.valueOf(pauseIconColor)
            iconSize = (circlePx * 0.42).toInt()
            iconGravity = com.google.android.material.button.MaterialButton.ICON_GRAVITY_TEXT_START
            elevation = 8 * dp
            clipToOutline = true
        }

        binding.btnPlay.apply {
            val lp = LinearLayout.LayoutParams(circlePx, circlePx)
            lp.setMargins(gap / 2, 0, 0, 0)
            layoutParams = lp
            backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#007AFF"))
            iconTint = android.content.res.ColorStateList.valueOf(Color.WHITE)
            iconSize = (circlePx * 0.42).toInt()
            iconGravity = com.google.android.material.button.MaterialButton.ICON_GRAVITY_TEXT_START
            elevation = 8 * dp
            clipToOutline = true
        }

        val bigPx = (220 * dp).toInt()
        binding.btnPlayPause.apply {
            val lp = LinearLayout.LayoutParams(bigPx, bigPx)
            lp.gravity = android.view.Gravity.CENTER_HORIZONTAL
            layoutParams = lp
            backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#007AFF"))
            iconTint = android.content.res.ColorStateList.valueOf(Color.WHITE)
            iconSize = (bigPx * 0.42).toInt()
            iconGravity = com.google.android.material.button.MaterialButton.ICON_GRAVITY_TEXT_START
            elevation = 8 * dp
            clipToOutline = true
        }
    }

    // ── Full-screen enlargement ───────────────────────────────────────────────
    private fun enlargeButtons(mode: String) {
        val screenH = resources.displayMetrics.heightPixels
        val screenW = resources.displayMetrics.widthPixels
        val m = (20 * resources.displayMetrics.density).toInt()

        // ScrollView + container fill entire screen
        binding.scrollView.apply {
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            isFillViewport = true
            requestLayout()
        }
        binding.buttonContainer.apply {
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            setPadding(0, 0, 0, 0)
            gravity = android.view.Gravity.CENTER
            requestLayout()
        }

        if (mode == "compact") {
            // Single circle: 70% of smaller screen dimension
            val bigPx = (minOf(screenH, screenW) * 0.70f).toInt()
            binding.btnPlayPause.apply {
                layoutParams = LinearLayout.LayoutParams(bigPx, bigPx).apply {
                    gravity = android.view.Gravity.CENTER_HORIZONTAL
                }
                iconSize = (bigPx * 0.42).toInt()
                requestLayout()
            }
        } else {
            // originalContainer fills screen via weight=1
            binding.originalContainer.apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
                )
                gravity = android.view.Gravity.CENTER
                requestLayout()
            }
            // Two circles: each ~42% of smaller dimension, side-by-side
            val circlePx = (minOf(screenH, screenW) * 0.42f).toInt()
            val gap      = (minOf(screenH, screenW) * 0.06f).toInt()
            binding.btnPause.apply {
                layoutParams = LinearLayout.LayoutParams(circlePx, circlePx).apply {
                    setMargins(0, 0, gap / 2, 0)
                }
                iconSize = (circlePx * 0.42).toInt()
            }
            binding.btnPlay.apply {
                layoutParams = LinearLayout.LayoutParams(circlePx, circlePx).apply {
                    setMargins(gap / 2, 0, 0, 0)
                }
                iconSize = (circlePx * 0.42).toInt()
            }
        }
    }

    private fun hideSystemBars() {
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun isDarkMode(): Boolean {
        val nightMode = resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
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
            // Toggle state and dispatch the appropriate key for reliability
            isPlayingVisual = !isPlayingVisual
            dispatchMediaKey(
                if (isPlayingVisual) KeyEvent.KEYCODE_MEDIA_PLAY
                else KeyEvent.KEYCODE_MEDIA_PAUSE
            )
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
