package com.bitpay.sdk.test;

import android.test.AndroidTestCase;

import com.bitpay.sdk.android.BitPayAndroid;
import com.bitpay.sdk.android.interfaces.BitpayPromiseCallback;
import com.bitpay.sdk.android.interfaces.PromiseCallback;
import com.bitpay.sdk.controller.BitPayException;
import com.bitpay.sdk.model.Invoice;

import java.util.concurrent.CountDownLatch;

/**
 * Created by eordano on 10/8/14.
 */
public class BitPayAndroidTest extends AndroidTestCase {
    public static final String invoiceToken = "MuwmfQQE54MqGquaR6WtzV";
    public static final String privateKey = "b64a9314661c62de31e79607d2e426cc2bd7a68b2f8c58af8ee17f1bc2635dc8";
    public static final Double TOLERANCE = 0.01;

    public Exception createInvoiceError;
    public Exception createClientError;
    public Exception createClient2Error;

    public void testCreateInvoice() {

        final CountDownLatch latch = new CountDownLatch(1);
        BitPayAndroid.withToken(invoiceToken, "https://test.bitpay.com/").then(new BitpayPromiseCallback() {
            @Override
            public void onSuccess(BitPayAndroid client) {
                client.createNewInvoice(new Invoice(10.00, "USD")).then(new PromiseCallback<Invoice>() {
                    @Override
                    public void onSuccess(Invoice promised) {
                        assertEquals(promised.getCurrency(), "USD");
                        assertTrue(Math.abs(promised.getPrice() - 10.00) < TOLERANCE);
                        latch.countDown();
                    }

                    @Override
                    public void onError(BitPayException e) {
                        createInvoiceError = e;
                        latch.countDown();
                    }
                });
            }

            @Override
            public void onError(BitPayException e) {
                createInvoiceError = e;
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertNull(createInvoiceError);
    }
    public void testCreateClient() {

        final CountDownLatch latch = new CountDownLatch(1);
        BitPayAndroid.withToken(invoiceToken, "https://test.bitpay.com/").then(new BitpayPromiseCallback() {
            @Override
            public void onSuccess(BitPayAndroid client) {
                assertNotNull(client);
                latch.countDown();
            }

            @Override
            public void onError(BitPayException e) {
                createClientError = e;
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertNull(createClientError);
    }
    public void testCreateClientWithIdentity() {
        final CountDownLatch latch = new CountDownLatch(1);
        BitPayAndroid.getClient(privateKey, "https://test.bitpay.com/").then(new BitpayPromiseCallback() {
            @Override
            public void onSuccess(BitPayAndroid client) {
                assertNotNull(client);
                assertEquals(client.getPrivateKey(), privateKey);
                latch.countDown();
            }

            @Override
            public void onError(BitPayException e) {
                createClient2Error = e;
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertNull(createClient2Error);
    }

}
