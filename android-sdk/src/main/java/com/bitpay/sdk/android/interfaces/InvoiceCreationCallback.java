package com.bitpay.sdk.android.interfaces;

import com.bitpay.sdk.controller.BitPayException;
import com.bitpay.sdk.model.Invoice;

/**
 * Created by eordano on 9/11/14.
 */
public interface InvoiceCreationCallback {
    public void onSuccess(Invoice invoice);
    public void onError(BitPayException exception);
}
