package com.bitpay.sdk.controller;

import com.bitpay.sdk.model.Invoice;
import com.bitpay.sdk.model.Rate;
import com.bitpay.sdk.model.Rates;
import com.bitpay.sdk.model.Token;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.bitcoin.core.ECKey;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BitPay {

    private static final String BITPAY_API_VERSION = "2.0.0";
    private static final String BITPAY_PLUGIN_INFO = "BitPay Java Client " + BITPAY_API_VERSION;
    private static final String BITPAY_URL = "https://bitpay.com/";

    public static final String FACADE_PAYROLL = "payroll";
    public static final String FACADE_POS = "pos";
    public static final String FACADE_MERCHANT = "merchant";
    public static final String FACADE_USER = "user";

    static private ObjectMapper mapper = new ObjectMapper();

    protected static HttpClient _httpClient = new DefaultHttpClient();
    protected String _baseUrl = BITPAY_URL;
    protected ECKey _ecKey = null;
    protected String _identity = "";
    protected long _nonce = new Date().getTime();
    protected boolean _disableNonce = false;
    protected String _clientName = "";
    protected Hashtable<String, String> _tokenCache; // {facade, token}

    /**
     * Constructor for use if the keys and SIN are managed by this library.
     *
     * @param clientName - The label for this client.
     * @param envUrl     - The target server URL.
     * @throws BitPayException
     */
    public BitPay(String clientName, String envUrl) {
        if (clientName.equals(BITPAY_PLUGIN_INFO)) {
            clientName += " on unknown host";
        }
        _clientName = clientName;

        _baseUrl = envUrl;
    }

    public BitPay(String clientName) throws BitPayException {
        this(clientName, BITPAY_URL);
    }

    public BitPay(Token token, String url) {
        _clientName = "Android Authorized Client";

        _baseUrl = url;

        _tokenCache = new Hashtable<String, String>();
        _tokenCache.put(token.getFacade(), token.getValue());
    }

    public BitPay() {
        this(BITPAY_PLUGIN_INFO, BITPAY_URL);
    }

    /**
     * Constructor for use if the keys and SIN were derived external to this library.
     *
     * @param ecKey      - An elliptical curve key.
     * @param clientName - The label for this client.
     * @param envUrl     - The target server URL.
     * @throws BitPayException
     */
    public BitPay(ECKey ecKey, String clientName, String envUrl) throws BitPayException {
        _ecKey = ecKey;
        this.deriveIdentity();
        _baseUrl = envUrl;
        this.tryGetAccessTokens();
    }

    public String getIdentity() {
        return _identity;
    }

    public boolean getDisableNonce() {
        return _disableNonce;
    }

    public void setDisableNonce(boolean value) {
        _disableNonce = value;
    }

    public void authorizeClient(String pairingCode) throws BitPayException {
        Token token = new Token();
        token.setId(_identity);
        token.setGuid(this.getGuid());
        token.setNonce(getNextNonce());
        token.setPairingCode(pairingCode);
        token.setLabel(_clientName);

        ObjectMapper mapper = new ObjectMapper();
        String json;
        try {
            json = mapper.writeValueAsString(token);
        } catch (JsonProcessingException e) {
            throw new BitPayException("Error - failed to serialize Token object : " + e.getMessage());
        }
        HttpResponse response = this.post("tokens", json);

        List<Token> tokens;
        try {
            tokens = Arrays.asList(mapper.readValue(this.responseToJsonString(response), TokenWrapper.class).data);
        } catch (JsonProcessingException e) {
            throw new BitPayException("Error - failed to deserialize BitPay server response (Tokens) : " + e.getMessage());
        } catch (IOException e) {
            throw new BitPayException("Error - failed to deserialize BitPay server response (Tokens) : " + e.getMessage());
        }
        for (Token t : tokens) {
            _tokenCache.put(t.getFacade(), t.getValue());
        }
    }


    public List<Token> createToken(String facade) throws BitPayException {
        Token token = new Token();
        token.setId(_identity);
        token.setGuid(this.getGuid());
        token.setNonce(getNextNonce());
        token.setLabel(_clientName);
        token.setFacade(facade);

        String json;
        try {
            json = mapper.writeValueAsString(token);
        } catch (JsonProcessingException e) {
            throw new BitPayException("Error - failed to serialize Token object : " + e.getMessage());
        }
        HttpResponse response = this.post("tokens", json);

        List<Token> tokens;
        try {
            tokens = Arrays.asList(mapper.readValue(this.responseToJsonString(response), TokenWrapper.class).data);
        } catch (JsonProcessingException e) {
            throw new BitPayException("Error - failed to deserialize BitPay server response (Tokens) : " + e.getMessage());
        } catch (IOException e) {
            throw new BitPayException("Error - failed to deserialize BitPay server response (Tokens) : " + e.getMessage());
        }
        for (Token t : tokens) {
            _tokenCache.put(t.getFacade(), t.getValue());
        }
        return tokens;
    }

    public boolean clientIsAuthorized(String facade) {
        return _tokenCache.containsKey(facade);
    }

    public Invoice createInvoice(Invoice invoice, String facade) throws BitPayException {
        invoice.setGuid(this.getGuid());

        String json;
        try {
            json = mapper.writeValueAsString(invoice);
        } catch (JsonProcessingException e) {
            throw new BitPayException("Error - failed to serialize Invoice object : " + e.getMessage());
        }

        HttpResponse response = this.makeRPCpost(facade, "createInvoice", json);

        try {
            invoice = mapper.readValue(this.responseToJsonString(response), InvoiceWrapper.class).data;
        } catch (JsonProcessingException e) {
            throw new BitPayException("Error - failed to deserialize BitPay server response (Invoice) : " + e.getMessage());
        } catch (IOException e) {
            throw new BitPayException("Error - failed to deserialize BitPay server response (Invoice) : " + e.getMessage());
        }
        return invoice;
    }

    public Invoice createInvoice(Invoice invoice) throws BitPayException {
        return this.createInvoice(invoice, FACADE_POS);
    }

    public Invoice getInvoice(String invoiceId) throws BitPayException {
        HttpResponse response = this.get("invoices/" + invoiceId);
        Invoice i;
        try {
            i = mapper.readValue(this.responseToJsonString(response), InvoiceWrapper.class).data;
        } catch (JsonProcessingException e) {
            throw new BitPayException("Error - failed to deserialize BitPay server response (Invoice) : " + e.getMessage());
        } catch (IOException e) {
            throw new BitPayException("Error - failed to deserialize BitPay server response (Invoice) : " + e.getMessage());
        }
        return i;
    }

    public List<Invoice> getInvoices(String dateStart, String dateEnd) throws BitPayException {
        Hashtable<String, String> parameters = this.getParams();
        parameters.put("token", this.getAccessToken(FACADE_MERCHANT));
        parameters.put("dateStart", dateStart);
        parameters.put("dateEnd", dateEnd);
        HttpResponse response = this.get("invoices", parameters);

        List<Invoice> invoices;
        try {
            invoices = mapper.readValue(this.responseToJsonString(response), InvoicesWrapper.class).data;
        } catch (JsonProcessingException e) {
            throw new BitPayException("Error - failed to deserialize BitPay server response (Invoices) : " + e.getMessage());
        } catch (IOException e) {
            throw new BitPayException("Error - failed to deserialize BitPay server response (Invoices) : " + e.getMessage());
        }
        return invoices;
    }

    public Rates getRates() throws BitPayException {
        HttpResponse response = this.get("rates");

        List<Rate> rates;
        try {
            rates = Arrays.asList(mapper.readValue(this.responseToJsonString(response), RatesWrapper.class).data);
        } catch (JsonProcessingException e) {
            throw new BitPayException("Error - failed to deserialize BitPay server response (Rates) : " + e.getMessage());
        } catch (IOException e) {
            throw new BitPayException("Error - failed to deserialize BitPay server response (Rates) : " + e.getMessage());
        }
        return new Rates(rates);
    }

    private void initKeys() {
        _ecKey = KeyUtils.createEcKey();
    }

    private void deriveIdentity() throws IllegalArgumentException {
        // Identity in this implementation is defined to be the SIN.
        _identity = KeyUtils.deriveSIN(_ecKey);
    }

    private long getNextNonce() {
        if (!getDisableNonce()) {
            _nonce++;
        } else {
            _nonce = 0;  // Nonce must be 0 when it has been disabled (0 value prevents serialization)
        }
        return _nonce;
    }

    private Hashtable<String, String> responseToTokenCache(HttpResponse response) throws BitPayException {
        // The response is expected to be an array of key/value pairs (facade name = token).
        String json = this.responseToJsonString(response);

        _tokenCache = new Hashtable<String, String>();
        try {
            for (Hashtable<String, String> entry1 : mapper.readValue(json, TokenCacheWrapper.class).data) {
                _tokenCache.putAll(entry1);
            }
        } catch (JsonProcessingException e) {
            throw new BitPayException("Error - failed to deserialize BitPay server response (Token array) : " + e.getMessage());
        } catch (IOException e) {
            throw new BitPayException("Error - failed to deserialize BitPay server response (Token array) : " + e.getMessage());
        }
        return _tokenCache;
    }

    private void clearAccessTokenCache() {
        _tokenCache = new Hashtable<String, String>();
    }

    private boolean tryGetAccessTokens() throws BitPayException {
        // Attempt to get access tokens for this client identity.
        try {
            // Success if at least one access token was returned.
            return this.getAccessTokens() > 0;
        } catch (BitPayException ex) {
            // If the error states that the identity is invalid then this client has not been
            // registered with the BitPay account.
            if (ex.getMessage().contains("Unauthorized sin")) {
                this.clearAccessTokenCache();
                return false;
            } else {
                // Propagate all other errors.
                throw ex;
            }
        }
    }

    public List<Token> getTokens() throws BitPayException {
        HttpResponse response = this.get("tokens", getParams());
        try {
            return Arrays.asList(mapper.readValue(this.responseToJsonString(response), TokenWrapper.class).data);
        } catch (JsonProcessingException e) {
            throw new BitPayException("Error - failed to deserialize BitPay server response (Invoice) : " + e.getMessage());
        } catch (IOException e) {
            throw new BitPayException("Error - failed to deserialize BitPay server response (Invoice) : " + e.getMessage());
        }
    }

    protected int getAccessTokens() throws BitPayException {
        this.clearAccessTokenCache();
        Hashtable<String, String> parameters = this.getParams();
        HttpResponse response = this.get("tokens", parameters);
        _tokenCache = responseToTokenCache(response);
        return _tokenCache.size();
    }

    private String getAccessToken(String facade) throws BitPayException {
        if (!_tokenCache.containsKey(facade)) {
            throw new BitPayException("Error: You do not have access to facade: " + facade);
        }
        return _tokenCache.get(facade);
    }

    private Hashtable<String, String> getParams() {
        Hashtable<String, String> params = new Hashtable<String, String>();
        params.put("nonce", getNextNonce() + "");
        return params;
    }

    private HttpResponse get(String uri, Hashtable<String, String> parameters) throws BitPayException {
        try {

            String fullURL = _baseUrl + uri;
            HttpGet get = new HttpGet(fullURL);
            if (parameters != null) {
                fullURL += "?";
                for (String key : parameters.keySet()) {
                    fullURL += key + "=" + parameters.get(key) + "&";
                }
                fullURL = fullURL.substring(0, fullURL.length() - 1);
                get.setURI(new URI(fullURL));
                String signature = KeyUtils.sign(_ecKey, fullURL);
                get.addHeader("x-bitpay-plugin-info", BITPAY_PLUGIN_INFO);
                get.addHeader("x-accept-version", BITPAY_API_VERSION);
                get.addHeader("x-signature", signature);
                get.addHeader("x-identity", KeyUtils.bytesToHex(_ecKey.getPubKey()));
            }
            return _httpClient.execute(get);

        } catch (URISyntaxException e) {
            throw new BitPayException("Error: GET failed\n" + e.getMessage());
        } catch (ClientProtocolException e) {
            throw new BitPayException("Error: GET failed\n" + e.getMessage());
        } catch (IOException e) {
            throw new BitPayException("Error: GET failed\n" + e.getMessage());
        }
    }

    private HttpResponse get(String uri) throws BitPayException {
        return this.get(uri, null);
    }

    private HttpResponse post(String uri, String json, boolean signatureRequired) throws BitPayException {
        try {
            HttpPost post = new HttpPost(_baseUrl + uri);
            post.setEntity(new ByteArrayEntity(json.toString().getBytes("UTF8")));
            if (signatureRequired) {
                String signature = KeyUtils.sign(_ecKey, _baseUrl + uri + json);
                post.addHeader("x-signature", signature);
                post.addHeader("x-identity", KeyUtils.bytesToHex(_ecKey.getPubKey()));
            }
            post.addHeader("x-accept-version", BITPAY_API_VERSION);
            post.addHeader("x-bitpay-plugin-info", BITPAY_PLUGIN_INFO);
            post.addHeader("Content-Type", "application/json");
            return _httpClient.execute(post);

        } catch (UnsupportedEncodingException e) {
            throw new BitPayException("Error: POST failed\n" + e.getMessage());
        } catch (ClientProtocolException e) {
            throw new BitPayException("Error: POST failed\n" + e.getMessage());
        } catch (IOException e) {
            throw new BitPayException("Error: POST failed\n" + e.getMessage());
        }
    }

    private HttpResponse post(String uri, String json) throws BitPayException {
        return this.post(uri, json, false);
    }

    private HttpResponse postWithSignature(String uri, String json) throws BitPayException {
        return this.post(uri, json, true);
    }

    private HttpResponse makeRPCpost(String facade, String method, String params) throws BitPayException {
        try {
            return post("api/" + _tokenCache.get(facade), new ObjectMapper().writeValueAsString(new RPCCall(method, params)));
        } catch (JsonProcessingException e) {
            throw new BitPayException("Unable to parse JSON", e);
        }
    }

    private class RPCCall {
        @JsonProperty
        private final String method;
        @JsonProperty
        private final String params;

        RPCCall(String method, String params) {
            this.method = method;
            this.params = params;
        }
    }

    private String responseToJsonString(HttpResponse response) throws BitPayException {
        if (response == null) {
            throw new BitPayException("Error: HTTP response is null");
        }
        try {
            // Get the JSON string from the response.
            HttpEntity entity = response.getEntity();
            String jsonString;
            jsonString = EntityUtils.toString(entity, "UTF-8");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonString);

            JsonNode node = rootNode.get("error");
            if (node != null) {
                throw new BitPayException("Error: " + jsonString);
            }

            node = rootNode.get("errors");
            if (node != null) {
                String message = "Multiple errors:";
                if (node.isArray()) {
                    for (final JsonNode errorNode : node) {
                        message += "\n" + errorNode.asText();
                    }
                    throw new BitPayException(message);
                }
            }

            return jsonString;

        } catch (ParseException e) {
            throw new BitPayException("Error - failed to retrieve HTTP response body : " + e.getMessage());
        } catch (JsonMappingException e) {
            throw new BitPayException("Error - failed to parse json response to map : " + e.getMessage());
        } catch (IOException e) {
            throw new BitPayException("Error - failed to retrieve HTTP response body : " + e.getMessage());
        }
    }

    private String getGuid() {
        int Min = 0;
        int Max = 99999999;
        return Min + (int) (Math.random() * ((Max - Min) + 1)) + "";
    }

    public Token newToken(String facade) {
        Token token = new Token();
        token.setId(_identity);
        token.setGuid(this.getGuid());
        token.setNonce(getNextNonce());
        token.setLabel(_clientName);
        token.setFacade(facade);
        return token;
    }

    public static class Wrapper<D> {
        public Wrapper() {
        }

        @JsonProperty("data")
        public D data;
        @JsonProperty("facade")
        public String facade;
    }

    public static class TokenWrapper extends Wrapper<Token[]> {
        public TokenWrapper() {
            super();
        }
    }

    public static class InvoiceWrapper extends Wrapper<Invoice> {
        public InvoiceWrapper() {
            super();
        }
    }

    public static class InvoicesWrapper extends Wrapper<List<Invoice>> {
        public InvoicesWrapper() {
            super();
        }
    }

    public static class TokenCacheWrapper extends Wrapper<Hashtable<String, String>[]> {
        public TokenCacheWrapper() {
            super();
        }
    }

    public static class RatesWrapper extends Wrapper<Rate[]> {
        public RatesWrapper() {
            super();
        }
    }
}
