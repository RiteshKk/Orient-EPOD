package com.ipssi.orient_epod.remote.util

const val BASE_URL = "http://61.0.231.147:4000/"
//const val BASE_URL = "http://182.72.88.12:4000/"
const val SEARCH_DRIVER_SHIPMENT = "searchDriverShipments"
const val SAVE_RECEIVER = "saveReceiver"
const val GET_RECEIVERS = "getInvoiceReceivers/{invoice}"
const val UPLOAD_IMAGE = "uploadDriverDocs"
const val GET_UPLOADED_IMAGE = "getDriverDocs/{shipment}/{invoice}/{do_no}/{lr}/{from1}"
const val DELETE_UPLOADED_IMAGE = "deleteCustDocs/{id}"
const val SAVE_DRIVER_LOCATION = "saveDriverLocation"
const val SEND_OTP = "sendDriverOtp/{phone}"