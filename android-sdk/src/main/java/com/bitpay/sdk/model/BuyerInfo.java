package com.bitpay.sdk.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by eordano on 10/6/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.ANY)
public class BuyerInfo implements Parcelable {
    private String name;
    private String address1;
    private String address2;
    private String locality;
    private String region;
    private String postalCode;
    private String email;
    private String phone;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BuyerInfo() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(address1);
        dest.writeString(address2);
        dest.writeString(locality);
        dest.writeString(region);
        dest.writeString(postalCode);
        dest.writeString(email);
        dest.writeString(phone);

    }
    public static final Parcelable.Creator<BuyerInfo> CREATOR = new Parcelable.Creator<BuyerInfo>() {
        public BuyerInfo createFromParcel(Parcel in) {

            BuyerInfo buyerInfo = new BuyerInfo();
            buyerInfo.name = in.readString();
            buyerInfo.address1 = in.readString();
            buyerInfo.address2 = in.readString();
            buyerInfo.locality = in.readString();
            buyerInfo.region = in.readString();
            buyerInfo.postalCode = in.readString();
            buyerInfo.email = in.readString();
            buyerInfo.phone = in.readString();
            return buyerInfo;
        }

        @Override
        public BuyerInfo[] newArray(int size) {
            return new BuyerInfo[size];
        }
    };
}
