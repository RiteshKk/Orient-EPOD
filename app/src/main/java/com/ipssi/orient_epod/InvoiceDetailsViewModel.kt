package com.ipssi.orient_epod

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipssi.orient_epod.model.Receiver
import com.ipssi.orient_epod.remote.remote.ApiClient
import com.ipssi.orient_epod.remote.remote.util.Resource
import com.ipssi.orient_epod.remote.repository.ApiHelper
import com.ipssi.orient_epod.remote.util.AppConstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.lang.Exception

class InvoiceDetailsViewModel : ViewModel() {

    var receiversList = MutableLiveData<Resource<ArrayList<Receiver>>>()
    fun getReceivers(invoice: String) {
        viewModelScope.launch(Dispatchers.IO) {
            receiversList.postValue(Resource.loading(null))
            try {
                val receivers = ApiHelper(ApiClient.getApiService()).getReceivers(invoice)
                withContext(Dispatchers.Default) {
                    receiversList.postValue(Resource.success(receivers))
                }
            } catch (ex: Exception) {
                when (ex) {
                    is IOException -> receiversList.postValue(Resource.offline(null, AppConstant.OFFLINE_ERROR))
                    else -> receiversList.postValue(Resource.error(null, AppConstant.GENERIC_ERROR))
                }

            }
        }
    }
}