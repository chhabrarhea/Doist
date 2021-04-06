package com.example.todo.fragments.draw

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.todo.R
import com.example.todo.databinding.FragmentDrawBinding
import com.example.todo.fragments.SharedViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

@Suppress("BlockingMethodInNonBlockingContext", "KDocUnresolvedReference")
class DrawFragment : Fragment(){
private lateinit var binding:FragmentDrawBinding

private lateinit var sheetBehavior:BottomSheetBehavior<CardView>
private val sharedViewModel:SharedViewModel by viewModels()

    private var isSaved=false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding= FragmentDrawBinding.inflate(layoutInflater,container,false)
        sheetBehavior=BottomSheetBehavior.from(binding.bottomSheet)
        (activity as AppCompatActivity?)!!.setSupportActionBar(binding.toolbar)
        setHasOptionsMenu(true)
        initStrokeOptions()
        initColorOptions()
        binding.detail.eraser.setOnClickListener{
            binding.canvas.onEraser()
            binding.detail.pen.setCardBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.detail.eraser.setCardBackgroundColor(ContextCompat.getColor(requireContext(),R.color.secondaryColor))
        }
        binding.detail.pen.setOnClickListener{
            binding.canvas.onEraser()
            binding.detail.pen.setCardBackgroundColor(ContextCompat.getColor(requireContext(),R.color.secondaryColor))
            binding.detail.eraser.setCardBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
        }
        binding.detail.delete.setOnClickListener{
            val alertDialog=AlertDialog.Builder(requireContext())
            alertDialog.setTitle("Clear Canvas")
            alertDialog.setMessage("Are you sure you want to delete your progress?")
            alertDialog.setPositiveButton("Yes"){_,_->
                run {
                    binding.canvas.clearBrushes()
                }
            }
            alertDialog.setNegativeButton("No"){_,_->}
        }
        return binding.root
    }

    private fun initStrokeOptions() {
           BrushSizePicker(binding.detail.brush,requireContext(),object:BrushSizePicker.SizeSelected{
               override fun sizeSelected(size: Float) {
                   binding.canvas.setSizeForBrush(size)
               }
           })
    }

    private fun initColorOptions(){
        ColorPicker(binding.detail.palette,object:ColorPicker.ColorPicked{
            override fun colorPicked(color: Int) {
                binding.canvas.setBrushColor(color)
            }
        })
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.draw_fragment_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.grid->openGridDialog()
            R.id.share->shareDrawing()
            R.id.undo->binding.canvas.undoPath()
            R.id.redo->binding.canvas.redoPath()
            R.id.save->saveImageAndNavigate()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveBitmap(i:Int) {
        if(!checkForPermission())
            requestPermissions(arrayOf(permission), permissionCode)
        else{
            CoroutineScope(IO).launch{
                val a=async(IO) {
                    saveImage(getBitmapFromView(binding.canvas))
                }
                sharedViewModel.setCanvasFromBackground(a.await())

                a.invokeOnCompletion {
                    isSaved=true

                   if(i==1)
                       saveImageAndNavigate()
                    else
                        shareDrawing()
                }}

        }

    }

    private fun saveImageAndNavigate(){
        if(!isSaved){
            saveBitmap(1) }
        else{

        findNavController().popBackStack()}
    }


    private fun shareDrawing() {
        if(!isSaved){
            saveBitmap(2)}
        else{
            // This is used for sharing the image after it has being stored in the storage.
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.putExtra(
                Intent.EXTRA_STREAM,
                FileProvider.getUriForFile(requireContext(),requireContext().applicationContext.packageName+".provider",File(SharedViewModel.canvasImage.value!!))
            )
            shareIntent.type = "image/*"
            startActivity(shareIntent)
        }
    }

    private fun openGridDialog() {
        val dialog=Dialog(requireContext())
        dialog.setContentView(R.layout.grid_dialog)
        dialog.setTitle("Choose Grid:")
        dialog.findViewById<RelativeLayout>(R.id.grid).setOnClickListener { binding.gridBackground.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_grid_texture))
            binding.gridBackground.scaleType=ImageView.ScaleType.FIT_XY
            dialog.dismiss()}
        dialog.findViewById<RelativeLayout>(R.id.ruled).setOnClickListener { binding.gridBackground.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_ruled_texture))
            binding.gridBackground.scaleType=ImageView.ScaleType.FIT_XY
            dialog.dismiss()}
        dialog.findViewById<RelativeLayout>(R.id.isometric).setOnClickListener { binding.gridBackground.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_isometric_texture))
            binding.gridBackground.scaleType=ImageView.ScaleType.CENTER_CROP
            dialog.dismiss() }
        dialog.findViewById<RelativeLayout>(R.id.none).setOnClickListener { binding.gridBackground.setImageDrawable(null)
            binding.gridBackground.scaleType=ImageView.ScaleType.FIT_XY
            dialog.dismiss() }
        dialog.findViewById<Button>(R.id.cancel).setOnClickListener{dialog.dismiss()}
        dialog.show()
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = binding.gridBackground.drawable
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {

            canvas.drawColor(Color.parseColor("#e2e2e2"))
        }
        view.draw(canvas)
        return returnedBitmap
    }

    companion object{
        private const val permissionCode=2
        private const val permission= android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    }

    private fun checkForPermission():Boolean{
        if(ContextCompat.checkSelfPermission(requireContext(), permission)==PackageManager.PERMISSION_GRANTED)
            return true
        return false
    }

    private suspend fun saveImage(bitmap: Bitmap):String = withContext(IO){
        var result: String
        try {
            val bytes = ByteArrayOutputStream() // Creates a new byte array output stream.
            // The buffer capacity is initially 32 bytes, though its size increases if necessary.

            bitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)
            /**
             * Write a compressed version of the bitmap to the specified output stream.
             * If this returns true, the bitmap can be reconstructed by passing a
             * corresponding input stream to BitmapFactory.decodeStream(). Note: not
             * all Formats support all bitmap configs directly, so it is possible that
             * the returned bitmap from BitmapFactory could be in a different bit depth,
             * and/or may have lost per-pixel alpha (e.g. JPEG only supports opaque
             * pixels).
             *
             * @param format   The format of the compressed image
             * @param quality  Hint to the compressor, 0-100. 0 meaning compress for
             *                 small size, 100 meaning compress for max quality. Some
             *                 formats, like PNG which is lossless, will ignore the
             *                 quality setting
             * @param stream   The output stream to write the compressed data.
             * @return true if successfully compressed to the specified stream.
             */
            /**
             * Write a compressed version of the bitmap to the specified output stream.
             * If this returns true, the bitmap can be reconstructed by passing a
             * corresponding input stream to BitmapFactory.decodeStream(). Note: not
             * all Formats support all bitmap configs directly, so it is possible that
             * the returned bitmap from BitmapFactory could be in a different bit depth,
             * and/or may have lost per-pixel alpha (e.g. JPEG only supports opaque
             * pixels).
             *
             * @param format   The format of the compressed image
             * @param quality  Hint to the compressor, 0-100. 0 meaning compress for
             *                 small size, 100 meaning compress for max quality. Some
             *                 formats, like PNG which is lossless, will ignore the
             *                 quality setting
             * @param stream   The output stream to write the compressed data.
             * @return true if successfully compressed to the specified stream.
             */

            val f = File(
                requireContext().externalCacheDir!!.absoluteFile.toString()
                        + File.separator + "Doist_" + System.currentTimeMillis() / 1000 + ".jpg"
            )

            val fo = FileOutputStream(f) // Creates a file output stream to write to the file represented by the specified object.
            fo.write(bytes.toByteArray()) // Writes bytes from the specified byte array to this file output stream.
            fo.close() // Closes this file output stream and releases any system resources associated with this stream. This file output stream may no longer be used for writing bytes.
            result = f.absolutePath // The file absolute path is return as a result.
        } catch (e: Exception) {
            result = ""
            e.printStackTrace()
        }

        return@withContext result
        }





}