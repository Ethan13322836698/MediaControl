package com.mediacontrol.app

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.mediacontrol.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var audioManager: AudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        setupVolumeSeekBar()
        setupButtons()
    }

    private fun setupVolumeSeekBar() {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        binding.seekbarVolume.max = maxVolume
        binding.seekbarVolume.progress = currentVolume

        binding.seekbarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        progress,
                        0
                    )
                    updateVolumeIcon(progress, maxVolume)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        updateVolumeIcon(currentVolume, maxVolume)
    }

    private fun updateVolumeIcon(volume: Int, maxVolume: Int) {
        val iconRes = when {
            volume == 0 -> R.drawable.ic_volume_off
            volume < maxVolume / 2 -> R.drawable.ic_volume_down
            else -> R.drawable.ic_volume_up
        }
        binding.btnVolume.setIconResource(iconRes)
        binding.tvVolumeLevel.text = "${(volume * 100f / maxVolume).toInt()}%"
    }

    private fun setupButtons() {
        binding.btnVolume.setOnClickListener {
            animateButton(it)
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_SAME,
                AudioManager.FLAG_SHOW_UI
            )
        }

        binding.btnPlay.setOnClickListener {
            animateButton(it)
            dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY)
            Snackbar.make(binding.root, getString(R.string.playing), Snackbar.LENGTH_SHORT).show()
        }

        binding.btnPause.setOnClickListener {
            animateButton(it)
            dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PAUSE)
            Snackbar.make(binding.root, getString(R.string.paused), Snackbar.LENGTH_SHORT).show()
        }

        binding.btnVolumeDown.setOnClickListener {
            animateButton(it)
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_LOWER,
                AudioManager.FLAG_SHOW_UI
            )
            refreshVolumeSeekBar()
        }

        binding.btnVolumeUp.setOnClickListener {
            animateButton(it)
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_RAISE,
                AudioManager.FLAG_SHOW_UI
            )
            refreshVolumeSeekBar()
        }
    }

    private fun dispatchMediaKey(keyCode: Int) {
        val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
        val upEvent = KeyEvent(KeyEvent.ACTION_UP, keyCode)
        audioManager.dispatchMediaKeyEvent(downEvent)
        audioManager.dispatchMediaKeyEvent(upEvent)
    }

    private fun refreshVolumeSeekBar() {
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        binding.seekbarVolume.progress = current
        updateVolumeIcon(current, max)
    }

    private fun animateButton(view: android.view.View) {
        val anim = AnimationUtils.loadAnimation(this, R.anim.button_scale)
        view.startAnimation(anim)
    }

    override fun onResume() {
        super.onResume()
        refreshVolumeSeekBar()
    }
}
