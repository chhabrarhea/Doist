package com.example.todo.fragments.add


import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.drawable.Animatable
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.todo.R
import com.example.todo.data.TodoViewModel
import com.example.todo.data.models.ToDoData
import com.example.todo.databinding.FragmentAddBinding
import com.example.todo.fragments.SharedViewModel
import java.io.File
import java.io.FileInputStream
import java.lang.reflect.Method
import java.util.*


class AddFragment : Fragment(),SeekBar.OnSeekBarChangeListener, MediaPlayer.OnCompletionListener{
    private lateinit var view: FragmentAddBinding
    private val todoViewModel: TodoViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by viewModels()
    private  var mediaPlayer:MediaPlayer?=null
    private var isStarted=false
    private var pause=false
    private var noteUrl=""
    private lateinit var anim: Animatable
    private  var runnable: Runnable?=null
    private var handler: Handler = Handler(Looper.getMainLooper())
    private var audioFilePath=""


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        view = FragmentAddBinding.inflate(inflater, container, false)


        setHasOptionsMenu(true)
        (activity as AppCompatActivity?)!!.setSupportActionBar(view.toolbar)
        view.toolbar.overflowIcon= ContextCompat.getDrawable(
            requireContext(),
            R.drawable.ic_baseline_attach_file_24)
        view.priorityIndicator.setCardBackgroundColor(ContextCompat.getColor(requireContext(),R.color.high))
        view.prioritiesSpinner.onSpinnerItemSelectedListener = sharedViewModel.initializeSpinner(requireContext(),view.priorityIndicator)

