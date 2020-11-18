package com.ipssi.orient_epod.model

import android.os.Parcel
import android.os.Parcelable

class Invoice() : Parcelable {
    var receiversCount = 0
    var invoiceNumber = ""
    var invoiceDate = ""
    var invoiceTime = ""
    var invoiceQuantity = ""
    var lrLink = ""
    var doNumber = ""
    var lrNumber = ""
    var destinationName = ""
    var shiptopartyCode = ""
    var shiptopartyName = ""
    var shiptopartyAddress = ""
    var shiptopartyAddress1 = ""
    var shiptopartyAddress2 = ""
    var shiptopartyMobileno = ""
    var soldtopartyName = ""
    var soldtopartyMobileno = ""
    var status = 0
    var invoiceStatus = 0
    var receivers = emptyList<Receiver>()
    var shipmentNumber = ""
    var shipmentCreationDate = ""
    var shipmentCreationTime = ""
    var vehicleNo = ""
    var transporterCode = ""
    var loadType = "standard"

    constructor(parcel: Parcel) : this() {
        receivers = parcel.createTypedArrayList(Receiver.CREATOR)!!
        receiversCount = parcel.readInt()
        invoiceNumber = parcel.readString() ?: ""
        invoiceDate = parcel.readString() ?: ""
        invoiceTime = parcel.readString() ?: ""
        invoiceQuantity = parcel.readString() ?: ""
        lrLink = parcel.readString() ?: ""
        doNumber = parcel.readString() ?: ""
        lrNumber = parcel.readString() ?: ""
        destinationName = parcel.readString() ?: ""
        shiptopartyCode = parcel.readString() ?: ""
        shiptopartyName = parcel.readString() ?: ""
        shiptopartyAddress = parcel.readString() ?: ""
        shiptopartyAddress1 = parcel.readString() ?: ""
        shiptopartyAddress2 = parcel.readString() ?: ""
        shiptopartyMobileno = parcel.readString() ?: ""
        soldtopartyName = parcel.readString() ?: ""
        soldtopartyMobileno = parcel.readString() ?: ""
        status = parcel.readInt()
        invoiceStatus = parcel.readInt()
        shipmentNumber = parcel.readString() ?: ""
        shipmentCreationDate = parcel.readString() ?: ""
        shipmentCreationTime = parcel.readString() ?: ""
        vehicleNo = parcel.readString() ?: ""
        transporterCode = parcel.readString() ?: ""
        loadType = parcel.readString() ?: "standard"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(receivers)
        parcel.writeInt(receiversCount)
        parcel.writeString(invoiceNumber)
        parcel.writeString(invoiceDate)
        parcel.writeString(invoiceTime)
        parcel.writeString(invoiceQuantity)
        parcel.writeString(lrLink)
        parcel.writeString(doNumber)
        parcel.writeString(lrNumber)
        parcel.writeString(destinationName)
        parcel.writeString(shiptopartyCode)
        parcel.writeString(shiptopartyName)
        parcel.writeString(shiptopartyAddress)
        parcel.writeString(shiptopartyAddress1)
        parcel.writeString(shiptopartyAddress2)
        parcel.writeString(shiptopartyMobileno)
        parcel.writeString(soldtopartyName)
        parcel.writeString(soldtopartyMobileno)
        parcel.writeInt(status)
        parcel.writeInt(invoiceStatus)
        parcel.writeString(shipmentNumber)
        parcel.writeString(shipmentCreationDate)
        parcel.writeString(shipmentCreationTime)
        parcel.writeString(vehicleNo)
        parcel.writeString(transporterCode)
        parcel.writeString(loadType)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Invoice> {
        override fun createFromParcel(parcel: Parcel): Invoice {
            return Invoice(parcel)
        }

        override fun newArray(size: Int): Array<Invoice?> {
            return arrayOfNulls(size)
        }
    }


}