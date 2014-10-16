package com.bitpay.sdk.android;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;

import com.bitpay.sdk.android.interfaces.PromiseCallback;
import com.bitpay.sdk.android.promises.BitpayPromise;
import com.bitpay.sdk.controller.BitPay;
import com.bitpay.sdk.controller.BitPayException;
import com.bitpay.sdk.controller.KeyUtils;
import com.bitpay.sdk.model.Invoice;
import com.bitpay.sdk.model.Rates;
import com.bitpay.sdk.model.Token;
import com.google.bitcoin.core.ECKey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Created by eordano on 9/10/14.
 */
public class BitPayAndroid extends BitPay implements Parcelable {

    private static final String EMPTY_BITCOIN_URI = "bitcoin:";
    private Executor executor = AsyncTask.THREAD_POOL_EXECUTOR;
    public static final List<String> END_STATUS = Arrays.asList(Invoice.STATUS_COMPLETE, Invoice.STATUS_INVALID, Invoice.EXSTATUS_FALSE, Invoice.STATUS_CONFIRMED);

    public BitPayAndroid(ECKey ecKey, String clientName, String envUrl) throws BitPayException {
        super(ecKey, clientName, envUrl);
    }

    public BitPayAndroid(String clientName, String envUrl) {
        super(clientName, envUrl);
    }

    public BitPayAndroid(Token token, String envUrl) {
        super(token, envUrl);
    }
    public BitPayAndroid(String clientName) throws BitPayException {
        super(clientName);
    }
    public BitPayAndroid() {
        super();
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public static boolean isWalletAvailable(Context context) {

        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(EMPTY_BITCOIN_URI));
        List resolveInfo = packageManager.queryIntentActivities(intent, 0);
        if (resolveInfo.size() > 0) {
            return true;
        }
        return false;
    }

    public String getPrivateKey() {
        if (_ecKey == null) {
            return null;
        }
        return KeyUtils.exportPrivateKeyToHexa(_ecKey);
    }

    public static HashMap<String, BitPayAndroid> clients = new HashMap<String, BitPayAndroid>();

    public BitpayPromise<Invoice> createNewInvoice(final Invoice builder) {
        return new BitpayPromise<Invoice>() {
            public void then(final PromiseCallback<Invoice> callback) {
                new CreateInvoiceTask(BitPayAndroid.this) {
                    @Override
                    protected void onPostExecute(Invoice invoice) {
                        if (invoice == null) {
                            callback.onError(error);
                        } else {
                            callback.onSuccess(invoice);
                        }
                    }
                }.executeOnExecutor(executor, builder);
            }
        };
    }

    public BitpayPromise<List<Invoice>> getInvoicesAsync(final String from, final String to) {
        return new BitpayPromise<List<Invoice>>() {
            @Override
            public void then(final PromiseCallback<List<Invoice>> callback) {
                new AsyncTask<String, Void, List<Invoice>>() {
                    BitPayException error;
                    @Override
                    protected List<Invoice> doInBackground(String... strings) {
                        try {
                            return getInvoices(strings[0], strings[1]);
                        } catch (BitPayException e) {
                            error = e;
                            return null;
                        }
                    }

                    @Override
                    protected void onPostExecute(List<Invoice> invoices) {
                        if (error != null) {
                            callback.onError(error);
                        } else {
                            callback.onSuccess(invoices);
                        }
                    }
                }.execute(from, to);
            }
        };
    }

    public BitpayPromise<Rates> getRatesAsync() {
        return new BitpayPromise<Rates>() {
            @Override
            public void then(final PromiseCallback<Rates> callback) {

                new AsyncTask<Void, Void, Rates>() {
                    protected BitPayException error;

                    @Override
                    protected final Rates doInBackground(Void... params) {
                        try {
                            return BitPayAndroid.this.getRates();
                        } catch (BitPayException e) {
                            this.error = e;
                            return null;
                        }
                    }

                    @Override
                    protected void onPostExecute(Rates invoice) {
                        if (invoice == null) {
                            callback.onError(error);
                        } else {
                            callback.onSuccess(invoice);
                        }
                    }
                }.executeOnExecutor(executor, null, null);
            }
        };
    }

