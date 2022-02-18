package com.atria.software.marqueberry

import android.net.Uri
import androidx.lifecycle.MutableLiveData

object Model {
    val imageUri  = MutableLiveData<Uri?>(null)
    val selfieCaptureListener = MutableLiveData<Boolean>(false)
}