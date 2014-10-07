package com.bitpay.sdk.android;

import android.app.Activity;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.bitpay.sdk.model.Invoice;
import com.bitpay.sdk.android.R;

public class InvoiceActivity extends Activity implements NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback {

    public static final String INVOICE = "invoice";
    public static final String PRIVATE_KEY = "privateKey";

    private String mEcKey = null;
    private Invoice mInvoice = null;
    private BitPayAndroid.GetClientTask clientTask;
    private BitPayAndroid.FollowInvoiceStatusTask invoiceTask;
    private WebView webView;
    private ProgressBar progressBar;
    private NfcAdapter mNfcAdapter;

    public static int getResourseIdByName(String packageName, String className, String name) {
        Class r = null;
        int id = 0;
        try {
            r = Class.forName(packageName + ".R");

            Class[] classes = r.getClasses();
            Class desireClass = null;

            for (int i = 0; i < classes.length; i++) {
                if(classes[i].getName().split("\\$")[1].equals(className)) {
                    desireClass = classes[i];

                    break;
                }
            }

            if(desireClass != null)
                id = desireClass.getField(name).getInt(desireClass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return id;

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(INVOICE, mInvoice);
        outState.putString(PRIVATE_KEY, mEcKey);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getResourseIdByName(getPackageName(), "layout", "activity_invoice"));

        if (savedInstanceState != null) {
            mInvoice = savedInstanceState.getParcelable(INVOICE);
            mEcKey = savedInstanceState.getString(PRIVATE_KEY);
        } else {
            mInvoice = getIntent().getParcelableExtra(INVOICE);
            mEcKey = getIntent().getStringExtra(PRIVATE_KEY);
        }
        webView = (WebView) findViewById(getResourseIdByName(getPackageName(), "id", "webView"));
        progressBar = (ProgressBar) findViewById(getResourseIdByName(getPackageName(), "id", "progressBar"));
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress < 100 && progressBar.getVisibility() == ProgressBar.GONE) {
                    progressBar.setVisibility(ProgressBar.VISIBLE);
                }
                progressBar.setProgress(progress);
                if (progress == 100) {
                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                }
            }
        });

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(mNfcAdapter != null) {
            // Register callback to set NDEF message
            mNfcAdapter.setNdefPushMessageCallback(this, this);

            // Register callback to listen for message-sent success
            mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
        } else {
            Log.i("InvoiceActivity", "NFC is not available on this device");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (clientTask != null) {
            clientTask.cancel(true);
        }
        if (invoiceTask != null) {
            invoiceTask.cancel(true);
        }
        webView.stopLoading();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (webView.getUrl() == null || webView.getProgress() != 100) {
            webView.loadUrl(mInvoice.getUrl());
        }
        clientTask = new BitPayAndroid.GetClientTask() {
            @Override
            protected void onPostExecute(final BitPayAndroid bitPay) {
                clientTask = null;
                if (bitPay != null) {

                    invoiceTask = new BitPayAndroid.FollowInvoiceStatusTask(bitPay) {

                        @Override
                        public void onStatePaid() {
                            finish();
                        }

                        @Override
                        public void onStateConfirmed() {
                            finish();
                        }

                        @Override
                        public void onStateComplete() {
                            finish();
                        }

                        @Override
                        public void onStateExpired() {
                            finish();
                        }
                        @Override
                        public void onStateInvalid() {
                            finish();
                        }

                    };
                    invoiceTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mInvoice.getId());
                }
            }
        };
        clientTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mEcKey);
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {

        if (mInvoice != null && mInvoice.getPaymentUrls() != null && mInvoice.getPaymentUrls().getBIP72b() != null) {
            return new NdefMessage(new NdefRecord[]{
                    NdefRecord.createUri(mInvoice.getPaymentUrls().getBIP72())
            });
        }
        return null;
    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {
        // Pass
    }
}