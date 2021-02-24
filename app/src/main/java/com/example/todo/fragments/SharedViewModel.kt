package com.example.todo.fragments

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.todo.BuildConfig
import com.example.todo.R
import com.example.todo.data.models.Priority
import com.example.todo.data.models.ToDoData
import org.angmarch.views.OnSpinnerItemSelectedListener
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SharedViewModel(application: Application) : AndroidViewModel(application) {
    var listIsEmpty = MutableLiveData<Boolean>()
    var mCurrentPhotoPath = ""
    val fromCamera = 200

    companion object{
    var audioRecorded=MutableLiveData("")}

    //constants for permissions and results
    val requiredPermissionForAudioRecord = arrayOf(
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    val requiredPermissionForAudioPicker = arrayOf(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )
    val micRequestCode = 99
    val audioDirectoryRequestCode = 98
    val fromGallery = 100
    val requiredPermissionsForImage = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    val requestCodeForImagePermissions = 10


    fun validateData(title: String, desc: String): Boolean {
        return if (TextUtils.isEmpty(title) || TextUtils.isEmpty(desc)) {
            false
        } else !(title.isEmpty() || desc.isEmpty())
    }

    fun setListOrientation(context: Context, staggered: Boolean) {
        val pref: SharedPreferences =
            context.getSharedPreferences("ToDoOrientation", Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putBoolean("StaggeredOrientation", staggered)
        editor.apply()
    }

    fun getListOrientation(context: Context): Boolean {
        val pref: SharedPreferences =
            context.getSharedPreferences("ToDoOrientation", Context.MODE_PRIVATE)
        return pref.getBoolean("StaggeredOrientation", true)
    }



    fun parsePriority(priority: String): Priority {
        Log.i("prior", priority)
        return when (priority) {
            "High Priority" -> Priority.HIGH
            "Medium Priority" -> Priority.MEDIUM
            "Low Priority" -> Priority.LOW
            else -> Priority.LOW

        }

    }
    fun parsePriorityById(priority: Int): Priority {

        return when (priority) {
            0-> Priority.HIGH
            1 -> Priority.MEDIUM
            2-> Priority.LOW
            else -> Priority.LOW

        }

    }

    fun setRecordAudio(path:String){
            audioRecorded.value=path
        Log.i("onO","$path ${audioRecorded.value}")
    }

    fun checkListIsEmpty(list: List<ToDoData>) {
        listIsEmpty.value = list.isEmpty()
    }


    fun initializeSpinner(context: Context,priorityIndicator: CardView):OnSpinnerItemSelectedListener{

        return OnSpinnerItemSelectedListener { parent, v, position, id ->
            parent.setTextAppearance(R.style.textAppearance)
            when(position){
                0->{
                    parent.setTextColor(ContextCompat.getColor(context,R.color.high))
                    priorityIndicator.setCardBackgroundColor(ContextCompat.getColor(context,R.color.high))
                }
                1->{
                    parent.setTextColor(ContextCompat.getColor(context,R.color.medium))
                    priorityIndicator.setCardBackgroundColor(ContextCompat.getColor(context,R.color.medium))
                }
                2->{
                    parent.setTextColor(ContextCompat.getColor(context,R.color.low))
                    priorityIndicator.setCardBackgroundColor(ContextCompat.getColor(context,R.color.low))
                }
            }
        }
    }

    fun initializeSpinnerForCheckList(context: Context):OnSpinnerItemSelectedListener{
        return OnSpinnerItemSelectedListener { parent, v, position, id ->
            parent.setTextAppearance(R.style.textAppearance)
            when(position){
                0->{
                    parent.setTextColor(ContextCompat.getColor(context,R.color.high))
                }
                1->{
                    parent.setTextColor(ContextCompat.getColor(context,R.color.medium))
                }
                2->{
                    parent.setTextColor(ContextCompat.getColor(context,R.color.low))
                }
            }
        }

    }

    val listener: AdapterView.OnItemSelectedListener = object :
        AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {

        }
        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            when (position) {
                0 -> {
                    Log.i("itemSelected","${view?.id}  ")
                    val text = (view as TextView?)
                    text?.setTextAppearance(R.style.textAppearance)
                    text?.setTextColor(ContextCompat.getColor(application, R.color.high))
                }
                1 -> {
                    Log.i("itemSelected","${view?.id}  ")
                    val text = (parent?.getChildAt(0) as TextView?)
                    text?.setTextAppearance(R.style.textAppearance)
                    text?.setTextColor(ContextCompat.getColor(application, R.color.medium))
                }
                2 -> {
                    val text = (parent?.getChildAt(0) as TextView?)
                    text?.setTextAppearance(R.style.textAppearance)
                    text?.setTextColor(ContextCompat.getColor(application, R.color.low))
                }
            }
        }
    }

    fun openCamera(activity: Activity, context: Context): Intent? {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(activity.packageManager) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                return null
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                val photoURI: Uri = FileProvider.getUriForFile(
                    context,
                    BuildConfig.APPLICATION_ID + ".provider",
                    createImageFile()!!
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                return takePictureIntent
            }
        }
        return null
    }

    @Throws(IOException::class)
    fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir: File = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM
            ), "Camera"
        )
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )
        mCurrentPhotoPath = image.absolutePath
        return image
    }

    fun getRealPathFromURI(contentUri: Uri, context: Activity): String? {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? = context.getContentResolver().query(
            contentUri,
            proj,  // Which columns to return
            null,  // WHERE clause; which rows to return (all rows)
            null,  // WHERE clause selection arguments (none)
            null
        ) // Order-by clause (ascending by name)
        val column_index: Int = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(column_index)
    }

    fun allPermissionsGrantedForImage(context: Context) = requiredPermissionsForImage.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
    fun allPermissionsGrantedForMic(context: Context)=requiredPermissionForAudioRecord.all {
        ContextCompat.checkSelfPermission(context,it)== PackageManager.PERMISSION_GRANTED
    }
    fun allPermissionsGrantedForAudioPicker(context: Context)=requiredPermissionForAudioPicker.all {
        ContextCompat.checkSelfPermission(context,it)== PackageManager.PERMISSION_GRANTED
    }


}