package com.ipssi.orient_epod.login

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipssi.orient_epod.remote.remote.ApiClient
import com.ipssi.orient_epod.model.Credentials
import com.ipssi.orient_epod.model.DriverShipmentDao
import com.ipssi.orient_epod.remote.remote.util.Resource
import com.ipssi.orient_epod.remote.repository.ApiHelper
import com.ipssi.orient_epod.remote.util.AppConstant.OFFLINE_ERROR
import com.ipssi.orient_epod.remote.util.AppConstant.SERVER_ERROR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException


class SharedViewModel : ViewModel() {
    var isLoading = MutableLiveData<Boolean>().apply { value = false }
    var shipmentDetailsData = MutableLiveData<Resource<DriverShipmentDao>>()

    fun getShipmentDetails(credentials: Credentials) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                shipmentDetailsData.postValue(Resource.loading(null))
                val driverShipment = ApiHelper(ApiClient.getApiService()).getDriverShipment(credentials)
                withContext(Dispatchers.Default) {
                    Log.d("data loaded", "status=${driverShipment.status}")
                    if (driverShipment.status == 1) {
                        shipmentDetailsData.postValue(Resource.success(driverShipment))
                    } else {
                        shipmentDetailsData.postValue(Resource.error(driverShipment, "Generic Error"))
                    }
                }
            } catch (exception: Exception) {
                withContext(Dispatchers.Default) {
                    when (exception) {
                        is IOException -> {
                            shipmentDetailsData.postValue(Resource.offline(null, OFFLINE_ERROR))
                        }
                        is HttpException -> {
                            shipmentDetailsData.postValue(Resource.error(null, SERVER_ERROR))
                        }
                        else ->{
                            shipmentDetailsData.postValue(Resource.error(null, SERVER_ERROR))
                        }

                    }
                }
            }
        }
    }
}

