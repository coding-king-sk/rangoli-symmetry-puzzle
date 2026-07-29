package com.rehan.rangoli

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rehan.rangoli.ui.RangoliApp
import com.rehan.rangoli.ui.theme.RangoliTheme

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			RangoliTheme {
				RangoliApp()
			}
		}
	}
}
