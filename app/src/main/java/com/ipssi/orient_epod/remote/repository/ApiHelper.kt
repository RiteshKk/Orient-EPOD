package com.ipssi.orient_epod.remote.repository


import com.ipssi.orient_epod.model.Credentials
import com.ipssi.orient_epod.model.EpodResponse
import com.ipssi.orient_epod.model.Receiver
import com.ipssi.orient_epod.model.UploadDocumentEntity
import com.ipssi.orient_epod.remote.remote.ApiService
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Path

class ApiHelper(private val apiService: ApiService) {
    suspend fun getReceivers(invoice: String) = apiService.getReceivers(invoice)

    suspend fun getDriverShipment(credentials: Credentials) = apiService.getDriverShipment(credentials = credentials)

    suspend fun saveReceiverDetails(receiver: Receiver) = apiService.saveReceiverDetails(receiver = receiver)

    suspend fun uploadImage(body: UploadDocumentEntity) = apiService.uploadImage(body)

    suspend fun getUploadedImage(shipment: String, invoice: String, doNumber: String, lrNumber: String, from: String) = apiService.getUploadedImage(shipment, invoice, doNumber, lrNumber, from)

    suspend fun deleteUploadedImage(id: Long) = apiService.getUploadedImage(id)
}