package com.ipssi.orient_epod.model;

import android.os.Parcel;
import android.os.Parcelable;

public class InvoiceDetail implements Parcelable {
    private String invoiceNo;
    private String invoiceDate;

    protected InvoiceDetail(Parcel in) {
        invoiceNo = in.readString();
        invoiceDate = in.readString();
        customerName = in.readString();
        customerAddress = in.readString();
        destination = in.readString();
        quantity = in.readString();
        lrNumber = in.readString();
        lrPdfLink = in.readString();
        location = in.readString();
    }

    public static final Creator<InvoiceDetail> CREATOR = new Creator<InvoiceDetail>() {
        @Override
        public InvoiceDetail createFromParcel(Parcel in) {
            return new InvoiceDetail(in);
        }

        @Override
        public InvoiceDetail[] newArray(int size) {
            return new InvoiceDetail[size];
        }
    };

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getLrNumber() {
        return lrNumber;
    }

    public void setLrNumber(String lrNumber) {
        this.lrNumber = lrNumber;
    }

    public String getLrPdfLink() {
        return lrPdfLink;
    }

    public void setLrPdfLink(String lrPdfLink) {
        this.lrPdfLink = lrPdfLink;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    private String customerName;
    private String customerAddress;
    private String destination;
    private String quantity;
    private String lrNumber;
    private String lrPdfLink;
    private String location;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(invoiceNo);
        dest.writeString(invoiceDate);
        dest.writeString(customerName);
        dest.writeString(customerAddress);
        dest.writeString(destination);
        dest.writeString(quantity);
        dest.writeString(lrNumber);
        dest.writeString(lrPdfLink);
        dest.writeString(location);
    }
}
