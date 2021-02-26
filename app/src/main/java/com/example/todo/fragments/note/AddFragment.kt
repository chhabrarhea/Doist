package com.example.todo.fragments.note


import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.view.*
import android.widget.EditText
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
import com.example.todo.fragments.MediaPlayerLifeCycle
import com.example.todo.fragments.SharedViewModel
import java.lang.reflect.Method


class AddFragment : Fragment(){
    private lateinit var view: FragmentAddBinding
    private val todoViewModel: TodoViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by viewModels()
    private var noteUrl=""
    private lateinit var mediaPlayerLifeCycle: MediaPlayerLifeCycle


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

        mediaPlayerLifeCycle= MediaPlayerLifeCycle(view.mediaPlayer,requireContext(),"")
        if(mediaPlayerLifeCycle.audioFilePath!="" && SharedViewModel.audioRecorded.value=="")
            sharedViewModel.setRecordAudio("")
        SharedViewModel.audioRecorded.observe(viewLifecycleOwner, {
            if(it!="") mediaPlayerLifeCycle.initializeMediaPlayer(it)})

        view.deleteImage.setOnClickListener { removeImage() }
        view.deleteUrl.setOnClickListener { removeUrl() }
        view.deleteAudio.setOnClickListener { removeMediaPlayer() }
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
                val uri = data.data!!
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
                sharedViewModel.mCurrentPhotoPath,mediaPlayerLifeCycle.audioFilePath,
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
        mediaPlayerLifeCycle.removeMediaPlayer()
        sharedViewModel.setRecordAudio("") }

    override fun onStop() {
        super.onStop()
        mediaPlayerLifeCycle.stopMediaPlayer()
         sharedViewModel.setRecordAudio("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayerLifeCycle.destroyMediaPlayer()
    }

}