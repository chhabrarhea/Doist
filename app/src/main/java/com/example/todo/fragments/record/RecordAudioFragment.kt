package com.example.todo.fragments.record

import android.content.pm.PackageManager
import android.graphics.drawable.Animatable
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import android.view.*
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.todo.R
import com.example.todo.databinding.FragmentRecordAudioBinding
import com.example.todo.fragments.SharedViewModel
import com.github.squti.androidwaverecorder.WaveRecorder
import kotlinx.coroutines.NonCancellable.start
import org.angmarch.views.NiceSpinner
import org.angmarch.views.OnSpinnerItemSelectedListener
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*


class RecordAudioFragment : Fragment(),MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener{
    private var isRecording = false
    private val sharedViewModel: SharedViewModel by viewModels()
    private var isStarted = false
    lateinit var binding: FragmentRecordAudioBinding
    lateinit var recorder: WaveRecorder
    private var audioFilePath = ""
    private lateinit var file: File
    private var mediaPlayer : MediaPlayer?= MediaPlayer()
    private var runnable: Runnable?=null
    private var handler: Handler = Handler(Looper.getMainLooper())
    private var pause: Boolean = false
    private lateinit var anim:Animatable
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecordAudioBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        if (!hasMicrophone()) {
            binding.stopButton.isEnabled = false
            binding.recordButton.isEnabled = false;
            Toast.makeText(requireContext(), "No microphone found!", Toast.LENGTH_SHORT).show();
        } else {
            binding.stopButton.setEnabled(false)
            file = File(requireContext().getExternalFilesDir(null)?.absolutePath, "DoIt")
            if (!file.exists()) file.mkdir()
            audioFilePath =
                file.path + "/audio" + Calendar.getInstance().time + ".wav"
            mediaPlayer!!.setOnCompletionListener(this) }
        binding.seekBar.setOnSeekBarChangeListener(this)
        binding.recordButton.setOnClickListener { recordAudio() }
        binding.stopButton.setOnClickListener { stopRecording() }
        binding.playButton.setOnClickListener { playAndPauseAudio() }
        binding.stopBtn.setOnClickListener { stopAudio() }
        setHasOptionsMenu(true)
        (activity as AppCompatActivity?)!!.setSupportActionBar(binding.toolbar)
        return binding.root
    }

    private fun stopAudio() {
        if(mediaPlayer!=null && (mediaPlayer!!.isPlaying || pause.equals(true))){
            Log.i("stopAudio", "called")
            pause = true
            mediaPlayer!!.stop()
            mediaPlayer!!.reset()
            isStarted=false
            handler.removeCallbacks(runnable!!)
            binding.playButton.isEnabled = true
            binding.playButton.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.avd_pause_to_play
                )
            )
            binding.stopBtn.isEnabled = false
            binding.tvPass.text = "0 secs"
            binding.tvDue.text = "${binding.seekBar.max} secs"
            binding.seekBar.progress=0
        }
    }


    private fun hasMicrophone(): Boolean {
        val packageManager: PackageManager = requireActivity().packageManager
        return packageManager.hasSystemFeature(
            PackageManager.FEATURE_MICROPHONE
        )
    }

    @Throws(IOException::class)
    fun recordAudio() {
        if(mediaPlayer!=null && (mediaPlayer!!.isPlaying || pause)){
            mediaPlayer!!.stop()
            mediaPlayer!!.reset()
            isStarted=false
            if (mediaPlayer!!.isPlaying){
                pause=true
            binding.playButton.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.avd_pause_to_play
                )
            )}
            anim=binding.playButton.drawable as Animatable
            anim.start()
            binding.playButton.isEnabled=false
            binding.stopBtn.isEnabled=false
            binding.seekBar.progress=0
            binding.tvDue.text=""
            binding.tvPass.text=""
            binding.seekBar.isEnabled=false
            if (runnable!=null)
            handler.removeCallbacks(runnable!!)
        }
        isRecording = true
        binding.stopButton.isEnabled = true
        binding.recordButton.isEnabled = false
        try {
            binding.timer.base = SystemClock.elapsedRealtime()
            recorder = WaveRecorder(audioFilePath)
            recorder.startRecording()
            binding.timer.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        if (isRecording) {
            Log.i("stopRecording", "called")
            binding.timer.stop()
            recorder.stopRecording()
            binding.recordButton.isEnabled = true
            isRecording = false
            binding.mediaPlayer.visibility=View.VISIBLE
            binding.stopButton.isEnabled = false
            binding.playButton.isEnabled=true
            binding.seekBar.isEnabled=true
            binding.stopBtn.isEnabled=true

        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.record_audio_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_save) {
           sharedViewModel.setRecordAudio(audioFilePath)
            findNavController().popBackStack()

        }
        return super.onOptionsItemSelected(item)
    }




    private fun playAndPauseAudio() {
        //initialize mediaPlayer
//       mediaPlayer goes to idle state after calling reset
        if (mediaPlayer==null)
            return
        if (!isStarted) {
            val file = File(audioFilePath)
            file.setReadable(true, false)
            val inputStream = FileInputStream(file)
            mediaPlayer!!.setDataSource(inputStream.getFD())
            inputStream.close()
            mediaPlayer!!.prepare()
            mediaPlayer!!.setOnPreparedListener { mp: MediaPlayer? ->
                initializeSeekBar()
                mediaPlayer!!.start()
                anim=binding.playButton.drawable as Animatable
                anim.start()
                isStarted = true
                binding.stopBtn.isEnabled=true
            }
        }
        //Pause playing audio
        else if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.pause()
            pause = true
            binding.playButton.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.avd_pause_to_play))
            anim=binding.playButton.drawable as Animatable
            anim.start()
        }
        //Play paused audio
        else  {
            binding.playButton.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.avd_pause_to_play
                )
            )
            mediaPlayer!!.start()
            initializeSeekBar()
        }
    }


    private fun initializeSeekBar() {
        if (mediaPlayer==null)
            return
        binding.seekBar.max = mediaPlayer!!.duration/1000
        binding.tvDue.text = "${binding.seekBar.max} secs"
        runnable = Runnable {
            binding.seekBar.progress = mediaPlayer!!.currentPosition/1000
            binding.tvPass.text = "${mediaPlayer!!.currentPosition/1000} secs"

            handler.postDelayed(runnable!!, 1000)
        }
        handler.postDelayed(runnable!!, 1000)
    }

    //automatically starts from beginning if start() is called
    override fun onCompletion(p0: MediaPlayer?) {
        Log.i("onCompletion", "called")
        binding.playButton.isEnabled = true
        pause=true
        binding.playButton.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.avd_pause_to_play
            )
        )
    }

    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
        if (p2) {
            if (mediaPlayer!=null)
            mediaPlayer!!.seekTo(p1 * 1000)
        }
    }

    override fun onStartTrackingTouch(p0: SeekBar?) {

    }

    override fun onStopTrackingTouch(p0: SeekBar?) {

    }

    override fun onStop() {
        super.onStop()
        if(mediaPlayer!=null) {
            if(mediaPlayer!!.isPlaying() || pause){
                mediaPlayer!!.pause()
                binding.playButton.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.avd_pause_to_play))
                pause=true
            }
        }}

    override fun onDestroyView() {
        super.onDestroyView()
        if(mediaPlayer!=null){
            if(runnable!=null)
                handler.removeCallbacks(runnable!!)
            mediaPlayer!!.reset()
            mediaPlayer!!.release()
            mediaPlayer=null}
    }



}