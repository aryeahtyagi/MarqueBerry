package com.atria.software.marqueberry

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import com.oginotihiro.cropview.CropUtil
import com.oginotihiro.cropview.CropView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class EditFragment : Fragment() {

    private lateinit var cropView : CropView
    private lateinit var normalView : ImageView
    private lateinit var bottomFilter : LinearLayout
    private var original:Uri? =null

    companion object{
        private const val TAG = "EditFragment"
        private var degreesCache = 0f
    }
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cropView = view.findViewById<CropView>(R.id.cropView)
        normalView = view.findViewById(R.id.normalView)
        bottomFilter = view.findViewById(R.id.bottomFilter)
        val undoButton = view.findViewById<AppCompatButton>(R.id.undoButton)
        val cropButton = view.findViewById<AppCompatButton>(R.id.cropButton)
        val doneButton = view.findViewById<AppCompatButton>(R.id.doneButton)
        val rotateLeft = view.findViewById<AppCompatButton>(R.id.rotateLeftButton)
        val saveButton = view.findViewById<AppCompatButton>(R.id.saveButton)

        Model.imageUri.observe(viewLifecycleOwner){
            normalView.setImageURI(it)
            cropView.of(it).asSquare().initialize(context)
            original = it
        }
        cropButton.setOnClickListener {
            bottomFilter.visibility = View.GONE
            normalView.visibility = View.GONE
            cropView.visibility = View.VISIBLE
            doneButton.visibility = View.VISIBLE
        }

        doneButton.setOnClickListener {
            bottomFilter.visibility = View.VISIBLE
            if(cropView.visibility == View.VISIBLE){
                cropView.visibility = View.GONE
                val output = cropView.output
                normalView.setImageBitmap(output)
                normalView.visibility = View.VISIBLE
                doneButton.visibility = View.INVISIBLE
            }else{
                // here rotate is happening
                rotateLeft.visibility = View.GONE
                doneButton.visibility = View.GONE
            }
            bottomFilter.visibility = View.VISIBLE
        }

//        val done = view.findViewById<Button>(R.id.button)
        val rotateButton = view.findViewById<AppCompatButton>(R.id.rotateButton)
//        done.setOnClickListener {
//            val output = cropView.output
//            saveImageToGallery(requireContext(),output)
//        }
        rotateButton.setOnClickListener {
            bottomFilter.visibility = View.GONE
            rotateLeft.visibility = View.VISIBLE
            doneButton.visibility = View.VISIBLE
        }

        rotateLeft.setOnClickListener {
            rotate()
        }


        undoButton.setOnClickListener {
            normalView.scaleType = ImageView.ScaleType.CENTER
            normalView.setImageURI(original)
            cropView.of(original).asSquare().initialize(context)
        }

        saveButton.setOnClickListener {
            val matrix = Matrix()
            matrix.postRotate(
                degreesCache,
                (normalView.drawable.bounds.width() / 2).toFloat(),
                (normalView.drawable.bounds.height() / 2).toFloat()
            )
            val bitmap = normalView.drawable.toBitmap(normalView.width,normalView.height)
            val rotatedBitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.width,bitmap.height,matrix,false)
            saveImageToGallery(requireContext(),rotatedBitmap)
            Toast.makeText(context, "Image Save Successfully", Toast.LENGTH_SHORT).show()
        }


    }


    private fun rotate(){
        val matrix = Matrix()
        degreesCache.updateDegree()
        normalView.scaleType = ImageView.ScaleType.MATRIX
            matrix.postRotate(
                degreesCache,
                (normalView.drawable.bounds.width() / 2).toFloat(),
                (normalView.drawable.bounds.height() / 2).toFloat()
            )
        normalView.imageMatrix = matrix
        cropView.setImageBitmap(normalView.drawable.toBitmap(normalView.width,normalView.height))
    }

    private fun Float.updateDegree(){
        if(degreesCache==270f){
            degreesCache = 0f
        }else{
            degreesCache+=90f
        }
    }

    private fun saveImageToGallery(context: Context, bmp: Bitmap): Boolean {
        // First save the picture
        val storePath =
            Environment.getExternalStorageDirectory().absolutePath + File.separator + "dearxy"
        val appDir = File(storePath)
        if (!appDir.exists()) {
            appDir.mkdir()
        }
        val fileName = System.currentTimeMillis().toString() + ".jpg"
        val file = File(appDir, fileName)
        try {
            val fos = FileOutputStream(file)
            //Compress and save pictures by io stream
            val isSuccess = bmp.compress(Bitmap.CompressFormat.JPEG, 60, fos)
            fos.flush()
            fos.close()

            //Update the database by sending broadcast notifications after saving pictures
            val uri = Uri.fromFile(file)
            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
            return isSuccess
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }
}