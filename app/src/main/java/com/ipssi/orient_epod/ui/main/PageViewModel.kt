package com.ipssi.orient_epod.ui.main

import android.graphics.Bitmap
import android.location.Location
import android.util.Base64
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipssi.orient_epod.model.*
import com.ipssi.orient_epod.remote.remote.ApiClient.getApiService
import com.ipssi.orient_epod.remote.remote.util.Resource
import com.ipssi.orient_epod.remote.repository.ApiHelper
import com.ipssi.orient_epod.remote.util.AppConstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.ByteArrayOutputStream
import java.io.IOException


class PageViewModel : ViewModel() {

    var isEditable = MutableLiveData<Boolean>().apply { value = true }
    var isCompleteTripChecked = MutableLiveData<Boolean>().apply { value = false }
    var isLoading = MutableLiveData<Boolean>().apply { value = false }
    var saveReceiverResponse = MutableLiveData<Resource<SaveReceiverResponse>>()
    var name = MutableLiveData<String>().apply { value = "" }
    var mobile = MutableLiveData<String>().apply { value = "" }
    var bagsReceived = MutableLiveData<String>().apply { value = "" }
    var damageBags = MutableLiveData<String>().apply { value = "" }
    var remarks = MutableLiveData<String>().apply { value = "" }
    var signature = MutableLiveData<Bitmap>()
    var uploadStatus = MutableLiveData<Resource<UploadDocumentEntity>>()
    var imageList = MutableLiveData<Resource<ArrayList<UploadDocumentEntity>>>()

    var imageDeleteObserver = MutableLiveData<Resource<EpodResponse>>()


    fun saveReceiver(id:Int,invoice: String, loadType: Int, location: Location, isFinalSubmit: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            saveReceiverResponse.postValue(Resource.loading(null))
            val byteArrayOutputStream = ByteArrayOutputStream()
            signature.value?.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream)
            val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
            byteArrayOutputStream.close()
            val encoded = Base64.encodeToString(byteArray, Base64.DEFAULT)
            val bags = if (bagsReceived.value?.length ?: 0 > 0 ) bagsReceived.value ?: "0" else "0"
            val damaged = if (damageBags.value?.length ?: 0 > 0 ) damageBags.value ?: "0" else "0"

            val receiver = Receiver(name.value, mobile.value, invoice, bags, damaged, loadType, remarks.value, encoded, if (isFinalSubmit) 2 else 1, "${location.latitude},${location.longitude}")
            if(id>0){
                receiver.id = id
            }
            try {
                val saveReceiverDetails = ApiHelper(getApiService()).saveReceiverDetails(receiver)
                saveReceiverResponse.postValue(Resource.success(saveReceiverDetails))
            } catch (ex: Exception) {
                when (ex) {
                    is IOException -> saveReceiverResponse.postValue(Resource.offline(null, AppConstant.OFFLINE_ERROR))
                    else -> saveReceiverResponse.postValue(Resource.error(null, AppConstant.GENERIC_ERROR))
                }
            }
        }
    }

    fun uploadImage(bitmap: Bitmap, invoice: Invoice) {
        viewModelScope.launch(Dispatchers.IO) {
            uploadStatus.postValue(Resource.loading(null))
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream)
            val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
            byteArrayOutputStream.close()
            val image = Base64.encodeToString(byteArray, Base64.DEFAULT)
            val body = UploadDocumentEntity(invoice.shipmentNumber, invoice.invoiceNumber, invoice.doNumber, invoice.lrNumber, "1", "lr_${invoice.lrNumber}.jpg", image)
            try {
                val uploadImage = ApiHelper(getApiService()).uploadImage(body)
                if (uploadImage.id > 0) {
                    withContext(Dispatchers.Main) {
                        imageList.value?.data?.add(uploadImage)
                    }
                    uploadStatus.postValue(Resource.success(uploadImage))
                } else {
                    uploadStatus.postValue(Resource.error(null, AppConstant.GENERIC_ERROR))
                }
            } catch (ex: Exception) {
                print(ex)
                when (ex) {
                    is IOException -> uploadStatus.postValue(Resource.offline(null, AppConstant.OFFLINE_ERROR))
                    else -> uploadStatus.postValue(Resource.error(null, AppConstant.GENERIC_ERROR))
                }
            }
        }
    }

    fun fetchUploadedImage(invoice: Invoice?) {
        viewModelScope.launch(Dispatchers.IO) {
            imageList.postValue(Resource.loading(null))

            try {
                val uploadedImage = ApiHelper(getApiService()).getUploadedImage(
                        shipment = invoice?.shipmentNumber ?: "0",
                        invoice = invoice?.invoiceNumber ?: "0",
                        doNumber = invoice?.doNumber ?: "0",
                        lrNumber = invoice?.lrNumber ?: "0",
                        from = "1"
                )
                imageList.postValue(Resource.success(uploadedImage))
            } catch (ex: java.lang.Exception) {
                when (ex) {
                    is IOException -> imageList.postValue(Resource.offline(null, AppConstant.OFFLINE_ERROR))
                    else -> imageList.postValue(Resource.error(null, AppConstant.GENERIC_ERROR))
                }
            }
        }
    }

    fun deleteUploadedImage(entity: UploadDocumentEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            imageDeleteObserver.postValue(Resource.loading(null))
            try {
                val deleteUploadedImage = ApiHelper(getApiService()).deleteUploadedImage(entity.id)
                if (deleteUploadedImage.status == "1") {
                    val data = imageList.value?.data
                    data?.remove(entity)
                    imageList.postValue(Resource.success(data ?: ArrayList()))
                    imageDeleteObserver.postValue(Resource.success(deleteUploadedImage))
                }
            } catch (ex: Exception) {
                when (ex) {
                    is IOException -> imageDeleteObserver.postValue(Resource.offline(null, AppConstant.OFFLINE_ERROR))
                    else -> imageDeleteObserver.postValue(Resource.error(null, AppConstant.GENERIC_ERROR))
                }
            }
        }
    }
}