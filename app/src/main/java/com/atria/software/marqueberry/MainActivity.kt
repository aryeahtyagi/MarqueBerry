package com.atria.software.marqueberry

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.oginotihiro.cropview.Crop.REQUEST_PICK
import java.util.jar.Manifest

class MainActivity :  AppCompatActivity(){
    companion object{
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermission(){
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED){

            requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ,android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.CAMERA),100)

        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 100){
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED
                && grantResults[2]==PackageManager.PERMISSION_GRANTED){

            }else{
                MaterialAlertDialogBuilder(this)
                    .setTitle("Permission Necessary ")
                    .setMessage("Camera and storage permission are required ")
                    .setPositiveButton("Okay") { _: DialogInterface, _: Int ->
                        requestPermissions(
                            arrayOf(
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                android.Manifest.permission.CAMERA
                            ), 100
                        )
                    }
                    .setNegativeButton("Exit"){_,_->
                        finish()
                    }
                    .show()
            }
        }
    }

}