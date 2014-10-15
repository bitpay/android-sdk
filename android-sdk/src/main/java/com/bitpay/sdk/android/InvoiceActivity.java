package com.bitpay.sdk.android;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bitpay.sdk.model.Invoice;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.File;
import java.util.Date;
import java.util.Hashtable;

public class InvoiceActivity extends Activity implements NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback {

    public static final int RESULT_STATE_INVALID = 1;
    public static final int RESULT_EXPIRED = 2;
    public static final int RESULT_COMPLETE = 3;
    public static final int RESULT_CONFIRMED = 4;
    public static final int RESULT_PAID = 5;

    public static final String INVOICE = "invoice";
    public static final String CLIENT = "bitpay";
    private static final String TRIGGERED_WALLET = "triggered";

    private boolean triggeredWallet;

    private NfcAdapter mNfcAdapter;
    private Invoice mInvoice = null;
    private BitPayAndroid client;
    private AsyncTask<String, String, Void> followInvoiceTask;
    private AsyncTask<Void, Void, Void> updateTimerTask;

    private ProgressBar progressBar;
    private ProgressBar loadingQr;
    private TextView status;
    private TextView price;

    private Button launchWallet;
    private TextView showQR;
    private ImageView qrView;

    private TextView address;
    private TextView timeRemaining;
    private TextView conversion;