    public static BitpayPromise<BitPayAndroid> getClient(final String privateKey, final String server, final Executor executor) {
        return new BitpayPromise<BitPayAndroid>() {
            @Override
            public void then(final PromiseCallback<BitPayAndroid> callback) {
                new GetClientTask(){
                    @Override
                    protected void onPostExecute(BitPayAndroid bitPayAndroid) {
                        if (bitPayAndroid == null) {
                            callback.onError(error);
                        } else {
                            callback.onSuccess(bitPayAndroid);
                        }
                    }
                }.executeOnExecutor(executor, privateKey, server);
            }
        };
    }

    public static BitpayPromise<BitPayAndroid> getClient(final String privateKey) {
        return getClient(privateKey, AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static BitpayPromise<BitPayAndroid> getClient(final String privateKey, final Executor executor) {
        return getClient(privateKey, "https://bitpay.com/", executor);
    }
    public static BitpayPromise<BitPayAndroid> getClient(final String privateKey, final String server) {
        return getClient(privateKey, server, AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static BitpayPromise<BitPayAndroid> withToken(final String token, final String serverUrl) {
        return withToken(token, serverUrl, AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static BitpayPromise<BitPayAndroid> withToken(final String token) {
        return withToken(token, "https://bitpay.com/", AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static BitpayPromise<BitPayAndroid> withToken(final String token, final String serverUrl, final Executor executor) {
        return new BitpayPromise<BitPayAndroid>() {
            @Override
            public void then(final PromiseCallback<BitPayAndroid> callback) {
                new GetClientWithTokenTask(){
                    @Override
                    protected void onPostExecute(BitPayAndroid bitPayAndroid) {
                        if (bitPayAndroid == null) {
                            callback.onError(error);
                        } else {
                            callback.onSuccess(bitPayAndroid);
                        }
                    }
                }.executeOnExecutor(executor, token, serverUrl);
            }
        };
    }

    public BitpayPromise<Void> authorizeClientAsync(final String token) {
        return new BitpayPromise<Void>() {
            @Override
            public void then(final PromiseCallback<Void> callback) {
                new AsyncTask<Void, Void, Void>(){

                    private BitPayException error;
                    @Override
                    protected Void doInBackground(Void... voids) {
                        try {
                            authorizeClient(token);
                        } catch (BitPayException e) {
                            error = e;
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        if (error != null) {
                            callback.onError(error);
                        } else {
                            callback.onSuccess(null);
                        }
                    }
                }.execute(null, null);
            }
        };
    }

    public BitpayPromise<List<Token>> createTokenAsync(final String facade) {
        return new BitpayPromise<List<Token>>() {
            @Override
            public void then(final PromiseCallback<List<Token>> callback) {
                new AsyncTask<Void, Void, List<Token>>(){

                    private BitPayException error;
                    @Override
                    protected List<Token> doInBackground(Void... voids) {
                        try {
                            return createToken(facade);
                        } catch (BitPayException e) {
                            error = e;
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(List<Token> tokens) {
                        if (error != null) {
                            callback.onError(error);
                        } else {
                            callback.onSuccess(tokens);
                        }
                    }
                }.execute(null, null);
            }
        };
    }

    public BitpayPromise<List<Token>> getTokensAsync() {
        return new BitpayPromise<List<Token>>() {
            @Override
            public void then(final PromiseCallback<List<Token>> callback) {
                new AsyncTask<Void, Void, List<Token>>(){

                    private BitPayException error;
                    @Override
                    protected List<Token> doInBackground(Void... voids) {
                        try {
                            getAccessTokens();
                            List<Token> result = new ArrayList<Token>();
                            for (Map.Entry<String, String> entry : _tokenCache.entrySet()) {
                                Token token = new Token();
                                token.setFacade(entry.getKey());
                                token.setValue(entry.getValue());
                                result.add(token);
                            }
                            return result;
                        } catch (BitPayException e) {
                            error = e;
                            return null;
                        }
                    }

                    @Override
                    protected void onPostExecute(List<Token> tokens) {
                        if (error != null) {
                            callback.onError(error);
                        } else {
                            callback.onSuccess(tokens);
                        }
                    }
                }.execute(null, null);
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(_baseUrl);
        dest.writeString(_clientName);
        dest.writeSerializable(_tokenCache);
        dest.writeString(getPrivateKey());
    }


    public static final Parcelable.Creator<BitPayAndroid> CREATOR = new Parcelable.Creator<BitPayAndroid>() {
        public BitPayAndroid createFromParcel(Parcel in) {
            String env = in.readString();
            String client = in.readString();
            BitPayAndroid result = new BitPayAndroid(client, env);

            result._tokenCache = (java.util.Hashtable<String, String>) in.readSerializable();
            String eckey = in.readString();
            if (eckey != null) {
                try {
                    result._ecKey = KeyUtils.loadEcKey(eckey);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return result;
        }

        @Override
        public BitPayAndroid[] newArray(int size) {
            return new BitPayAndroid[size];
        }
    };

    public static class GetClientTask extends AsyncTask<String, Void, BitPayAndroid> {
        protected BitPayException error;

        @Override
        protected final BitPayAndroid doInBackground(String... params) {

            try {
                String ecKey = params[0];
                if (clients.containsKey(ecKey)) {
                    return clients.get(ecKey);
                }
                BitPayAndroid client = new BitPayAndroid(KeyUtils.loadFromHexaEncodedPrivateKey(ecKey), "Android Client", "https://test.bitpay.com/");
                clients.put(ecKey, client);
                return client;
            } catch (BitPayException e) {
                this.error = e;
                return null;
            }
        }
    }

    public static class GetClientWithTokenTask extends AsyncTask<String, Void, BitPayAndroid> {
        protected BitPayException error;

        @Override
        protected final BitPayAndroid doInBackground(String... params) {
            String tokenStr = params[0];
            Token token = new Token();
            token.setFacade("pos");
            token.setValue(tokenStr);
            String url = params.length > 1? params[1] : null;
            BitPayAndroid client = new BitPayAndroid(token, url == null ? "https://test.bitpay.com/" : url);
            return client;
        }
    }

    public static class CreateInvoiceTask extends AsyncTask<Invoice, Void, Invoice> {

        private BitPayAndroid mBitpay;
        protected BitPayException error;

        public CreateInvoiceTask (BitPayAndroid bitpay) {
            mBitpay = bitpay;
        }

        @Override
        protected final Invoice doInBackground(Invoice... params) {
            try {
                return mBitpay.createInvoice(params[0], "pos");
            } catch (BitPayException e) {
                this.error = e;
                return null;
            }
        }
    }

    public static class FollowInvoiceStatusTask extends AsyncTask<String, Invoice, Void> {

        private static final long DELAY_MS = 3000;
        private BitPayAndroid mBitpay;
        public FollowInvoiceStatusTask (BitPayAndroid bitpay) {
            mBitpay = bitpay;
        }

        @Override
        protected final Void doInBackground(String... params) {
            while (true) {
                try {
                    try {
                        Invoice invoice = mBitpay.getInvoice(params[0]);
                        publishProgress(invoice);
                        if (END_STATUS.contains(invoice.getStatus())) {
                            return null;
                        }
                    } catch (BitPayException e) {
                    }
                    Thread.sleep(DELAY_MS);
                } catch (InterruptedException e) {
                    return null;
                }
            }
        }

        @Override
        protected void onProgressUpdate(Invoice... values) {
            if (values[0] == null) {
                return;
            }
            String newState = values[0].getStatus();
            if (newState.equals("paid")) {
                onStatePaid();
            }
            if (newState.equals("confirmed")) {
                onStateConfirmed();
            }
            if (newState.equals("complete")) {
                onStateComplete();
            }
            if (newState.equals("expired")) {
                onStateExpired();
            }
            if (newState.equals("invalid")) {
                onStateInvalid();
            }
        }

        public void onStatePaid() {
        }
        public void onStateConfirmed() {
        }
        public void onStateComplete() {
        }
        public void onStateExpired() {
        }
        public void onStateInvalid() {
        }
    }
}
