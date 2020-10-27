package com.ipssi.orient_epod.ui.main

import android.graphics.Bitmap
import android.location.Location
import android.util.Base64
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipssi.orient_epod.model.Receiver
import com.ipssi.orient_epod.model.SaveReceiverResponse
import com.ipssi.orient_epod.remote.remote.ApiClient.getApiService
import com.ipssi.orient_epod.remote.repository.ApiHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class PageViewModel : ViewModel() {
    var saveReceiverResponse = MutableLiveData<SaveReceiverResponse>()
    private val mIndex = MutableLiveData<Int>()
    val text = Transformations.map(mIndex) { input: Int -> "Hello world from section: $input" }
    fun setIndex(index: Int) {
        mIndex.value = index
    }

    fun saveReceiver(name: String, mobile: String, invoice: String, bagsReceived: String, shortage: String, loadType: Int, remarks: String, image: Bitmap, location: Location) {
        viewModelScope.launch(Dispatchers.IO) {

            val byteArrayOutputStream = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream)
            val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
            Log.d("imageSize", "${byteArray.size}")
            byteArrayOutputStream.close()
            val encoded = Base64.encodeToString(byteArray, Base64.DEFAULT)
            Log.d("imageSize2","${encoded.length}")
            val receiver = Receiver(name, mobile, invoice, bagsReceived, shortage, loadType, remarks, encoded, 1, "${location.latitude},${location.longitude}")
            val saveReceiverDetails = ApiHelper(getApiService()).saveReceiverDetails(receiver)
            withContext(Dispatchers.Default) {
                saveReceiverResponse.postValue(saveReceiverDetails)
            }
        }
    }
}