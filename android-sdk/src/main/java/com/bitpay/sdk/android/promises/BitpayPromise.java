package com.bitpay.sdk.android.promises;

import com.bitpay.sdk.android.interfaces.PromiseCallback;

/**
 * Created by eordano on 9/11/14.
 */
public abstract class BitpayPromise<Promised> {
    public abstract void then(PromiseCallback<Promised> callback);
}