        if(audioFilePath!="" && SharedViewModel.audioRecorded.value=="")
            sharedViewModel.setRecordAudio("")
        mediaPlayer=MediaPlayer()
        mediaPlayer?.setOnCompletionListener(this)
        view.playButton.setOnClickListener { playAndPauseAudio() }
        SharedViewModel.audioRecorded.observe(viewLifecycleOwner, {
            if(it!=""){
            audioFilePath = it
            view.mediaPlayer.visibility = View.VISIBLE
            isStarted = false
            if (mediaPlayer==null){
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
            }} }})

        view.deleteImage.setOnClickListener { removeImage() }
        view.deleteUrl.setOnClickListener { removeUrl() }
        view.deleteAudio.setOnClickListener { removeMediaPlayer() }
        view.seekBar.setOnSeekBarChangeListener(this)
        return view.root

    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_add -> insertData()
            R.id.menu_add_image -> addImage()
            R.id.add_url -> addURL()
            R.id.menu_add_vn -> addVn()
            R.id.canvas->{
                findNavController().navigate(R.id.action_addFragment_to_drawFragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addVn() {
         val alert=AlertDialog.Builder(requireContext())
        alert.setTitle("Add Voice Note").setMessage("Choose an audio file or Record audio")
            .setNegativeButton("Record"){ _, _->run{
                if(!sharedViewModel.allPermissionsGrantedForMic(requireContext())){
                    requestPermissions(
                        sharedViewModel.requiredPermissionForAudioRecord,
                        sharedViewModel.micRequestCode
                    )
                }else{
                findNavController().navigate(R.id.action_addFragment_to_recordAudioFragment)}
            }}
            .setPositiveButton("Upload"){ _, _->run{
                if (sharedViewModel.allPermissionsGrantedForAudioPicker(requireContext())){
                val intent=Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, 77)}
                else{
                    requestPermissions(
                        sharedViewModel.requiredPermissionForAudioPicker,
                        sharedViewModel.audioDirectoryRequestCode
                    )
                }
            }}
        alert.create().show()

    }

    private fun addURL() {
        val dialogView=layoutInflater.inflate(R.layout.url_dialog, null, false)
        val edit=dialogView.findViewById(R.id.url_edittext) as EditText
        val builder= AlertDialog.Builder(requireContext())
        builder.setView(dialogView).setPositiveButton("Done"){ _, _->run{}}.setNegativeButton("Cancel"){ dialog, _->run{
            dialog.dismiss()
        }}
        val alert= builder.create()
        alert.show()
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener{
            noteUrl=edit.text.toString()
            if(Patterns.WEB_URL.matcher(noteUrl).matches()){
                noteUrl=edit.text.toString()
                view.urlText.visibility=View.VISIBLE
                view.urlText.text=edit.text.toString()
                view.deleteUrl.visibility=View.VISIBLE
                alert.dismiss()
            }else{
                noteUrl=""
                edit.error="Enter valid URL!"
            }
        }
        Log.i("sjd", "$noteUrl,")
    }

    private fun addImage() {
        if(!sharedViewModel.allPermissionsGrantedForImage(requireContext())){
               requestPermissions(
                   sharedViewModel.requiredPermissionsForImage,
                   sharedViewModel.requestCodeForImagePermissions
               ) }
        else{
            dialogForImage()
        }
    }

    private fun dialogForImage(){
        val dialog=AlertDialog.Builder(requireContext())
        dialog.setTitle("Add Image").setMessage("Capture from Camera or Upload from Gallery")
        dialog.setNegativeButton("Gallery") { _, _ -> openGallery()}
        dialog.setPositiveButton("Camera"){ _, _ -> openCamera()}
        dialog.create().show()
    }

    private fun openCamera() {
        val intent=sharedViewModel.openCamera(requireActivity(), requireContext())
        if(intent==null)
            Toast.makeText(requireContext(), "Some error occured!", Toast.LENGTH_SHORT).show()
        else
            startActivityForResult(intent, sharedViewModel.fromCamera)
    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, sharedViewModel.fromGallery)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode==RESULT_OK){
            if (requestCode==sharedViewModel.fromCamera){
                val uri = Uri.parse(sharedViewModel.mCurrentPhotoPath)
                Glide.with(requireContext()).load(uri.toString()).override(500, 500).into(view.image)
                view.image.visibility=View.VISIBLE
                view.deleteImage.visibility=View.VISIBLE
            }
            else if(requestCode==sharedViewModel.fromGallery && data!=null){
                val uri = data.getData()!!
                Glide.with(requireContext()).load(uri.toString()).into(view.image)
                view.image.visibility=View.VISIBLE
                view.deleteImage.visibility=View.VISIBLE
                val path=sharedViewModel.getRealPathFromURI(uri, requireActivity())
                if(path!=null){
                    sharedViewModel.mCurrentPhotoPath=path
                }
            }
            else if(requestCode==77 && data!=null){
                val uri: Uri =data.data!!
                val path: String? = sharedViewModel.getRealPathFromURI(uri,requireActivity())
                if(path!=null){
                    sharedViewModel.setRecordAudio(path)
                }
                else{
                    Toast.makeText(requireContext(),"Some error occured, try again!",Toast.LENGTH_SHORT).show()
                }}}
    }

    private fun insertData() {
        val title = view.titleEt.text.toString()
        val priority = view.prioritiesSpinner.selectedIndex
        val desc = view.descriptionEt.text.toString()

        if (sharedViewModel.validateData(title, desc)) {
            val newData = ToDoData(
                0,
                title,
                sharedViewModel.parsePriorityById(priority),
                desc,
                view.timeText.text.toString(),
                sharedViewModel.mCurrentPhotoPath, audioFilePath,
                noteUrl,null
            )
            todoViewModel.insertData(newData,requireContext())
            sharedViewModel.setRecordAudio("")
            Toast.makeText(requireContext(), "Added Successfully!", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_addFragment_to_listFragment)
        } else {
            Toast.makeText(requireContext(), "Please fill all fields!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if (menu.javaClass.simpleName == "MenuBuilder") {
            try {

                val m: Method = menu.javaClass.getDeclaredMethod(
                    "setOptionalIconsVisible", java.lang.Boolean.TYPE
                )
                m.isAccessible = true
                m.invoke(menu, true)
            } catch (e: Exception) {
                Log.e(
                    javaClass.simpleName,
                    "onMenuOpened...unable to set icons for overflow menu",
                    e
                )
            }
        }
        super.onPrepareOptionsMenu(menu)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            sharedViewModel.requestCodeForImagePermissions -> {
                if (sharedViewModel.allPermissionsGrantedForImage(requireContext())) {
                    dialogForImage()
                } else {
                    Toast.makeText(activity, "Permissions not granted!", Toast.LENGTH_SHORT).show()
                }
            }
            sharedViewModel.audioDirectoryRequestCode -> {
                if (sharedViewModel.allPermissionsGrantedForAudioPicker(requireContext())) {
                    val intent = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    )
                    startActivityForResult(intent, 77)
                } else {
                    Toast.makeText(activity, "Permissions not granted!", Toast.LENGTH_SHORT).show()
                }
            }
            sharedViewModel.micRequestCode -> {
                if (sharedViewModel.allPermissionsGrantedForMic(requireContext())) {
                    findNavController().navigate(R.id.action_addFragment_to_recordAudioFragment)
                } else {
                    Toast.makeText(activity, "Permissions not granted!", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun removeImage(){
        view.deleteImage.visibility=View.GONE
        view.image.visibility=View.GONE
        sharedViewModel.mCurrentPhotoPath=""
    }
    private fun removeUrl(){
        noteUrl=""
        view.deleteUrl.visibility=View.GONE
        view.urlText.visibility=View.GONE
    }
    private fun removeMediaPlayer(){
        view.mediaPlayer.visibility=View.GONE
        sharedViewModel.setRecordAudio("")
        mediaPlayer=null
    }

    override fun onStop() {
        super.onStop()
        if(mediaPlayer!=null) {
            if(mediaPlayer!!.isPlaying()){
                mediaPlayer!!.pause()
               view.playButton.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.avd_pause_to_play))
                anim=view.playButton.drawable as Animatable
                anim.start()
                pause=true
            } }
         sharedViewModel.setRecordAudio("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if(mediaPlayer!=null){
            if(runnable!=null)
            handler.removeCallbacks(runnable!!)
            mediaPlayer!!.reset()
            mediaPlayer!!.release()
            mediaPlayer=null}
    }

    private fun playAndPauseAudio() {
        if (!isStarted) {
            if (mediaPlayer==null)
                return
            val file = File(audioFilePath)
            file.setReadable(true, false)
            val inputStream = FileInputStream(file)
            mediaPlayer!!.setDataSource(inputStream.getFD())
            inputStream.close()
            mediaPlayer!!.prepare()
            mediaPlayer!!.setOnPreparedListener { mp: MediaPlayer ->
                initializeSeekBar()
                anim=view.playButton.drawable as Animatable
                anim.start()
                isStarted = true
                mediaPlayer?.start()
            }
        }
        //Pause playing audio
        else if (mediaPlayer!!.isPlaying) {
            mediaPlayer?.pause()
            pause = true
            view.playButton.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.avd_pause_to_play))
            anim=view.playButton.drawable as Animatable
            anim.start()

        }
        //Play paused audio
        else  {
            view.playButton.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.avd_play_to_pause))
            anim=view.playButton.drawable as Animatable
            anim.start()
            initializeSeekBar()
            mediaPlayer?.start()
        }
    }


    private fun initializeSeekBar() {
        if (mediaPlayer==null)
            return
        view.seekBar.max = mediaPlayer!!.duration/1000
        view.tvDue.text = "${view.seekBar.max} secs"
        runnable = Runnable {
            view.seekBar.progress = mediaPlayer!!.currentPosition/1000
            view.tvPass.text = "${mediaPlayer!!.currentPosition/1000} secs"
            handler.postDelayed(runnable!!, 1000)
        }
        handler.postDelayed(runnable!!, 1000)
    }

    //automatically starts from beginning if start() is called
    override fun onCompletion(p0: MediaPlayer) {
        view.playButton.isEnabled = true
        pause=true
        view.playButton.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_play
            )
        )
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


}