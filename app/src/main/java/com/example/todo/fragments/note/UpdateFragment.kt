package com.example.todo.fragments.note

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.ImageView
import android.widget.RelativeLayout
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
import com.example.todo.databinding.FragmentUpdateBinding
import com.example.todo.utils.MediaPlayerLifeCycle
import com.example.todo.fragments.SharedViewModel
import java.lang.reflect.Method

class UpdateFragment : Fragment() {
    private lateinit var view: FragmentUpdateBinding
    private lateinit var args: ToDoData
    private val sharedViewModel by viewModels<SharedViewModel>()
    private val todoViewModel by viewModels<TodoViewModel>()
    private val fromGallery = 100
    private var noteUrl = ""
    private lateinit var uri: Uri
    private lateinit var mediaPlayerLifeCycle: MediaPlayerLifeCycle
    private var canvasPath=""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        view = FragmentUpdateBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        args = requireArguments().getParcelable("currentItem")!!
        view.lifecycleOwner = this
        view.args = args


        mediaPlayerLifeCycle = MediaPlayerLifeCycle(view.mediaPlayer, requireContext(), args.voicenote)
        if (SharedViewModel.audioRecorded.value == "" && mediaPlayerLifeCycle.audioFilePath != "")
            sharedViewModel.setRecordAudio(mediaPlayerLifeCycle.audioFilePath)
        SharedViewModel.audioRecorded.observe(viewLifecycleOwner, {
            if (it != "") mediaPlayerLifeCycle.setDataSource(it)
        })

        if(SharedViewModel.canvasImage.value=="" && args.canvasPath!="")
            sharedViewModel.setCanvasImage(args.canvasPath)
        SharedViewModel.canvasImage.observe(viewLifecycleOwner,{
            if(it!=""){
                canvasPath=it
                setImage(view.canvasRoot,it,view.canvas)
            } })

