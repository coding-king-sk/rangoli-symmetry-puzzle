package com.rehan.rangoli.ui.game

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember

/**
 * Thin wrapper around [ToneGenerator] that maps game events to short tones.
 *
 * All methods silently swallow exceptions so a missing audio device never
 * crashes the game.
 */
class SoundManager {
	private var toneGen: ToneGenerator? = null
	var enabled: Boolean = true

	fun init() {
		runCatching { toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 55) }
	}

	/** Short click when placing a tile. */
	fun playPlace() = tone(ToneGenerator.TONE_PROP_BEEP, 35)

	/** Error buzz when the board check fails. */
	fun playWrong() = tone(ToneGenerator.TONE_PROP_NACK, 160)

	/** Upbeat ding on a successful solve. */
	fun playSolve() = tone(ToneGenerator.TONE_PROP_ACK, 260)

	/** Soft beep when a hint is used. */
	fun playHint() = tone(ToneGenerator.TONE_DTMF_5, 70)

	/** Subtle pop on undo. */
	fun playUndo() = tone(ToneGenerator.TONE_DTMF_0, 45)

	private fun tone(type: Int, ms: Int) {
		if (!enabled) return
		runCatching { toneGen?.startTone(type, ms) }
	}

	fun release() {
		toneGen?.release()
		toneGen = null
	}
}

@Composable
fun rememberSoundManager(): SoundManager {
	val manager = remember { SoundManager() }
	DisposableEffect(manager) {
		manager.init()
		onDispose { manager.release() }
	}
	return manager
}
