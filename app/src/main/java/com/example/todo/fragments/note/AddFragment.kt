package com.example.todo.fragments.note


import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.ImageView
import android.widget.Toast
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
import com.example.todo.utils.MediaPlayerLifeCycle
import com.example.todo.fragments.SharedViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.lang.reflect.Method


class AddFragment : Fragment(){
    private lateinit var view: FragmentAddBinding
    private val todoViewModel: TodoViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by viewModels()
    private lateinit var mediaPlayerLifeCycle: MediaPlayerLifeCycle
    private var canvasPath:String=""


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
            R.drawable.ic_baseline_attach_file_24
        )

        view.prioritiesSpinner.onSpinnerItemSelectedListener = sharedViewModel.initializeSpinner(
            requireContext(),
            view.priorityIndicator
        )

        mediaPlayerLifeCycle= MediaPlayerLifeCycle(view.mediaPlayer, requireContext())
        if(mediaPlayerLifeCycle.audioFilePath!="" && SharedViewModel.audioRecorded.value=="")
            sharedViewModel.setRecordAudio("")

        SharedViewModel.audioRecorded.observe(viewLifecycleOwner, {
            if (it != "") mediaPlayerLifeCycle.setDataSource(it)
        })

        SharedViewModel.canvasImage.observe(viewLifecycleOwner, {
            if (it != "") {
                canvasPath = it
                setImage(it, view.canvasImage, view.deleteCanvas)
            }
        })

        view.deleteImage.setOnClickListener { removeImage() }
        view.deleteUrl.setOnClickListener { removeUrl() }
        view.mediaPlayer.deleteAudio.setOnClickListener { removeMediaPlayer() }
        view.deleteCanvas.setOnClickListener { removeCanvasImage() }
        view.reminderLayout.setOnClickListener {inflateCancelReminderDialog()}
        return view.root
    }

    //sets Image in imageView using Glide
    private fun setImage(it: String?, image: ImageView, button: FloatingActionButton) {
        Glide.with(requireContext()).load(it).into(image)
        image.visibility=View.VISIBLE
        button.visibility=View.VISIBLE
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
                sharedViewModel.inflateRecordDialog(requireContext()).show()
                }
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

    private fun addImage() {
        if(!sharedViewModel.allPermissionsGrantedForImage(requireContext())){
               requestPermissions(
                   sharedViewModel.requiredPermissionsForImage,
                   sharedViewModel.requestCodeForImagePermissions)
        }
        else{
            val dialog=AlertDialog.Builder(requireContext())
            dialog.setTitle("Add Image").setMessage("Capture from Camera or Upload from Gallery")
            dialog.setNegativeButton("Gallery") { _, _ -> openGallery()}
            dialog.setPositiveButton("Camera"){ _, _ -> openCamera()}
            dialog.create().show()
        }
    }
    private fun openCamera() {
        val intent=sharedViewModel.openCamera(requireActivity(), requireContext())
        if(intent==null)
            Toast.makeText(requireContext(), "Some error occurred!", Toast.LENGTH_SHORT).show()
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
               setImage(sharedViewModel.mCurrentPhotoPath, view.image, view.deleteImage)
            }
            else if(requestCode==sharedViewModel.fromGallery && data!=null){
                val path=sharedViewModel.getRealPathFromURI(data.data!!, requireActivity())
                if(path!=null){
                    sharedViewModel.mCurrentPhotoPath=path
                    setImage(sharedViewModel.mCurrentPhotoPath, view.image, view.deleteImage)
                }
            }
            else if(requestCode==77 && data!=null){
                val uri: Uri =data.data!!
                val path: String? = sharedViewModel.getRealPathFromURI(uri, requireActivity())
                if(path!=null){
                    sharedViewModel.setRecordAudio(path)
                }
                else{
                    Toast.makeText(
                        requireContext(),
                        "Some error occured, try again!",
                        Toast.LENGTH_SHORT
                    ).show()
                }}}
    }

    private fun insertData() {

        val title = view.titleEt.text.toString()
        val priority = view.prioritiesSpinner.selectedIndex
        val desc = view.descriptionEt.text.toString()

        if (sharedViewModel.validateData(title, desc) && (sharedViewModel.date==null || sharedViewModel.date!!.timeInMillis>System.currentTimeMillis())) {
            val newData = ToDoData(
                0,
                title,
                sharedViewModel.parsePriorityById(priority),
                desc,
                view.timeText.text.toString(),
                sharedViewModel.mCurrentPhotoPath, mediaPlayerLifeCycle.audioFilePath,
                view.urlText.text.toString(), null, canvasPath,sharedViewModel.dateString
            )
                todoViewModel.insertData(newData,requireContext(),sharedViewModel.date)
                sharedViewModel.deinitializeSharedVariables()
            Toast.makeText(requireContext(), "Added Successfully!", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_addFragment_to_listFragment)
        }else if (sharedViewModel.date!=null && sharedViewModel.date!!.timeInMillis<System.currentTimeMillis())
            Toast.makeText(requireContext(), "Please enter a later time for reminder!", Toast.LENGTH_LONG).show()
        else {
            Toast.makeText(requireContext(), "Please fill all fields!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if (menu.javaClass.simpleName == "MenuBuilder") {
            try {
                val m: Method = menu.javaClass.getDeclaredMethod(
                    "setOptionalIconsVisible",
                    java.lang.Boolean.TYPE
                )
                m.isAccessible = true
                m.invoke(menu, true)
            } catch (e: Exception) {}
        }
        super.onPrepareOptionsMenu(menu)
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_fragment_menu, menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_add -> insertData()
            R.id.menu_add_image -> addImage()
            R.id.add_url -> sharedViewModel.urlDialog(requireContext(), layoutInflater, view.url).show()
            R.id.menu_add_vn -> addVn()
            R.id.canvas -> findNavController().navigate(R.id.action_addFragment_to_drawFragment)
            R.id.reminder -> sharedViewModel.setReminder(requireContext(),view.reminderLayout)
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            sharedViewModel.requestCodeForImagePermissions -> {
                if (sharedViewModel.allPermissionsGrantedForImage(requireContext())) {
                    addImage()
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
                    sharedViewModel.inflateRecordDialog(requireContext()).show()
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
       view.urlText.text=""
       view.url.visibility=View.VISIBLE
    }
    private fun removeMediaPlayer(){
        mediaPlayerLifeCycle.removeMediaPlayer()
        sharedViewModel.setRecordAudio("") }
    private fun removeCanvasImage(){
        sharedViewModel.setCanvasImage("")
        view.canvasImage.visibility=View.GONE
        view.deleteCanvas.visibility=View.GONE
    }
    private fun inflateCancelReminderDialog() {
        sharedViewModel.deleteReminderDialog(requireContext(),view.reminderLayout)
    }

    override fun onStop() {
        super.onStop()
        mediaPlayerLifeCycle.stopMediaPlayer()
         sharedViewModel.setRecordAudio("")
    }
    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayerLifeCycle.destroyMediaPlayer()
    }

    override fun onDetach() {
        super.onDetach()
        sharedViewModel.deinitializeSharedVariables()
    }
}