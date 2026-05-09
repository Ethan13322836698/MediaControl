package com.mediacontrol.app

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mediacontrol.app.databinding.FragmentSettingsBinding

class SettingsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val prefs = requireContext()
            .getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_PRIVATE)

        val mode  = prefs.getString(MainActivity.KEY_MODE,  "original")
        val size  = prefs.getString(MainActivity.KEY_SIZE,  "normal")
        val night = prefs.getString(MainActivity.KEY_NIGHT, "system")

        if (mode == "compact") binding.chipModeCompact.isChecked  = true
        else                   binding.chipModeOriginal.isChecked = true

        if (size == "large") binding.chipSizeLarge.isChecked = true
        else                 binding.chipSizeNormal.isChecked = true

        when (night) {
            "light" -> binding.chipNightLight.isChecked  = true
            "dark"  -> binding.chipNightDark.isChecked   = true
            else    -> binding.chipNightSystem.isChecked = true
        }

        binding.btnApply.setOnClickListener {
            prefs.edit().apply {
                putString(MainActivity.KEY_MODE,
                    if (binding.chipModeCompact.isChecked) "compact" else "original")
                putString(MainActivity.KEY_SIZE,
                    if (binding.chipSizeLarge.isChecked) "large" else "normal")
                putString(MainActivity.KEY_NIGHT, when {
                    binding.chipNightLight.isChecked -> "light"
                    binding.chipNightDark.isChecked  -> "dark"
                    else                             -> "system"
                })
                apply()
            }
            dismiss()
            (activity as? MainActivity)?.restart()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
