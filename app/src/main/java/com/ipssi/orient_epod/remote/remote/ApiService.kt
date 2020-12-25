package com.ipssi.orient_epod.remote.remote

import com.ipssi.orient_epod.model.*
import com.ipssi.orient_epod.remote.util.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST(SEARCH_DRIVER_SHIPMENT)
    suspend fun getDriverShipment(@Body credentials: Credentials): DriverShipmentDao

    @POST(SAVE_RECEIVER)
    suspend fun saveReceiverDetails(@Body receiver: Receiver): SaveReceiverResponse

    @GET(GET_RECEIVERS)
    suspend fun getReceivers(@Path("invoice") invoice: String): ArrayList<Receiver>

    @POST(UPLOAD_IMAGE)
    suspend fun uploadImage(@Body body: UploadDocumentEntity): UploadDocumentEntity

    @GET(GET_UPLOADED_IMAGE)
    suspend fun getUploadedImage(@Path("shipment") shipment: String, @Path("invoice") invoice: String, @Path("do_no") doNumber: String, @Path("lr") lrNumber: String, @Path("from1") from: String): ArrayList<UploadDocumentEntity>

    @DELETE(DELETE_UPLOADED_IMAGE)
    suspend fun getUploadedImage(@Path("id") id: Long): EpodResponse

    @POST(SAVE_DRIVER_LOCATION)
    suspend fun saveDriverLocation(@Body driverLocationEntity: DriverLocationEntity): Any

    @GET(SEND_OTP)
    suspend fun getOTP(@Path("phone") mobileNumber: String): Response<String>
}

