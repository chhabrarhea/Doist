package com.example.todo.fragments

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.os.SystemClock
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.todo.BuildConfig
import com.example.todo.R
import com.example.todo.data.models.Priority
import com.example.todo.data.models.ToDoData
import com.example.todo.databinding.RecordDialogBinding
import com.example.todo.databinding.UrlDialogBinding
import com.github.squti.androidwaverecorder.WaveRecorder
import com.google.android.material.textfield.TextInputLayout
import org.angmarch.views.OnSpinnerItemSelectedListener
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SharedViewModel(application: Application) : AndroidViewModel(application) {
    var listIsEmpty = MutableLiveData<Boolean>()
    var mCurrentPhotoPath = ""
    val fromCamera = 200
    var date:Calendar?=null
    var dateString:String?=null

    //data shared by fragments- audio path and canvas path
    companion object{
    var audioRecorded=MutableLiveData("")
    var canvasImage=MutableLiveData("")}
    @Synchronized fun setCanvasImage(s:String){
        canvasImage.value=s
    }
    @Synchronized fun setCanvasFromBackground(res:String){
        canvasImage.postValue(res)
    }
    @Synchronized fun setRecordAudio(path:String){
        audioRecorded.value=path
    }



    //permissions and results
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
    fun allPermissionsGrantedForImage(context: Context) = requiredPermissionsForImage.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
    fun allPermissionsGrantedForMic(context: Context)=requiredPermissionForAudioRecord.all {
        ContextCompat.checkSelfPermission(context,it)== PackageManager.PERMISSION_GRANTED
    }
    fun allPermissionsGrantedForAudioPicker(context: Context)=requiredPermissionForAudioPicker.all {
        ContextCompat.checkSelfPermission(context,it)== PackageManager.PERMISSION_GRANTED
    }




    fun urlDialog(context: Context,layoutInflater:LayoutInflater,view:RelativeLayout):AlertDialog{
        val binding=UrlDialogBinding.inflate(layoutInflater,null,false)
        val builder= AlertDialog.Builder(context)
        builder.setView(binding.root)
        val alert= builder.create()
        binding.cancel.setOnClickListener { alert.dismiss() }
        binding.ok.setOnClickListener {
            if(Patterns.WEB_URL.matcher(binding.urlEdittext.text).matches()){
                view.visibility=View.VISIBLE
                view.findViewById<TextView>(R.id.url_text).text=binding.urlEdittext.text.toString()
                alert.dismiss()
            }else{
               binding.urlEdittext.error="Enter valid URL!"
            } }
        return alert}



    fun validateData(title: String, desc: String): Boolean {
        return if (TextUtils.isEmpty(title) || TextUtils.isEmpty(desc)) {
            false
        } else !(title.isEmpty() || desc.isEmpty())
    }

    //orientation of listFragment RecyclerView
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
    fun checkListIsEmpty(list: List<ToDoData>) {
        listIsEmpty.value = list.isEmpty()
    }



    //setting UI and extracting data from UI for priority
    fun parsePriority(priority: String): Priority {
        return when (priority) {
            "High Priority" -> Priority.HIGH
            "Medium Priority" -> Priority.MEDIUM
            "Low Priority" -> Priority.LOW
            else -> Priority.LOW } }
    fun parsePriorityById(priority: Int): Priority {
        return when (priority) {
            0-> Priority.HIGH
            1 -> Priority.MEDIUM
            2-> Priority.LOW
            else -> Priority.LOW
        } }
    fun initializeSpinner(context: Context,priorityIndicator: CardView):OnSpinnerItemSelectedListener{

        return OnSpinnerItemSelectedListener { parent, _, position, _ ->
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
    fun initializeSpinnerForCheckList(context: Context,layout:TextInputLayout):OnSpinnerItemSelectedListener{
        return OnSpinnerItemSelectedListener { parent, _, position, _ ->
            parent.setTextAppearance(R.style.textAppearance)
            when(position){
                0->{
                    parent.setTextColor(ContextCompat.getColor(context,R.color.high))
                    layout.boxStrokeColor=ContextCompat.getColor(context,R.color.high)
                }
                1->{
                    parent.setTextColor(ContextCompat.getColor(context,R.color.medium))
                    layout.boxStrokeColor=ContextCompat.getColor(context,R.color.medium)
                }
                2->{
                    parent.setTextColor(ContextCompat.getColor(context,R.color.low))
                    layout.boxStrokeColor=ContextCompat.getColor(context,R.color.low)
                }
            }
        }

    }


     //setting cameraIntent
    fun openCamera(activity: Activity, context: Context): Intent? {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(activity.packageManager) != null) {
            // Create the File where the photo should go
            val photoFile: File?
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                return null
            }
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
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss",Locale.getDefault()).format(Date())
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
    @SuppressLint("Recycle")

    //getting path of photo selected from gallery
    fun getRealPathFromURI(contentUri: Uri, context: Activity): String? {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? = context.contentResolver.query(
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
    
    fun setReminder(context: Context,view:RelativeLayout){
        val newCalender = Calendar.getInstance();
        val datePicker= DatePickerDialog(
            context,
            { _, year, month, day ->
                run {
                    val newDate = Calendar.getInstance()
                    val newTime = Calendar.getInstance()
                    val time = TimePickerDialog(
                        context,
                        { _, hour, min ->
                            run {
                                newDate.set(year, month, day, hour, min)
                                val tem = Calendar.getInstance()
                                if (newDate.timeInMillis - tem.timeInMillis > 0) {
                                    date=newDate
                                    view.visibility=View.VISIBLE
                                   val tv=view.getChildAt(1) as TextView
                                    val df=SimpleDateFormat("MMM dd, h:mm a",Locale.getDefault())
                                    dateString=df.format(date!!.time)
                                    tv.text=dateString
                                } else Toast.makeText(
                                    context,
                                    "Invalid time",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }, newTime.get(Calendar.HOUR_OF_DAY), newTime.get(Calendar.MINUTE), true
                    )
                    time.show()
                }

            }, newCalender.get(Calendar.YEAR), newCalender.get(Calendar.MONTH), newCalender.get(
                Calendar.DAY_OF_MONTH
            )
        )
        datePicker.datePicker.minDate = System.currentTimeMillis()
        datePicker.show()
    }

    fun deinitializeSharedVariables(){
        canvasImage.value=""
        audioRecorded.value=""
        date=null
        dateString=null
    }

    fun deleteReminderDialog(context: Context,view:RelativeLayout):AlertDialog.Builder{
        val alertDialog=AlertDialog.Builder(context)
        alertDialog.setTitle("Cancel Reminder?")
        alertDialog.setPositiveButton("Yes"){_,_->run{
            view.visibility=View.GONE
            date=null
            dateString=null

        }}
        alertDialog.setNegativeButton("No",null)
        alertDialog.create()
        return alertDialog
    }

    fun inflateRecordDialog(context: Context):AlertDialog{
        val file = File(context.getExternalFilesDir(null)?.absolutePath, "Doist")
        if (!file.exists()) file.mkdir()
        val audioFilePath =file.path + "/audio" + Calendar.getInstance().time + ".wav"
        var isRecording=false
        var recorder:WaveRecorder?=null
        val alertDialog=AlertDialog.Builder(context)
        val binding=RecordDialogBinding.inflate(LayoutInflater.from(context))
        alertDialog.setView(binding.root)
        val alb=alertDialog.create()

        binding.recordButton.setOnClickListener {
             if(!isRecording){
                 binding.recordButton.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_stop))
                 isRecording=true
                 try {
                     binding.timer.base = SystemClock.elapsedRealtime()
                     recorder = WaveRecorder(audioFilePath)
                     recorder!!.startRecording()
                     binding.timer.start()
                 } catch (e: Exception) {
                     e.printStackTrace()
                 }
             }
            else{
                 binding.recordButton.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_baseline_mic_24))
                 binding.timer.stop()
                 recorder!!.stopRecording()
                 binding.recordButton.isEnabled = true
                 isRecording = false
             }
        }
        binding.save.setOnClickListener {
           if(isRecording)
           {
               binding.recordButton.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_baseline_mic_24))
               binding.timer.stop()
               recorder!!.stopRecording()
               isRecording=!isRecording
           }
            this.setRecordAudio(audioFilePath)
            alb.dismiss()
        }
        return alb
    }
    }

    

