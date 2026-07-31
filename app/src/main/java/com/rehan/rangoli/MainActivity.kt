package com.rehan.rangoli

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rehan.rangoli.ui.RangoliApp

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		// RangoliApp applies RangoliTheme itself (it owns the dark/light preference),
		// so we must NOT wrap it in a second theme here or the toggle has no effect.
		setContent { RangoliApp() }
	}
}
