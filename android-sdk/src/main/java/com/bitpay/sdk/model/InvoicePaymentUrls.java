package com.bitpay.sdk.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Describes BIPs
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class InvoicePaymentUrls implements Parcelable {

	private String bip21 = "";
	private String bip72 = "";
	private String bip72b = "";
	private String bip73 = "";
	
    public InvoicePaymentUrls() {}

    @JsonIgnore
	public String getBIP21() {
		return bip21;
	}
        
    @JsonProperty("BIP21")
	public void setBIP21(String _BIP21) {
		this.bip21 = _BIP21;
	}

    @JsonIgnore
	public String getBIP72() {
		return bip72;
	}
        
    @JsonProperty("BIP72")
	public void setBIP72(String _BIP72) {
		this.bip72 = _BIP72;
	}

    @JsonIgnore
	public String getBIP72b() {
		return bip72b;
	}
        
    @JsonProperty("BIP72b")
	public void setBIP72b(String _BIP72b) {
		this.bip72b = _BIP72b;
	}

    @JsonIgnore
	public String getBIP73() {
		return bip73;
	}
        
    @JsonProperty("BIP73")
	public void setBIP73(String _BIP73) {
		this.bip73 = _BIP73;
	}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bip21);
        dest.writeString(bip72);
        dest.writeString(bip72b);
        dest.writeString(bip73);
    }

    public static final Parcelable.Creator<InvoicePaymentUrls> CREATOR = new Parcelable.Creator<InvoicePaymentUrls>() {
        public InvoicePaymentUrls createFromParcel(Parcel in) {

            InvoicePaymentUrls urls = new InvoicePaymentUrls();
            urls.setBIP21(in.readString());
            urls.setBIP72(in.readString());
            urls.setBIP72b(in.readString());
            urls.setBIP73(in.readString());
            return urls;
        }

        @Override
        public InvoicePaymentUrls[] newArray(int size) {
            return new InvoicePaymentUrls[size];
        }
    };
}
