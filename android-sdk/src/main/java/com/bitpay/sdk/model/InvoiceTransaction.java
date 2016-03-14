package com.bitpay.sdk.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Stores information about when (in which transaction in the blockchain was an invoice paid,
 * partially or totally.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.ANY)
public class InvoiceTransaction implements Parcelable {

	private String txid;
	private String type;
	private double amount;
    private Integer confirmations;
    private String time;
    private String receivedTime;
	
    public InvoiceTransaction() {}

	public String getTxid() {
		return txid;
	}
	public void setTxid(String _txid) {
        this.txid = _txid;
    }
	public String getType() {
		return type;
	}
	public void setType(String _type) {
		this.type = _type;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double _amount) {
		this.amount = _amount;
	}
    public Integer getConfirmations() {
        return confirmations;
    }
    public void setConfirmations(Integer confirmations) {
        this.confirmations = confirmations;
    }
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public String getReceivedTime() {
        return receivedTime;
    }
    public void setReceivedTime(String receivedTime) {
        this.receivedTime = receivedTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(txid);
        dest.writeString(type);
        dest.writeDouble(amount);
        dest.writeInt(confirmations == null ? 0 : confirmations);
        dest.writeString(time);
        dest.writeString(receivedTime);
    }

    public static final Parcelable.Creator<InvoiceTransaction> CREATOR = new Parcelable.Creator<InvoiceTransaction>() {
        public InvoiceTransaction createFromParcel(Parcel in) {

            InvoiceTransaction transaction = new InvoiceTransaction();
            transaction.setTxid(in.readString());
            transaction.setTxid(in.readString());
            transaction.setAmount(in.readDouble());
            transaction.setConfirmations(in.readInt());
            transaction.setTxid(in.readString());
            transaction.setTxid(in.readString());
            return transaction;
        }

        @Override
        public InvoiceTransaction[] newArray(int size) {
            return new InvoiceTransaction[size];
        }
    };
}