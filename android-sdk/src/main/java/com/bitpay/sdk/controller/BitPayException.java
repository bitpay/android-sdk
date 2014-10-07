package com.bitpay.sdk.controller;

public class BitPayException extends Exception {

	public BitPayException(String message) {
		super(message);
	}

	private static final long serialVersionUID = 1L;

    public BitPayException(String s, Exception e) {
        super(s, e);
    }
}
