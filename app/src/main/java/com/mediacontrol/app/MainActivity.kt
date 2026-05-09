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
        const val KEY_STYLE  = "style"
        const val KEY_MODE   = "mode"
        const val KEY_SIZE   = "size"
        const val KEY_NIGHT  = "night"
        const val KEY_COLOR  = "color"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        applyNightMode()
        applyTheme()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        isLargeMode = prefs.getString(KEY_SIZE, "normal") == "large"
        applyLayout()
        setupButtons()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && isLargeMode) hideSystemBars()
    }

    private fun applyTheme() {
        val style = prefs.getString(KEY_STYLE, "material") ?: "material"
        val color = prefs.getString(KEY_COLOR, "skyblue") ?: "skyblue"
        setTheme(when {
            style == "apple"    -> R.style.Theme_MediaControl_Apple
            color == "purple"   -> R.style.Theme_MediaControl_Purple
            color == "blue"     -> R.style.Theme_MediaControl_Blue
            color == "green"    -> R.style.Theme_MediaControl_Green
            color == "orange"   -> R.style.Theme_MediaControl_Orange
            color == "red"      -> R.style.Theme_MediaControl_Red
            color == "teal"     -> R.style.Theme_MediaControl_Teal
            else                -> R.style.Theme_MediaControl
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
        val mode    = prefs.getString(KEY_MODE, "original") ?: "original"
        val isApple = prefs.getString(KEY_STYLE, "material") == "apple"

        if (mode == "compact") {
            binding.originalContainer.visibility = View.GONE
            binding.btnPlayPause.visibility = View.VISIBLE
        } else {
            binding.originalContainer.visibility = View.VISIBLE
            binding.btnPlayPause.visibility = View.GONE
        }

        if (isApple) styleApple(mode)
        else         styleMaterial(mode)

        if (isLargeMode) {
            hideSystemBars()
            // post() defers until after first layout pass — sizes are real then
            binding.root.post { enlargeButtons(mode, isApple) }
        }
    }

    // ── Apple: real iOS feel ──────────────────────────────────────────────────
    // iOS Control Center style: dark/light surface background,
    // large white/dark circles floating on it, icon centered inside circle
    private fun styleApple(mode: String) {
        val dp = resources.displayMetrics.density
        val isDark = isDarkMode()

        // Background: iOS system background
        val bgColor  = if (isDark) Color.parseColor("#000000") else Color.parseColor("#F2F2F7")
        val cardColor = if (isDark) Color.parseColor("#1C1C1E") else Color.parseColor("#FFFFFF")
        val pauseIconColor = if (isDark) Color.parseColor("#EBEBF5") else Color.parseColor("#3C3C43")
        val playIconColor  = Color.parseColor("#007AFF")

        binding.root.setBackgroundColor(bgColor)
        binding.buttonContainer.setBackgroundColor(bgColor)

        // Circle size
        val circlePx = (180 * dp).toInt()
        val gap      = (28 * dp).toInt()

        // Original: two circles side-by-side, horizontally centered
        binding.originalContainer.orientation = LinearLayout.HORIZONTAL
        binding.originalContainer.gravity = android.view.Gravity.CENTER

        // Pause: gray circle (iOS secondary)
        binding.btnPause.apply {
            val lp = LinearLayout.LayoutParams(circlePx, circlePx)
            lp.setMargins(0, 0, gap / 2, 0)
            layoutParams = lp
            setBackgroundColor(Color.TRANSPARENT)
            backgroundTintList = android.content.res.ColorStateList.valueOf(cardColor)
            iconTint = android.content.res.ColorStateList.valueOf(pauseIconColor)
            iconSize = (circlePx * 0.42).toInt()
            elevation = 8 * dp
            clipToOutline = true
        }

        // Play: blue circle (iOS primary)
        binding.btnPlay.apply {
            val lp = LinearLayout.LayoutParams(circlePx, circlePx)
            lp.setMargins(gap / 2, 0, 0, 0)
            layoutParams = lp
            backgroundTintList = android.content.res.ColorStateList.valueOf(
                Color.parseColor("#007AFF")
            )
            iconTint = android.content.res.ColorStateList.valueOf(Color.WHITE)
            iconSize = (circlePx * 0.42).toInt()
            elevation = 8 * dp
            clipToOutline = true
        }

        // Compact: single large circle
        val bigPx = (220 * dp).toInt()
        binding.btnPlayPause.apply {
            val lp = LinearLayout.LayoutParams(bigPx, bigPx)
            lp.gravity = android.view.Gravity.CENTER_HORIZONTAL
            layoutParams = lp
            backgroundTintList = android.content.res.ColorStateList.valueOf(
                Color.parseColor("#007AFF")
            )
            iconTint = android.content.res.ColorStateList.valueOf(Color.WHITE)
            iconSize = (bigPx * 0.42).toInt()
            elevation = 8 * dp
            clipToOutline = true
        }
    }

    // ── Material: full-width pills ────────────────────────────────────────────
    private fun styleMaterial(mode: String) {
        val dp = resources.displayMetrics.density

        binding.originalContainer.orientation = LinearLayout.VERTICAL
        binding.originalContainer.gravity = android.view.Gravity.CENTER

        val btnH  = (150 * dp).toInt()
        val gap   = (20 * dp).toInt()

        binding.btnPause.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, btnH
        ).apply { bottomMargin = gap }

        binding.btnPlay.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, btnH
        )

        binding.btnPlayPause.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, (220 * dp).toInt()
        )
    }

    // ── Full-screen enlargement ───────────────────────────────────────────────
    // Called via post{} so layout is already measured — real heights available
    private fun enlargeButtons(mode: String, isApple: Boolean) {
        val dp = resources.displayMetrics.density

        // ScrollView + container fill entire screen
        binding.scrollView.apply {
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            isFillViewport = true
            requestLayout()
        }
        binding.buttonContainer.apply {
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            setPadding(0, 0, 0, 0)   // remove 32dp padding so content truly fills
            gravity = android.view.Gravity.CENTER
            requestLayout()
        }

        if (isApple) {
            val screenH  = resources.displayMetrics.heightPixels
            val screenW  = resources.displayMetrics.widthPixels

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
                // Two circles: each ~45% of smaller dimension, side-by-side
                val circlePx = (minOf(screenH, screenW) * 0.42f).toInt()
                val gap      = (minOf(screenH, screenW) * 0.06f).toInt()
                binding.originalContainer.apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    gravity = android.view.Gravity.CENTER
                    requestLayout()
                }
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
        } else {
            val m = (20 * dp).toInt()

            if (mode == "compact") {
                binding.btnPlayPause.layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
                ).apply { setMargins(m, m, m, m) }
            } else {
                // originalContainer fills screen, each button = 50%
                binding.originalContainer.layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
                )
                binding.btnPause.layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
                ).apply { setMargins(m, m, m, m / 2) }
                binding.btnPlay.layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
                ).apply { setMargins(m, m / 2, m, m) }
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
