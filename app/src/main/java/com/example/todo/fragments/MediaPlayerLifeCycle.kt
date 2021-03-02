package com.example.todo.fragments

import android.content.Context
import android.graphics.drawable.Animatable
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.todo.R
import java.io.File
import java.io.FileInputStream

class MediaPlayerLifeCycle(
    val view: RelativeLayout,
    val context: Context,
    var audioFilePath: String
) : SeekBar.OnSeekBarChangeListener, MediaPlayer.OnCompletionListener {

    private val seekBar: SeekBar = view.findViewById(R.id.seek_bar)
    private val tvDue: TextView = view.findViewById(R.id.tv_due)
    private val tvPass: TextView = view.findViewById(R.id.tv_pass)
    private var runnable: Runnable? = null
    private var handler: Handler = Handler(Looper.getMainLooper())
    private val playButton: ImageButton = view.findViewById(R.id.playButton)
    private var mediaPlayer: MediaPlayer?
    private var pause = false
    private var isStarted = false
    private lateinit var anim: Animatable


    init {
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setOnCompletionListener(this)
        playButton.setOnClickListener {playAndPauseAudio()}
    }


    private fun initializeSeekBar() {
        if (mediaPlayer == null)
            return
        seekBar.max = mediaPlayer!!.duration / 1000
        tvDue.text = "${seekBar.max} secs"
        runnable = Runnable {
            seekBar.progress = mediaPlayer!!.currentPosition / 1000
            tvPass.text = "${mediaPlayer!!.currentPosition / 1000} secs"
            handler.postDelayed(runnable!!, 1000)
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun playAndPauseAudio() {
        if (!isStarted) {
            if (mediaPlayer == null)
                return
            val file = File(audioFilePath)
            file.setReadable(true, false)
            val inputStream = FileInputStream(file)
            mediaPlayer!!.setDataSource(inputStream.fd)
            inputStream.close()
            mediaPlayer!!.prepare()
            mediaPlayer!!.setOnPreparedListener {
                initializeSeekBar()
                anim = playButton.drawable as Animatable
                anim.start()
                isStarted = true
                mediaPlayer?.start()
            }
        }
        //Pause playing audio
        else if (mediaPlayer!!.isPlaying) {
            mediaPlayer?.pause()
            pause = true
            playButton.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.avd_pause_to_play
                )
            )
            anim = playButton.drawable as Animatable
            anim.start()

        }
        //Play paused audio
        else {
            playButton.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.avd_play_to_pause
                )
            )
            anim = playButton.drawable as Animatable
            anim.start()
            initializeSeekBar()
            mediaPlayer?.start()
        }
    }

    override fun onCompletion(p0: MediaPlayer) {
        playButton.isEnabled = true
        pause = true
        playButton.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.avd_pause_to_play
            )
        )
        anim = playButton.drawable as Animatable
        anim.start()
    }


    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
        if (p2) {
            mediaPlayer?.seekTo(p1 * 1000)
        }
    }

    override fun onStartTrackingTouch(p0: SeekBar?) {

    }

    override fun onStopTrackingTouch(p0: SeekBar?) {

    }

    fun initializeMediaPlayer(file: String) {
        audioFilePath = file
        isStarted = false
        view.visibility = View.VISIBLE
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
            }
            mediaPlayer?.setOnCompletionListener(this)
        }
    }

    fun stopMediaPlayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer!!.isPlaying) {
                mediaPlayer!!.pause()
                playButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.avd_pause_to_play
                    )
                )
                anim = playButton.drawable as Animatable
                anim.start()
                pause = true
            }
        }
    }

    fun destroyMediaPlayer() {
        if (mediaPlayer != null) {
            if (runnable != null)
                handler.removeCallbacks(runnable!!)
            mediaPlayer!!.reset()
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }

    fun removeMediaPlayer() {
        handler.removeCallbacks(runnable!!)
        mediaPlayer = null
        view.visibility = View.GONE
        audioFilePath = ""
    }
}