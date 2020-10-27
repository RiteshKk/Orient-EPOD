package com.ipssi.orient_epod.remote.remote

import com.ipssi.orient_epod.model.Credentials
import com.ipssi.orient_epod.model.DriverShipmentDao
import com.ipssi.orient_epod.model.Receiver
import com.ipssi.orient_epod.model.SaveReceiverResponse
import com.ipssi.orient_epod.remote.util.SAVE_RECEIVER
import com.ipssi.orient_epod.remote.util.SEARCH_DRIVER_SHIPMENT
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST(SEARCH_DRIVER_SHIPMENT)
    suspend fun getDriverShipment(@Body credentials: Credentials): DriverShipmentDao

    @POST(SAVE_RECEIVER)
    suspend fun saveReceiverDetails(@Body receiver: Receiver) : SaveReceiverResponse
}
