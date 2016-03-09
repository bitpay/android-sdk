package com.bitpay.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Stores the exchange rate for a given currency.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Rate {
	
	private String _name;
	private String _code;
	private double _value;
	
    public Rate() {}
    
    @JsonIgnore
	public String getName() {
		return _name;
	}
    
    @JsonProperty("name")
	public void setName(String _name) {
		this._name = _name;
	}

    @JsonIgnore
	public String getCode() {
		return _code;
	}
    
    @JsonProperty("code")
	public void setCode(String _code) {
		this._code = _code;
	}

    @JsonIgnore
	public double getValue() {
		return _value;
	}
    
    @JsonProperty("rate")
	public void setValue(double _value) {
		this._value = _value;
	}

    @Override
    public String toString() {
        return _name;
    }
}
