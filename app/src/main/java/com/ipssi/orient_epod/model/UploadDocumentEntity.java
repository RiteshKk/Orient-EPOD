package com.ipssi.orient_epod.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class UploadDocumentEntity implements Parcelable {
    private long id;
    private String shipment;
    private String invoice;
    private String doNo;
    private String lr;
    private String from1;
    private String name;
    private String file;
    private String image = null;
    private int status;
    private String createdAt = null;
    private String contentType;

    public long getId() {
        return id;
    }

    public String getShipment() {
        return shipment;
    }

    public String getInvoice() {
        return invoice;
    }

    public String getDoNo() {
        return doNo;
    }

    public String getLr() {
        return lr;
    }

    public String getFrom1() {
        return from1;
    }

    public String getName() {
        return name;
    }

    public String getFile() {
        return file;
    }

    public String getImage() {
        return image;
    }

    public int getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getContentType() {
        return contentType;
    }

    public String getDriverDocs() {
        return driverDocs;
    }

    private String driverDocs;

    public UploadDocumentEntity() {
    }

    public UploadDocumentEntity(String shipment, String invoice, String doNo, String lr, String from1, String name, String image) {
        this.id = 0;
        this.shipment = shipment;
        this.invoice = invoice;
        this.doNo = doNo;
        this.lr = lr;
        this.from1 = from1;
        this.name = name;
        this.file = name;
        this.status = 1;
        this.contentType = "image/*";
        this.driverDocs = image;
    }

    protected UploadDocumentEntity(Parcel in) {
        id = in.readLong();
        shipment = in.readString();
        invoice = in.readString();
        doNo = in.readString();
        lr = in.readString();
        from1 = in.readString();
        name = in.readString();
        file = in.readString();
        driverDocs = in.readString();
        image = in.readString();
        status = in.readInt();
        createdAt = in.readString();
        contentType = in.readString();
    }

    public static final Creator<UploadDocumentEntity> CREATOR = new Creator<UploadDocumentEntity>() {
        @Override
        public UploadDocumentEntity createFromParcel(Parcel in) {
            return new UploadDocumentEntity(in);
        }

        @Override
        public UploadDocumentEntity[] newArray(int size) {
            return new UploadDocumentEntity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(shipment);
        dest.writeString(invoice);
        dest.writeString(doNo);
        dest.writeString(lr);
        dest.writeString(from1);
        dest.writeString(name);
        dest.writeString(file);
        dest.writeString(driverDocs);
        dest.writeString(image);
        dest.writeInt(status);
        dest.writeString(createdAt);
        dest.writeString(contentType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UploadDocumentEntity)) return false;
        UploadDocumentEntity that = (UploadDocumentEntity) o;
        return getId() == that.getId() &&
            getShipment().equals(that.getShipment()) &&
            getInvoice().equals(that.getInvoice()) &&
            getDoNo().equals(that.getDoNo()) &&
            getLr().equals(that.getLr());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getShipment(), getInvoice(), getDoNo(), getLr());
    }
}
