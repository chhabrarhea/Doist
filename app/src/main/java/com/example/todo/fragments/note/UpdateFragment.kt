package com.example.todo.fragments.note

import android.app.Activity
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
import com.example.todo.Utils.GlideApp
import com.example.todo.data.TodoViewModel
import com.example.todo.data.models.ToDoData
import com.example.todo.databinding.FragmentUpdateBinding
import com.example.todo.fragments.MediaPlayerLifeCycle
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


        mediaPlayerLifeCycle =
            MediaPlayerLifeCycle(view.mediaPlayer, requireContext(), args.voicenote)
        if (SharedViewModel.audioRecorded.value == "" && mediaPlayerLifeCycle.audioFilePath != "")
            sharedViewModel.setRecordAudio(mediaPlayerLifeCycle.audioFilePath)
        SharedViewModel.audioRecorded.observe(viewLifecycleOwner, {
            if (it != "") mediaPlayerLifeCycle.initializeMediaPlayer(it)
        })

        sharedViewModel.mCurrentPhotoPath = args.image
        (activity as AppCompatActivity?)!!.setSupportActionBar(view.toolbar)
        view.toolbar.overflowIcon =
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_attach_file_24)

        view.deleteImage.setOnClickListener { removeImage() }
        view.deleteUrl.setOnClickListener { removeUrl() }
        view.currentPrioritiesSpinner.onSpinnerItemSelectedListener =
            sharedViewModel.initializeSpinner(requireContext(), view.priorityIndicator)
        view.deleteAudio.setOnClickListener { removeMediaPlayer() }
        return view.root
    }

    private fun openCamera() {
        val intent = sharedViewModel.openCamera(requireActivity(), requireContext())
        if (intent == null)
            Toast.makeText(requireContext(), "Some error occured!", Toast.LENGTH_SHORT).show()
        else
            startActivityForResult(intent, sharedViewModel.fromCamera)
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

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, fromGallery)
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
        if (requestCode == sharedViewModel.requestCodeForImagePermissions) {
            if (sharedViewModel.allPermissionsGrantedForImage(requireContext())) {
                addImage()
            } else {
                Toast.makeText(activity, "Permissions not granted!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == sharedViewModel.fromCamera) {
                uri = Uri.parse(sharedViewModel.mCurrentPhotoPath)
                view.image.visibility = View.VISIBLE
                view.deleteImage.visibility = View.VISIBLE
                GlideApp.with(requireContext()).load(uri.toString()).into(view.image)
            } else if (requestCode == fromGallery && data != null) {
                uri = data.data!!
                Glide.with(requireContext()).load(uri.toString()).into(view.image)
                view.deleteImage.visibility = View.VISIBLE
                view.image.visibility = View.VISIBLE
                val path = sharedViewModel.getRealPathFromURI(uri, requireActivity())
                if (path != null) {
                    sharedViewModel.mCurrentPhotoPath = path
                }
            } else if (requestCode == 77 && data != null) {
                val uri: Uri = data.data!!
                val path: String? = sharedViewModel.getRealPathFromURI(uri, requireActivity())
                if (path != null) {
                    sharedViewModel.setRecordAudio(path)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Some error occured, try again!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.update_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_save -> updateData()
            R.id.menu_delete -> deleteData()
            R.id.menu_add_image -> addImage()
            R.id.add_url -> addURL()
            R.id.menu_add_vn -> addVn()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addURL() {
        val dialogView = layoutInflater.inflate(R.layout.url_dialog, null, false)
        val edit = dialogView.findViewById(R.id.url_edittext) as EditText
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView).setPositiveButton("Done") { _, _ -> run {} }
            .setNegativeButton("Cancel") { dialog, _ ->
                run {
                    dialog.dismiss()
                }
            }
        val alert = builder.create()
        alert.show()
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            noteUrl = edit.text.toString()
            if (Patterns.WEB_URL.matcher(noteUrl).matches()) {
                view.deleteUrl.visibility = View.VISIBLE
                view.urlText.visibility = View.VISIBLE
                view.urlText.text = noteUrl
                alert.dismiss()
            } else {
                noteUrl = ""
                view.deleteUrl.visibility = View.GONE
                view.urlText.visibility = View.GONE
                edit.error = "Enter valid URL!"
            }
        }
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
        if (sharedViewModel.validateData(title, desc)) {
            val newData = ToDoData(
                args.id,
                title,
                sharedViewModel.parsePriority(priority.toString()),
                desc,
                args.date,
                sharedViewModel.mCurrentPhotoPath,
                mediaPlayerLifeCycle.audioFilePath, noteUrl, null
            )
            todoViewModel.updateData(newData, requireContext())
            Toast.makeText(requireContext(), "Updated Successfully!", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        } else {
            Toast.makeText(requireContext(), "Please fill all fields!", Toast.LENGTH_SHORT).show()
        }
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
                        val bundle = Bundle()
                        bundle.putString("audioFilePath", args.date)
                        bundle.putString("title", args.title)
                        findNavController().navigate(
                            R.id.action_updateFragment_to_recordAudioFragment2,
                            bundle
                        )
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

    private fun removeImage() {
        view.deleteImage.visibility = View.GONE
        view.image.visibility = View.GONE
        sharedViewModel.mCurrentPhotoPath = ""
    }

    private fun removeUrl() {
        noteUrl = ""
        view.deleteUrl.visibility = View.GONE
        view.urlText.visibility = View.GONE
    }

    private fun removeMediaPlayer() {
        mediaPlayerLifeCycle.removeMediaPlayer()
        sharedViewModel.setRecordAudio("")
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