        sharedViewModel.mCurrentPhotoPath = args.image
        (activity as AppCompatActivity?)!!.setSupportActionBar(view.toolbar)
        view.toolbar.overflowIcon =
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_attach_file_24)

        view.deleteImage.setOnClickListener { removeImage() }
        view.deleteUrl.setOnClickListener { removeUrl() }
        view.deleteCanvas.setOnClickListener { removeCanvasImage() }
        view.currentPrioritiesSpinner.onSpinnerItemSelectedListener = sharedViewModel.initializeSpinner(requireContext(), view.priorityIndicator)
        view.mediaPlayer.deleteAudio.setOnClickListener { removeMediaPlayer() }
        view.reminderLayout.setOnClickListener{inflateCancelReminderDialog()}

        return view.root
    }

    private fun addImage() {
        if (!sharedViewModel.allPermissionsGrantedForImage(requireContext())) {
            requestPermissions(
                sharedViewModel.requiredPermissionsForImage,
                sharedViewModel.requestCodeForImagePermissions
            )
        } else {
            val dialog = AlertDialog.Builder(requireContext())
            dialog.setTitle("Add Image").setMessage("Capture from Camera or Upload from Gallery")
            dialog.setNegativeButton("Gallery") { _, _ -> openGallery() }
            dialog.setPositiveButton("Camera") { _, _ -> openCamera() }
            dialog.create().show()
        }
    }
    private fun openCamera() {
        val intent = sharedViewModel.openCamera(requireActivity(), requireContext())
        if (intent == null)
            Toast.makeText(requireContext(), "Some error occured!", Toast.LENGTH_SHORT).show()
        else
            startActivityForResult(intent, sharedViewModel.fromCamera)
    }
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, fromGallery)
    }
    private fun setImage(root:RelativeLayout,it: String,image:ImageView) {
        Glide.with(requireContext()).load(it).into(image)
        root.visibility=View.VISIBLE
    }
    private fun addVn() {
        val alert = AlertDialog.Builder(requireContext())
        alert.setTitle("Add Voice Note").setMessage("Choose an audio file or Record audio")
            .setNegativeButton("Record") { _, _ ->
                run {
                    if (!sharedViewModel.allPermissionsGrantedForMic(requireContext())) {
                        requestPermissions(
                            sharedViewModel.requiredPermissionForAudioRecord,
                            sharedViewModel.micRequestCode
                        )
                    } else {
                       sharedViewModel.inflateRecordDialog(requireContext()).show()

                    }
                }
            }
            .setPositiveButton("Upload") { _, _ ->
                run {
                    if (sharedViewModel.allPermissionsGrantedForAudioPicker(requireContext())) {
                        val intent =
                            Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                        startActivityForResult(intent, 77)
                    } else {
                        requestPermissions(
                            sharedViewModel.requiredPermissionForAudioPicker,
                            sharedViewModel.audioDirectoryRequestCode
                        )
                    }
                }
            }
        alert.create().show()

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == sharedViewModel.fromCamera) {
                uri = Uri.parse(sharedViewModel.mCurrentPhotoPath)
                setImage(view.imageRoot,uri.toString(),view.image)
            } else if (requestCode == fromGallery && data != null) {
                uri=data.data!!
                val path = sharedViewModel.getRealPathFromURI(uri, requireActivity())
                if (path != null) {
                    sharedViewModel.mCurrentPhotoPath = path
                    setImage(view.imageRoot,path,view.image)
                }
            } else if (requestCode == 77 && data != null) {
                val uri: Uri = data.data!!
                val path: String? = sharedViewModel.getRealPathFromURI(uri, requireActivity())
                if (path != null) {
                    sharedViewModel.setRecordAudio(path)
                }
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == sharedViewModel.requestCodeForImagePermissions) {
            if (sharedViewModel.allPermissionsGrantedForImage(requireContext())) {
                addImage()
            } else {
                Toast.makeText(activity, "Permissions not granted!", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.update_fragment_menu, menu)
    }
    override fun onPrepareOptionsMenu(menu: Menu) {
        if (menu.javaClass.simpleName == "MenuBuilder") {
            try {

                val m: Method = menu.javaClass.getDeclaredMethod(
                    "setOptionalIconsVisible", java.lang.Boolean.TYPE
                )
                m.isAccessible = true
                m.invoke(menu, true)
            } catch (e: Exception) {}
        }
        super.onPrepareOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_save -> updateData()
            R.id.menu_delete -> deleteData()
            R.id.menu_add_image -> addImage()
            R.id.add_url -> sharedViewModel.urlDialog(requireContext(), layoutInflater, view.urlRoot).show()
            R.id.menu_add_vn -> addVn()
            R.id.canvas->findNavController().navigate(R.id.action_updateFragment_to_drawFragment)
            R.id.reminder->sharedViewModel.setReminder(requireContext(),view.reminderLayout)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteData() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _, _ ->
            todoViewModel.deleteData(args, requireContext())
            Toast.makeText(
                requireContext(),
                "Successfully Removed: ${args.title}",
                Toast.LENGTH_SHORT
            ).show()
            sharedViewModel.deinitializeSharedVariables()
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("Delete '${args.title}'?")
        builder.setMessage("Are you sure you want to remove '${args.title}'?")
        builder.create().show()
    }
    private fun updateData() {
        val title = view.currentTitleEt.text.toString()
        val priority = view.currentPrioritiesSpinner.selectedItem
        val desc = view.currentDescriptionEt.text.toString()
        if (sharedViewModel.validateData(title, desc) && (sharedViewModel.date==null || sharedViewModel.date!!.timeInMillis>System.currentTimeMillis())) {
            val newData = ToDoData(
                args.id,
                title,
                sharedViewModel.parsePriority(priority.toString()),
                desc,
                args.date,
                sharedViewModel.mCurrentPhotoPath,
                mediaPlayerLifeCycle.audioFilePath, noteUrl, null,canvasPath,sharedViewModel.dateString
            )
            todoViewModel.updateData(newData, requireContext(),sharedViewModel.date)
            sharedViewModel.deinitializeSharedVariables()
            Toast.makeText(requireContext(), "Updated Successfully!", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        }else if(sharedViewModel.date!=null && sharedViewModel.date!!.timeInMillis<=System.currentTimeMillis())
            Toast.makeText(requireContext(), "Please set a later time for reminder!", Toast.LENGTH_SHORT).show()
        else {
            Toast.makeText(requireContext(), "Please fill all fields!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDetach() {
        super.onDetach()
        sharedViewModel.deinitializeSharedVariables()
    }

    private fun removeImage() {
        view.imageRoot.visibility=View.GONE
        sharedViewModel.mCurrentPhotoPath = ""
    }
    private fun removeUrl() {
        noteUrl = ""
        view.deleteUrl.visibility = View.GONE
        view.urlText.visibility = View.GONE
    }
    private fun removeCanvasImage(){
        canvasPath=""
        sharedViewModel.setCanvasImage("")
        view.canvasRoot.visibility=View.GONE
    }
    private fun removeMediaPlayer() {
        mediaPlayerLifeCycle.removeMediaPlayer()
        sharedViewModel.setRecordAudio("")
    }

    private fun inflateCancelReminderDialog() {
        val alertDialog=AlertDialog.Builder(requireContext())
        alertDialog.setTitle("Cancel Reminder?")
        alertDialog.setPositiveButton("Yes"){_,_->run{
            view.reminderLayout.visibility=View.GONE
            todoViewModel.cancelReminder(args.id,requireContext())
        }}
        alertDialog.setNegativeButton("No",null)
        alertDialog.create().show()
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
}