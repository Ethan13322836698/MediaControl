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
    private var isLargeMode = false

    companion object {
        const val PREF_NAME = "prefs"
        const val KEY_STYLE  = "style"   // "material" | "apple"
        const val KEY_MODE   = "mode"    // "original" | "compact"
        const val KEY_SIZE   = "size"    // "normal" | "large"
        const val KEY_NIGHT  = "night"   // "system" | "light" | "dark"
        const val KEY_COLOR  = "color"   // "skyblue"|"purple"|"blue"|"green"|"orange"|"red"|"teal"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        applyNightMode()
        applyTheme()
        super.onCreate(savedInstanceState)

        // Must be before setContentView for proper inset handling
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
            else                -> R.style.Theme_MediaControl  // skyblue default
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
            // Compact: only the toggle button + settings icon, nothing else
            binding.originalContainer.visibility = View.GONE
            binding.btnPlayPause.visibility = View.VISIBLE
        } else {
            binding.originalContainer.visibility = View.VISIBLE
            binding.btnPlayPause.visibility = View.GONE
        }

        // Apple = circle buttons arranged horizontally
        // Material = full-width pill buttons stacked
        if (isApple) styleApple(mode)
        else         styleMaterial(mode)

        if (isLargeMode) {
            hideSystemBars()
            enlargeButtons(mode)
        }
    }

    private fun styleApple(mode: String) {
        val dp = resources.displayMetrics.density
        val circleSizePx = (160 * dp).toInt()
        val gapPx = (24 * dp).toInt()

        // Side-by-side circles for original
        binding.originalContainer.orientation = LinearLayout.HORIZONTAL
        binding.originalContainer.gravity = android.view.Gravity.CENTER

        listOf(binding.btnPause, binding.btnPlay).forEach { btn ->
            val lp = LinearLayout.LayoutParams(circleSizePx, circleSizePx)
            lp.setMargins(gapPx / 2, 0, gapPx / 2, 0)
            btn.layoutParams = lp
        }

        // Compact circle
        val bigCircle = (200 * dp).toInt()
        val lp = LinearLayout.LayoutParams(bigCircle, bigCircle)
        lp.gravity = android.view.Gravity.CENTER_HORIZONTAL
        binding.btnPlayPause.layoutParams = lp
    }

    private fun styleMaterial(mode: String) {
        val dp = resources.displayMetrics.density
        val btnHeightPx = (140 * dp).toInt()
        val gapPx = (16 * dp).toInt()

        binding.originalContainer.orientation = LinearLayout.VERTICAL
        binding.originalContainer.gravity = android.view.Gravity.CENTER

        listOf(binding.btnPause to binding.btnPlay).forEach { (pause, play) ->
            val lpPause = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, btnHeightPx
            )
            lpPause.bottomMargin = gapPx
            pause.layoutParams = lpPause

            val lpPlay = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, btnHeightPx
            )
            play.layoutParams = lpPlay
        }

        val lpCompact = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, (200 * dp).toInt()
        )
        binding.btnPlayPause.layoutParams = lpCompact
    }

    private fun hideSystemBars() {
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun enlargeButtons(mode: String) {
        val dp = resources.displayMetrics.density
        val isApple = prefs.getString(KEY_STYLE, "material") == "apple"

        binding.scrollView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        binding.scrollView.isFillViewport = true
        binding.buttonContainer.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        binding.buttonContainer.requestLayout()

        if (isApple) {
            // Large Apple: bigger circles
            val circlePx = (220 * dp).toInt()
            val gap = (32 * dp).toInt()
            listOf(binding.btnPause, binding.btnPlay).forEach { btn ->
                val lp = LinearLayout.LayoutParams(circlePx, circlePx)
                lp.setMargins(gap / 2, 0, gap / 2, 0)
                btn.layoutParams = lp
            }
            val bigPx = (280 * dp).toInt()
            val lp = LinearLayout.LayoutParams(bigPx, bigPx)
            lp.gravity = android.view.Gravity.CENTER_HORIZONTAL
            binding.btnPlayPause.layoutParams = lp
        } else {
            // Large Material: half-screen buttons
            val margin = (24 * dp).toInt()
            val fillParam = { topM: Int, botM: Int ->
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
                ).apply {
                    setMargins(margin, topM, margin, botM)
                }
            }
            if (mode == "compact") {
                binding.btnPlayPause.layoutParams = fillParam(margin, margin)
            } else {
                binding.btnPause.layoutParams = fillParam(margin, margin / 2)
                binding.btnPlay.layoutParams  = fillParam(margin / 2, margin)
            }
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
