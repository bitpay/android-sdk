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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InvoiceActivity extends Activity implements NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback {

    public static final int RESULT_STATE_INVALID = 1;
    public static final int RESULT_EXPIRED = 2;
    public static final int RESULT_COMPLETE = 3;
    public static final int RESULT_CONFIRMED = 4;
    public static final int RESULT_PAID = 5;

    public static final String INVOICE = "invoice";
    private static final int FAKE_LOADING_MILLIS = 200;
    private static final int FAKE_LOADING_INTERVAL = 200;
    private static ScheduledExecutorService worker;
    private static final int FAKE_LOADING_PERCENT = 35;

    private long startTime;
    private int progress;
    private Invoice mInvoice = null;
    private WebView webView;
    private ProgressBar progressBar;
    private NfcAdapter mNfcAdapter;
    private Runnable command;
    private BitPayAndroid client;
    private AsyncTask<String, String, Void> followInvoiceTask;

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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getResourseIdByName(getPackageName(), "layout", "activity_invoice"));
        startTime = System.currentTimeMillis();
        worker = Executors.newSingleThreadScheduledExecutor();

        if (savedInstanceState != null) {
            mInvoice = savedInstanceState.getParcelable(INVOICE);
        } else {
            mInvoice = getIntent().getParcelableExtra(INVOICE);
        }
        webView = (WebView) findViewById(getResourseIdByName(getPackageName(), "id", "webView"));
        webView.getSettings().setJavaScriptEnabled(true);

        progressBar = (ProgressBar) findViewById(getResourseIdByName(getPackageName(), "id", "progressBar"));
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress < 100 && progressBar.getVisibility() == ProgressBar.GONE) {
                    progressBar.setVisibility(ProgressBar.VISIBLE);
                }
                InvoiceActivity.this.progress = progress;
                calculateFakedProgress(progress);
                if (progress == 100) {
                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                }
            }
        });
        command = new Runnable() {
            @Override
            public void run() {
                calculateFakedProgress(progress);
            }
        };
        worker.scheduleWithFixedDelay(command, FAKE_LOADING_INTERVAL, FAKE_LOADING_INTERVAL, TimeUnit.MILLISECONDS);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(mNfcAdapter != null) {
            // Register callback to set NDEF message
            mNfcAdapter.setNdefPushMessageCallback(this, this);

            // Register callback to listen for message-sent success
            mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
        } else {
            Log.i("InvoiceActivity", "NFC is not available on this device");
        }
        followInvoiceTask = new BitPayAndroid.FollowInvoiceStatusTask(new BitPayAndroid()) {

            @Override
            public void onStatePaid() {
                InvoiceActivity.this.setResult(RESULT_PAID);
                InvoiceActivity.this.finish();
                super.onStatePaid();
            }

            @Override
            public void onStateConfirmed() {
                InvoiceActivity.this.setResult(RESULT_CONFIRMED);
                InvoiceActivity.this.finish();
                super.onStateConfirmed();
            }

            @Override
            public void onStateComplete() {
                InvoiceActivity.this.setResult(RESULT_COMPLETE);
                InvoiceActivity.this.finish();
                super.onStateComplete();
            }

            @Override
            public void onStateExpired() {
                InvoiceActivity.this.setResult(RESULT_EXPIRED);
                InvoiceActivity.this.finish();
                super.onStateExpired();
            }

            @Override
            public void onStateInvalid() {
                InvoiceActivity.this.setResult(RESULT_STATE_INVALID);
                InvoiceActivity.this.finish();
                super.onStateInvalid();
            }
        }.execute(mInvoice.getId());
    }

    private void calculateFakedProgress(final int progress) {
        long currentTime = System.currentTimeMillis();
        int elapsedSinceStart = (int) (currentTime - startTime);
        final int tenthOfProgress = elapsedSinceStart / FAKE_LOADING_MILLIS;
        if (tenthOfProgress >= FAKE_LOADING_PERCENT) {
            worker.shutdown();
        }
        final int fakeProgress;
        if (progress == 100) {
            fakeProgress = 100;
        } else {
            fakeProgress = (tenthOfProgress >= FAKE_LOADING_PERCENT ? FAKE_LOADING_PERCENT : tenthOfProgress) + progress * (100 - FAKE_LOADING_PERCENT) / 100;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(fakeProgress);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.stopLoading();
        followInvoiceTask.cancel(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (webView.getUrl() == null || webView.getProgress() != 100) {
            webView.loadUrl(mInvoice.getUrl());
        }
        followInvoiceTask.execute(mInvoice.getId());
    }

    @Override
    protected void onStop() {
        super.onStop();
        worker.shutdown();
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
