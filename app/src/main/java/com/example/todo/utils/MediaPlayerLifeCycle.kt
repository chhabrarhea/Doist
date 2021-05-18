package com.example.todo.utils

import android.content.Context
import android.graphics.drawable.Animatable
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.todo.R
import com.example.todo.databinding.MediaPlayerBinding
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.TimeUnit

class MediaPlayerLifeCycle(
    val view: MediaPlayerBinding,
    val context: Context,
    var audioFilePath: String
) : SeekBar.OnSeekBarChangeListener, MediaPlayer.OnCompletionListener {

    private val seekBar: SeekBar = view.seekBar
    private val tvDue: TextView = view.tvDue
    private val tvPass: TextView = view.tvPass
    private var runnable: Runnable? = null
    private var handler: Handler = Handler(Looper.getMainLooper())
    private val playButton: ImageButton = view.playButton
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
        seekBar.max = mediaPlayer!!.duration/1000
        tvDue.text = getFormattedDuration(mediaPlayer!!.duration.toLong())
        runnable = Runnable {
            seekBar.progress = mediaPlayer!!.currentPosition / 1000
            tvPass.text = getFormattedDuration(mediaPlayer!!.currentPosition.toLong())
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
        view.root.visibility = View.VISIBLE
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
        view.root.visibility = View.GONE
        audioFilePath = ""
    }

    private fun getFormattedDuration(ms:Long):String{
        var milliseconds = ms
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)

        return if(hours>0){
            "${if(hours < 10) "0" else ""}$hours:" +
                    "${if(minutes < 10) "0" else ""}$minutes:" +
                    "${if(seconds < 10) "0" else ""}$seconds"
        } else
            "${if(minutes < 10) "0" else ""}$minutes:" +
                    "${if(seconds < 10) "0" else ""}$seconds"

    }
}