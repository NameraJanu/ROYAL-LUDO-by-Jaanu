package com.example.ludo.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

class LudoAudioEngine(private val context: Context? = null) {

    private val sampleRate = 22050
    private val scope = CoroutineScope(Dispatchers.Default)

    @Volatile
    var isSoundEnabled: Boolean = true
        set(value) {
            field = value
            if (!value) {
                sfxQueue.clear()
            }
        }

    @Volatile
    var isMusicEnabled: Boolean = true
        set(value) {
            field = value
            if (value) {
                startMusicLoop()
            } else {
                stopMusicLoop()
            }
        }

    private var musicJob: Job? = null
    private var sfxJob: Job? = null

    private val sfxQueue = ConcurrentLinkedQueue<ShortArray>()

    private var sfxTrack: AudioTrack? = null
    private var musicTrack: AudioTrack? = null

    init {
        initSfxEngine()
    }

    private fun initSfxEngine() {
        sfxJob = scope.launch {
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(2048)

            val builder = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBufSize)
                .setTransferMode(AudioTrack.MODE_STREAM)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && context != null) {
                val audioContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    context.createAttributionContext("default")
                } else {
                    context
                }
                builder.setContext(audioContext)
            }

            val track = builder.build()

            sfxTrack = track

            while (isActive) {
                try {
                    val samples = sfxQueue.poll()
                    if (samples != null && isSoundEnabled) {
                        if (track.playState != AudioTrack.PLAYSTATE_PLAYING) {
                            track.play()
                        }
                        track.write(samples, 0, samples.size)
                    } else {
                        delay(20)
                    }
                } catch (e: Exception) {
                    delay(50)
                }
            }
            try {
                track.stop()
                track.release()
            } catch (_: Exception) {}
        }
    }

    fun startMusicLoop() {
        if (!isMusicEnabled || musicJob?.isActive == true) return

        musicJob = scope.launch {
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(2048)

            val builder = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBufSize)
                .setTransferMode(AudioTrack.MODE_STREAM)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && context != null) {
                val audioContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    context.createAttributionContext("default")
                } else {
                    context
                }
                builder.setContext(audioContext)
            }

            val track = builder.build()

            musicTrack = track
            track.play()

            val pentatonicScale = doubleArrayOf(
                261.63, 293.66, 329.63, 392.00, 440.00,
                523.25, 587.33, 659.25, 783.99, 880.00
            )

            val melodyPattern = intArrayOf(
                0, 2, 4, 7,  5, 3, 2, 0,
                2, 4, 6, 8,  7, 5, 4, 2,
                4, 7, 9, 7,  4, 2, 0, 2
            )

            var step = 0

            while (isActive && isMusicEnabled) {
                try {
                    val noteIdx = melodyPattern[step % melodyPattern.size]
                    val freq = pentatonicScale[noteIdx]

                    val durationMs = 280
                    val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                    val samples = ShortArray(numSamples)

                    for (i in 0 until numSamples) {
                        val progress = i.toDouble() / numSamples
                        val env = sin(PI * progress) * (1.0 - progress * 0.7)
                        val mainWave = sin(2.0 * PI * i * freq / sampleRate)
                        val subHarmonic = sin(2.0 * PI * i * (freq * 0.5) / sampleRate) * 0.25
                        val shimmer = sin(2.0 * PI * i * (freq * 2.0) / sampleRate) * 0.15

                        val value = ((mainWave + subHarmonic + shimmer) * 2200 * env).toInt()
                        samples[i] = value.coerceIn(-32768, 32767).toShort()
                    }

                    if (isMusicEnabled) {
                        track.write(samples, 0, samples.size)
                    }

                    step++
                    delay(380)
                } catch (e: Exception) {
                    delay(500)
                }
            }

            try {
                track.stop()
                track.release()
            } catch (_: Exception) {}
        }
    }

    fun stopMusicLoop() {
        musicJob?.cancel()
        musicJob = null
        try {
            musicTrack?.stop()
            musicTrack?.release()
        } catch (_: Exception) {}
        musicTrack = null
    }

    private fun queueSfx(samples: ShortArray) {
        if (!isSoundEnabled) return
        if (sfxQueue.size < 12) {
            sfxQueue.add(samples)
        }
    }

    fun playDiceRollSound() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val durationMs = 320
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val samples = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val progress = i.toFloat() / numSamples
                    val freq = 140.0 + sin(i * 0.08) * 90.0 + Random.nextDouble(-30.0, 30.0)
                    val envelope = (1.0 - progress) * (if (i % 180 < 90) 1.0 else 0.25)
                    val value = (sin(2.0 * PI * i * freq / sampleRate) * 17000 * envelope).toInt()
                    samples[i] = value.coerceIn(-32768, 32767).toShort()
                }
                queueSfx(samples)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playTokenStepSound() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val durationMs = 85
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val samples = ShortArray(numSamples)
                val freq = 560.0 + Random.nextDouble(-20.0, 40.0)
                for (i in 0 until numSamples) {
                    val envelope = 1.0 - (i.toDouble() / numSamples)
                    val value = (sin(2.0 * PI * i * freq / sampleRate) * 22000 * envelope * envelope).toInt()
                    samples[i] = value.coerceIn(-32768, 32767).toShort()
                }
                queueSfx(samples)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playCaptureSound() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val durationMs = 400
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val samples = ShortArray(numSamples)
                val random = java.util.Random()

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples

                    val boomFreq = 340.0 * (1.0 - progress) + 45.0
                    val boomEnv = Math.pow(1.0 - progress, 1.8)
                    val boomWave = sin(2.0 * PI * i * boomFreq / sampleRate)

                    val noiseEnv = if (progress < 0.12) (1.0 - progress / 0.12) else 0.0
                    val noiseWave = (random.nextFloat() * 2.0 - 1.0) * noiseEnv

                    val chimeFreq = 880.0 + (progress * 880.0)
                    val chimeEnv = sin(PI * progress)
                    val chimeWave = sin(2.0 * PI * i * chimeFreq / sampleRate) * chimeEnv * 0.45

                    val combined = (boomWave * boomEnv * 22000) + (noiseWave * 14000) + (chimeWave * 11000)
                    samples[i] = combined.toInt().coerceIn(-32768, 32767).toShort()
                }
                queueSfx(samples)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playGoalSound() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val notes = doubleArrayOf(523.25, 659.25, 783.99)
                val noteDurationMs = 120
                val noteSamples = (sampleRate * (noteDurationMs / 1000.0)).toInt()
                val totalSamples = noteSamples * notes.size
                val samples = ShortArray(totalSamples)

                for (n in notes.indices) {
                    val freq = notes[n]
                    val startIdx = n * noteSamples
                    for (i in 0 until noteSamples) {
                        val env = 1.0 - (i.toDouble() / noteSamples)
                        val valInt = (sin(2.0 * PI * i * freq / sampleRate) * 22000 * env).toInt()
                        samples[startIdx + i] = valInt.coerceIn(-32768, 32767).toShort()
                    }
                }
                queueSfx(samples)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playVictorySound() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50, 1318.51, 1567.98)
                val noteDurations = intArrayOf(140, 140, 140, 220, 220, 500)

                var totalDurationMs = 0
                for (d in noteDurations) totalDurationMs += d

                val numSamples = (sampleRate * (totalDurationMs / 1000.0)).toInt()
                val samples = ShortArray(numSamples)

                var currentSampleIdx = 0
                for (n in notes.indices) {
                    val freq = notes[n]
                    val durationMs = noteDurations[n]
                    val currentNoteSamples = (sampleRate * (durationMs / 1000.0)).toInt()

                    for (i in 0 until currentNoteSamples) {
                        if (currentSampleIdx >= numSamples) break
                        val progress = i.toDouble() / currentNoteSamples
                        val env = Math.pow(1.0 - progress, 1.2)

                        val tone1 = sin(2.0 * PI * i * freq / sampleRate)
                        val tone2 = sin(2.0 * PI * i * (freq * 2.0) / sampleRate) * 0.25
                        val combined = (tone1 + tone2) * 24000 * env

                        samples[currentSampleIdx] = combined.toInt().coerceIn(-32768, 32767).toShort()
                        currentSampleIdx++
                    }
                }
                queueSfx(samples)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playThemeChangeSound() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val notes = doubleArrayOf(659.25, 783.99, 987.77, 1318.51)
                val noteDurationMs = 70
                val noteSamples = (sampleRate * (noteDurationMs / 1000.0)).toInt()
                val totalSamples = noteSamples * notes.size
                val samples = ShortArray(totalSamples)

                for (n in notes.indices) {
                    val freq = notes[n]
                    val startIdx = n * noteSamples
                    for (i in 0 until noteSamples) {
                        val env = 1.0 - (i.toDouble() / noteSamples)
                        val valInt = (sin(2.0 * PI * i * freq / sampleRate) * 18000 * env).toInt()
                        samples[startIdx + i] = valInt.coerceIn(-32768, 32767).toShort()
                    }
                }
                queueSfx(samples)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playButtonClickSound() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val durationMs = 45
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val samples = ShortArray(numSamples)
                val freq = 480.0
                for (i in 0 until numSamples) {
                    val env = 1.0 - (i.toDouble() / numSamples)
                    val value = (sin(2.0 * PI * i * freq / sampleRate) * 14000 * env * env).toInt()
                    samples[i] = value.coerceIn(-32768, 32767).toShort()
                }
                queueSfx(samples)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

