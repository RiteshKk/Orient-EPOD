package com.ipssi.orient_epod.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Receiver implements Parcelable {
    private String name;
    private String number;
    private String bagsReceived;
    private String shortage;
    private int shortageType;

    public Receiver(){}

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getBagsReceived() {
        return bagsReceived;
    }

    public void setBagsReceived(String bagsReceived) {
        this.bagsReceived = bagsReceived;
    }

    public String getShortage() {
        return shortage;
    }

    public void setShortage(String shortage) {
        this.shortage = shortage;
    }

    public int getShortageType() {
        return shortageType;
    }

    public void setShortageType(int shortageType) {
        this.shortageType = shortageType;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    private String remarks;
    private String signature;

    private Receiver(Parcel parcel) {
        name = parcel.readString();
        number = parcel.readString();
        bagsReceived = parcel.readString();
        shortage = parcel.readString();
        shortageType = parcel.readInt();
        remarks = parcel.readString();
        signature = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(number);
        dest.writeString(bagsReceived);
        dest.writeString(shortage);
        dest.writeInt(shortageType);
        dest.writeString(remarks);
        dest.writeString(signature);
    }
}
