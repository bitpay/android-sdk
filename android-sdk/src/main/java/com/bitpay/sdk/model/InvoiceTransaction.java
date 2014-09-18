package com.bitpay.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Stores information about when (in which transaction in the blockchain was an invoice paid,
 * partially or totally.
 */
public class InvoiceTransaction {

	private String _txid;
	private String _type;
	private double _amount;
    private Integer _confirmations;
    private String _time;
    private String _receivedTime;
	
    public InvoiceTransaction() {}
    
    @JsonIgnore
	public String getTxid() {
		return _txid;
	}
    
    @JsonProperty("txid")
	public void setTxid(String _txid) {
		this._txid = _txid;
	}

    @JsonIgnore
	public String getType() {
		return _type;
	}
    
    @JsonProperty("type")
	public void setType(String _type) {
		this._type = _type;
	}

    @JsonIgnore
	public double getAmount() {
		return _amount;
	}
    
    @JsonProperty("amount")
	public void setAmount(double _amount) {
		this._amount = _amount;
	}

    @JsonIgnore
    public Integer getConfirmations() {
        return _confirmations;
    }
    @JsonProperty("confirmations")
    public void setConfirmations(Integer confirmations) {
        this._confirmations = confirmations;
    }
    @JsonIgnore
    public String getTime() {
        return _time;
    }
    @JsonProperty("time")
    public void setTime(String time) {
        this._time = time;
    }
    @JsonIgnore
    public String getReceivedTime() {
        return _receivedTime;
    }
    @JsonProperty("receivedTime")
    public void setReceivedTime(String receivedTime) {
        this._receivedTime = receivedTime;
    }
}
