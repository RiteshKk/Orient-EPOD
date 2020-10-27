package com.ipssi.orient_epod.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Receiver implements Parcelable {

    public Receiver(String name, String phone, String invoiceNumber, String bagsRecv, String shortage, int loadType, String remarks, String sign, int isComplete, String location) {
        this.name = name;
        this.phone = phone;
        this.invoiceNumber = invoiceNumber;
        this.bagsRecv = bagsRecv;
        this.shortage = shortage;
        this.loadType = loadType;
        this.remarks = remarks;
        this.sign = sign;
        this.isComplete = isComplete;
        this.location = location;
    }

    private String name;
    private String phone;
    private String invoiceNumber;
    private String bagsRecv;
    private String shortage;
    private int loadType;
    private String remarks;
    private String sign;
    private int isComplete;
    private String location;

    protected Receiver(Parcel in) {
        name = in.readString();
        phone = in.readString();
        invoiceNumber = in.readString();
        bagsRecv = in.readString();
        shortage = in.readString();
        loadType = in.readInt();
        remarks = in.readString();
        sign = in.readString();
        isComplete = in.readInt();
        location = in.readString();
    }

    public static final Creator<Receiver> CREATOR = new Creator<Receiver>() {
        @Override
        public Receiver createFromParcel(Parcel in) {
            return new Receiver(in);
        }

        @Override
        public Receiver[] newArray(int size) {
            return new Receiver[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(phone);
        dest.writeString(invoiceNumber);
        dest.writeString(bagsRecv);
        dest.writeString(shortage);
        dest.writeInt(loadType);
        dest.writeString(remarks);
        dest.writeString(sign);
        dest.writeInt(isComplete);
        dest.writeString(location);
    }
}
