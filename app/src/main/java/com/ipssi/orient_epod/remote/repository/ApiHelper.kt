package com.ipssi.orient_epod.remote.repository


import com.ipssi.orient_epod.model.Credentials
import com.ipssi.orient_epod.remote.remote.ApiService

class ApiHelper(private val apiService: ApiService) {
    suspend fun getDriverShipment(credentials: Credentials) = apiService.getDriverShipment(credentials = credentials)
}