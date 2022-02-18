package com.atria.software.marqueberry

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class HomeFragment : Fragment() {

    private lateinit var pickImage :ActivityResultLauncher<Intent>

    companion object {
        private const val TAG = "HomeFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pickImage =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    // There are no request codes
                    val data: Intent? = it.data
                    val selectedImageUri: Uri? = data?.data
                    Model.imageUri.postValue(selectedImageUri)
                    findNavController().navigate(R.id.action_homeFragment_to_editFragment)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val galleryButton = view.findViewById<Button>(R.id.galleryButton)
        val selfieButton = view.findViewById<Button>(R.id.selfieButton)

        galleryButton.setOnClickListener {
            pick()
        }

        selfieButton.setOnClickListener {
            takePicture()
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==111){
            val photo = data?.extras?.get("data") as Bitmap
            saveImageToGallery(requireContext(),photo)
            findNavController().navigate(R.id.action_homeFragment_to_editFragment)
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
            val isSuccess = bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()

            //Update the database by sending broadcast notifications after saving pictures
            val uri = Uri.fromFile(file)
            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
            Model.imageUri.postValue(uri)
            return isSuccess
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    private fun takePicture() {
        val camera_intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(camera_intent, 111)
    }


    private fun pick() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        pickImage.launch(Intent.createChooser(intent, "Pick image"))
    }

}