    public static int getResourseIdByName(String packageName, String className, String name) {
        Class r = null;
        int id = 0;
        try {
            r = Class.forName(packageName + ".R");

            Class[] classes = r.getClasses();
            Class desireClass = null;

            for (int i = 0; i < classes.length; i++) {
                if (classes[i].getName().split("\\$")[1].equals(className)) {
                    desireClass = classes[i];

                    break;
                }
            }

            if (desireClass != null)
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
        outState.putBoolean(TRIGGERED_WALLET, triggeredWallet);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getResourseIdByName(getPackageName(), "layout", "activity_invoice"));

        status = (TextView) findViewById(getResourseIdByName(getPackageName(), "id", "status"));
        price = (TextView) findViewById(getResourseIdByName(getPackageName(), "id", "price"));

        launchWallet = (Button) findViewById(getResourseIdByName(getPackageName(), "id", "launchWallet"));
        progressBar = (ProgressBar) findViewById(getResourseIdByName(getPackageName(), "id", "progressBar"));
        loadingQr = (ProgressBar) findViewById(getResourseIdByName(getPackageName(), "id", "loadingQr"));
        qrView = (ImageView) findViewById(getResourseIdByName(getPackageName(), "id", "qr"));

        showQR = (TextView) findViewById(getResourseIdByName(getPackageName(), "id", "showQr"));
        address = (TextView) findViewById(getResourseIdByName(getPackageName(), "id", "address"));
        timeRemaining = (TextView) findViewById(getResourseIdByName(getPackageName(), "id", "timeRemaining"));
        conversion = (TextView) findViewById(getResourseIdByName(getPackageName(), "id", "conversion"));

        if (savedInstanceState != null) {
            mInvoice = savedInstanceState.getParcelable(INVOICE);
            client = savedInstanceState.getParcelable(CLIENT);
            triggeredWallet = savedInstanceState.getBoolean(TRIGGERED_WALLET);
        } else {
            mInvoice = getIntent().getParcelableExtra(INVOICE);
            client = getIntent().getParcelableExtra(CLIENT);
            triggeredWallet = getIntent().getBooleanExtra(TRIGGERED_WALLET, false);
        }

        progressBar.setRotation(180);
        price.setText(mInvoice.getBtcPrice() + " BTC");
        timeRemaining.setText(getRemainingTimeAsString());
        conversion.setText(mInvoice.getBtcPrice() + " BTC = " + mInvoice.getPrice() + mInvoice.getCurrency());
        address.setText(getAddress());
        address.setPaintFlags(address.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager ClipMan = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipMan.setPrimaryClip(ClipData.newPlainText("label", mInvoice.getPaymentUrls().getBIP73()));
                Toast toast = Toast.makeText(getApplicationContext(), "Copied payment address to clipboard", Toast.LENGTH_LONG);
                toast.show();
            }
        });
        showQR.setPaintFlags(showQR.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        showQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerQrLoad();
            }
        });
        launchWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent bitcoinIntent = new Intent(Intent.ACTION_VIEW);
                bitcoinIntent.setData(Uri.parse(mInvoice.getPaymentUrls().getBIP21()));
                startActivity(bitcoinIntent);
            }
        });

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter != null) {
            // Register callback to set NDEF message
            mNfcAdapter.setNdefPushMessageCallback(this, this);

            // Register callback to listen for message-sent success
            mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
        } else {
            Log.i("InvoiceActivity", "NFC is not available on this device");
        }
        if (!triggeredWallet) {
            if (BitPayAndroid.isWalletAvailable(this)) {
                Intent bitcoinIntent = new Intent(Intent.ACTION_VIEW);
                bitcoinIntent.setData(Uri.parse(mInvoice.getPaymentUrls().getBIP21()));
                triggeredWallet = true;
                startActivity(bitcoinIntent);
            } else {
                Toast.makeText(getApplicationContext(), "You don't have any bitcoin wallets installed.", Toast.LENGTH_LONG).show();
                triggerQrLoad();
            }
        } else {
            triggerStatusCheck();
        }
    }

    private void setupUpdateTimer() {
        updateTimerTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                while (true) {
                    publishProgress(null, null);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);

                int remainingSeconds = getRemainingSeconds();
                if (remainingSeconds < 0) {
                    timeRemaining.setText("-");
                    progressBar.setProgress(0);
                } else {
                    timeRemaining.setText(getRemainingTimeAsString());
                    progressBar.setProgress((getRemainingSeconds() * 100) / (15 * 60));
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null, null);
    }

    private void triggerStatusCheck() {
        if (followInvoiceTask != null) {
            followInvoiceTask.cancel(true);
        }
        if (client == null) {
            return;
        }
        followInvoiceTask = new BitPayAndroid.FollowInvoiceStatusTask(client) {

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

    @Override
    protected void onPause() {
        super.onPause();
        if (followInvoiceTask != null) {
            followInvoiceTask.cancel(true);
        }
        if (updateTimerTask != null) {
            updateTimerTask.cancel(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        triggerStatusCheck();
        setupUpdateTimer();
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

    public int getRemainingSeconds() {
        Date now = new Date();
        int millis = (int) (Math.abs(now.getTime() - Long.parseLong(mInvoice.getExpirationTime())));
        return millis / 1000;
    }

    public String getRemainingTimeAsString() {
        int seconds = getRemainingSeconds();
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    public String getAddress() {
        String bip21 = mInvoice.getPaymentUrls().getBIP21();
        return bip21.substring(bip21.indexOf(":") + 1, bip21.indexOf("?"));
    }

    public Bitmap generateQR(String text) {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x * 3 / 4;
        int height = width;
        MultiFormatWriter writer = new MultiFormatWriter();
        Hashtable hints = new Hashtable();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q);
        BitMatrix matrix = null;
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        try {
            matrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height, hints);
        } catch (WriterException e) {
            return bmp;
        }
        for (int x = 0; x < width; x++){
            for (int y = 0; y < height; y++){
                bmp.setPixel(x, y, matrix.get(x,y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bmp;
    }

    private void triggerQrLoad() {
        new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showQR.setVisibility(View.GONE);
                loadingQr.setVisibility(View.VISIBLE);
            }

            @Override
            protected Bitmap doInBackground(Void... params) {
                return generateQR(mInvoice.getPaymentUrls().getBIP73());
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                qrView.setImageBitmap(bitmap);
                loadingQr.setVisibility(View.GONE);
                qrView.setVisibility(View.VISIBLE);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null, null);
    }
}
