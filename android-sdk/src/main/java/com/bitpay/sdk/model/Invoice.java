package com.bitpay.sdk.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.bitpay.sdk.controller.BitPayException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Model for an Invoice object.
 *
 * It also serves as a builder object for Invoices.
 */
public class Invoice implements Parcelable {

    static private ObjectMapper mapper = new ObjectMapper();

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Serializes the invoice into a Parcel
     *
     * @param dest
     * @param flags
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(_nonce);
        dest.writeString(_guid);
        dest.writeString(_token);
        dest.writeDouble(_price);
        dest.writeString(_currency);
        dest.writeString(_posData);
        dest.writeString(_notificationURL);
        dest.writeString(_transactionSpeed);

        dest.writeInt(_fullNotifications ? 1 : 0);
        dest.writeString(_notificationEmail);
        dest.writeString(_redirectURL);
        dest.writeString(_orderId);
        dest.writeString(_itemDesc);
        dest.writeString(_itemCode);
        dest.writeInt(_physical ? 1 : 0);

        dest.writeString(_buyerName);
        dest.writeString(_buyerAddress1);
        dest.writeString(_buyerAddress2);
        dest.writeString(_buyerCity);
        dest.writeString(_buyerState);
        dest.writeString(_buyerZip);
        dest.writeString(_buyerCountry);
        dest.writeString(_buyerEmail);
        dest.writeString(_buyerPhone);

        dest.writeString(_id);
        dest.writeString(_url);
        dest.writeString(_status);
        dest.writeString(_btcPrice);
        dest.writeString(_invoiceTime);
        dest.writeString(_expirationTime);
        dest.writeString(_currentTime);
        dest.writeString(_btcPaid);
        dest.writeString(_btcPrice);

        dest.writeString(_rate);
        dest.writeString(_exceptionStatus);

        dest.writeInt(_transactions.size());
        for (InvoiceTransaction transaction : _transactions) {
            dest.writeString(transaction.getTxid());
            dest.writeString(transaction.getType());
            dest.writeDouble(transaction.getAmount());
            dest.writeInt(transaction.getConfirmations());
            dest.writeString(transaction.getTime());
            dest.writeString(transaction.getReceivedTime());
        }

        dest.writeInt(_exRates.size());
        for (Map.Entry<String, String> entry : _exRates.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeString(entry.getValue());
        }
        dest.writeString(_paymentUrls.getBIP21());
        dest.writeString(_paymentUrls.getBIP72());
        dest.writeString(_paymentUrls.getBIP72b());
        dest.writeString(_paymentUrls.getBIP73());
    }

    public static final Parcelable.Creator<Invoice> CREATOR = new Parcelable.Creator<Invoice>() {
        public Invoice createFromParcel(Parcel in) {

            Invoice invoice = new Invoice();
            invoice.setNonce(in.readLong());
            invoice.setGuid(in.readString());
            invoice.setToken(in.readString());
            invoice.setPrice(in.readDouble());
            try {
                invoice.setCurrency(in.readString());
            } catch (BitPayException e) {
                throw new RuntimeException(e);
            }
            invoice.setPosData(in.readString());
            invoice.setNotificationURL(in.readString());
            invoice.setTransactionSpeed(in.readString());

            Integer read = in.readInt();
            if (read != null && read == 1) {
                invoice.setFullNotifications(true);
            }
            invoice.setNotificationEmail(in.readString());
            invoice.setRedirectURL(in.readString());
            invoice.setOrderId(in.readString());
            invoice.setItemDesc(in.readString());
            invoice.setItemCode(in.readString());
            read = in.readInt();
            if (read != null && read == 1) {
                invoice.setPhysical(true);
            }

            invoice.setBuyerName(in.readString());
            invoice.setBuyerAddress1(in.readString());
            invoice.setBuyerAddress2(in.readString());
            invoice.setBuyerCity(in.readString());
            invoice.setBuyerState(in.readString());
            invoice.setBuyerZip(in.readString());
            invoice.setBuyerCountry(in.readString());
            invoice.setBuyerEmail(in.readString());
            invoice.setBuyerPhone(in.readString());

            invoice.setId(in.readString());
            invoice.setUrl(in.readString());
            invoice.setStatus(in.readString());
            invoice.setBtcPrice(in.readString());
            invoice.setInvoiceTime(in.readString());
            invoice.setExpirationTime(in.readString());
            invoice.setCurrentTime(in.readString());
            invoice.setBtcPaid(in.readString());
            invoice.setBtcDue(in.readString());

            invoice.setRate(in.readString());
            invoice.setExceptionStatus(in.readString());

            invoice.setTransactions(new ArrayList<InvoiceTransaction>());
            read = in.readInt();
            for (int i = 0; i < read; i++) {
                InvoiceTransaction transaction = new InvoiceTransaction();
                transaction.setTxid(in.readString());
                transaction.setType(in.readString());
                transaction.setAmount(in.readDouble());
                transaction.setConfirmations(in.readInt());
                transaction.setTime(in.readString());
                transaction.setReceivedTime(in.readString());
                invoice._transactions.add(transaction);
            }

            invoice._exRates = new Hashtable<String, String>();
            read = in.readInt();
            for (int i = 0; i < read; i++) {
                String key = in.readString();
                String value = in.readString();
                invoice._exRates.put(key, value);
            }

            invoice._paymentUrls = new InvoicePaymentUrls();
            invoice._paymentUrls.setBIP21(in.readString());
            invoice._paymentUrls.setBIP72(in.readString());
            invoice._paymentUrls.setBIP72b(in.readString());
            invoice._paymentUrls.setBIP73(in.readString());

            return invoice;
        }

        public Invoice[] newArray(int size) {
            return new Invoice[size];
        }
    };

	public static final String STATUS_NEW = "new";
	public static final String STATUS_PAID = "paid";
	public static final String STATUS_CONFIRMED = "confirmed";
	public static final String STATUS_COMPLETE = "complete";
	public static final String STATUS_INVALID = "invalid";
	public static final String EXSTATUS_FALSE = "false";
	public static final String EXSTATUS_PAID_OVER = "paidOver";
	public static final String EXSTATUS_PAID_PARTIAL = "paidPartial";
		
	private Long _nonce = 0L;
	private String _guid = "";
	private String _token = "";
	
	private Double _price;
	private String _currency;
	private String _posData = "";
	private String _notificationURL = "";
	private String _transactionSpeed = "";
	private boolean _fullNotifications = false;
	private String _notificationEmail = "";
	private String _redirectURL = "";
	private String _orderId = "";
	private String _itemDesc = "";
	private String _itemCode = "";
	private boolean _physical = false;

	private String _buyerName = "";
	private String _buyerAddress1 = "";
	private String _buyerAddress2 = "";
	private String _buyerCity = "";
	private String _buyerState = "";
	private String _buyerZip = "";
	private String _buyerCountry = "";
	private String _buyerEmail = "";
	private String _buyerPhone = "";
	
	private String _id;
	private String _url;
	private String _status;
	private String _btcPrice;
	private String _invoiceTime;
	private String _expirationTime;
	private String _currentTime;
	private String _btcPaid;
	private String _btcDue;
	private List<InvoiceTransaction> _transactions;
	private String _rate;
	private Hashtable<String, String> _exRates;
	private String _exceptionStatus;
	private InvoicePaymentUrls _paymentUrls;
    private String _confirmations;
	
    public Invoice() {}

    /**
     * Create an invoice with a price and a currency
     *
     * @param price the price to be paid
     * @param currency the ISO code for the currency this invoice is established in
     */
    public Invoice(Double price, String currency)
    {
        this._price = price;
        this._currency = currency;
    }

    // API fields
    //
	
    @JsonProperty("guid")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public String getGuid() {
		return _guid;
	}
	
    @JsonProperty("guid")
	public void setGuid(String _guid) {
		this._guid = _guid;
	}

    @JsonProperty("nonce")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public Long getNonce() {
		return _nonce;
	}
	
    @JsonProperty("nonce")
	public void setNonce(Long _nonce) {
		this._nonce = _nonce;
	}

    @JsonProperty("token")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public String getToken() {
		return _token;
	}
	
    @JsonProperty("token")
	public void setToken(String _token) {
		this._token = _token;
	}

    // Required fields
    //

    @JsonProperty("price")
	public Double getPrice() {
		return _price;
	}
	
    @JsonProperty("price")
	public void setPrice(Double _price) {
		this._price = _price;
	}

    @JsonProperty("currency")
	public String getCurrency() {
		return _currency;
	}
	
    @JsonProperty("currency")
	public void setCurrency(String _currency) throws BitPayException {
        if (_currency.length() != 3)
        {
            throw new BitPayException("Error: currency code must be exactly three characters");
        }
		this._currency = _currency;
	}

    // Optional fields
    //

    @JsonProperty("orderId")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public String getOrderId() {
		return _orderId;
	}
	
    @JsonProperty("orderId")
	public void setOrderId(String _orderId) {
		this._orderId = _orderId;
	}

    @JsonProperty("itemDesc")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public String getItemDesc() {
		return _itemDesc;
	}
	
    @JsonProperty("itemDesc")
	public void setItemDesc(String _itemDesc) {
		this._itemDesc = _itemDesc;
	}

    @JsonProperty("itemCode")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public String getItemCode() {
		return _itemCode;
	}
	
    @JsonProperty("itemCode")
	public void setItemCode(String _itemCode) {
		this._itemCode = _itemCode;
	}

    @JsonProperty("posData")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public String getPosData() {
		return _posData;
	}
	
    @JsonProperty("posData")
	public void setPosData(String _posData) {
		this._posData = _posData;
	}

    @JsonProperty("notificationURL")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public String getNotificationURL() {
		return _notificationURL;
	}
	
    @JsonProperty("notificationURL")
	public void setNotificationURL(String _notificationURL) {
		this._notificationURL = _notificationURL;
	}

    @JsonProperty("transactionSpeed")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public String getTransactionSpeed() {
		return _transactionSpeed;
	}
	
    @JsonProperty("transactionSpeed")
	public void setTransactionSpeed(String _transactionSpeed) {
		this._transactionSpeed = _transactionSpeed;
	}

    @JsonProperty("fullNotifications")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public Boolean getFullNotifications() {
		return _fullNotifications;
	}
	
    @JsonProperty("fullNotifications")
	public void setFullNotifications(Boolean _fullNotifications) {
		this._fullNotifications = _fullNotifications;
	}

    @JsonProperty("notificationEmail")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public String getNotificationEmail() {
		return _notificationEmail;
	}
	
    @JsonProperty("notificationEmail")
	public void setNotificationEmail(String _notificationEmail) {
		this._notificationEmail = _notificationEmail;
	}

    @JsonProperty("redirectURL")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public String getRedirectURL() {
		return _redirectURL;
	}
	
    @JsonProperty("redirectURL")
	public void setRedirectURL(String _redirectURL) {
		this._redirectURL = _redirectURL;
	}

    @JsonProperty("physical")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public Boolean getPhysical() {
		return _physical;
	}
	
    @JsonProperty("physical")
	public void setPhysical(boolean _physical) {
		this._physical = _physical;
	}

    @JsonProperty("buyerName")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public String getBuyerName() {
		return _buyerName;
	}
	
    @JsonProperty("buyerName")
	public void setBuyerName(String _buyerName) {
		this._buyerName = _buyerName;
	}

    @JsonProperty("buyerAddress1")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public String getBuyerAddress1() {
		return _buyerAddress1;
	}
	
    @JsonProperty("buyerAddress1")
	public void setBuyerAddress1(String _buyerAddress1) {
		this._buyerAddress1 = _buyerAddress1;
	}

    @JsonProperty("buyerAddress2")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public String getBuyerAddress2() {
		return _buyerAddress2;
	}
	
    @JsonProperty("buyerAddress2")
	public void setBuyerAddress2(String _buyerAddress2) {
		this._buyerAddress2 = _buyerAddress2;
	}

    @JsonProperty("buyerCity")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public String getBuyerCity() {
		return _buyerCity;
	}
	
    @JsonProperty("buyerCity")
	public void setBuyerCity(String _buyerCity) {
		this._buyerCity = _buyerCity;
	}

    @JsonProperty("buyerState")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public String getBuyerState() {
		return _buyerState;
	}
	
    @JsonProperty("buyerState")
	public void setBuyerState(String _buyerState) {
		this._buyerState = _buyerState;
	}

    @JsonProperty("buyerZip")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public String getBuyerZip() {
		return _buyerZip;
	}
	
    @JsonProperty("buyerZip")
	public void setBuyerZip(String _buyerZip) {
		this._buyerZip = _buyerZip;
	}

    @JsonProperty("buyerCountry")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public String getBuyerCountry() {
		return _buyerCountry;
	}
	
    @JsonProperty("buyerCountry")
	public void setBuyerCountry(String _buyerCountry) {
		this._buyerCountry = _buyerCountry;
	}

    @JsonProperty("buyerEmail")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public String getBuyerEmail() {
		return _buyerEmail;
	}
	
    @JsonProperty("buyerEmail")
	public void setBuyerEmail(String _buyerEmail) {
		this._buyerEmail = _buyerEmail;
	}

    @JsonProperty("buyerPhone")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public String getBuyerPhone() {
		return _buyerPhone;
	}
	
    @JsonProperty("buyerPhone")
	public void setBuyerPhone(String _buyerPhone) {
		this._buyerPhone = _buyerPhone;
	}
 
    // Response fields
    //

    @JsonIgnore
	public String getId() {
		return _id;
	}
	
    @JsonProperty("id")
	public void setId(String _id) {
		this._id = _id;
	}

    @JsonIgnore
	public String getUrl() {
		return _url;
	}
	
    @JsonProperty("url")
	public void setUrl(String _url) {
		this._url = _url;
	}

    @JsonIgnore
	public String getStatus() {
		return _status;
	}
	
    @JsonProperty("status")
	public void setStatus(String _status) {
		this._status = _status;
	}

    @JsonIgnore
	public String getBtcPrice() {
		return _btcPrice;
	}
	
    @JsonProperty("btcPrice")
	public void setBtcPrice(String _btcPrice) {
		this._btcPrice = _btcPrice;
	}

    @JsonIgnore
	public String getInvoiceTime() {
		return _invoiceTime;
	}
	
    @JsonProperty("invoiceTime")
	public void setInvoiceTime(String _invoiceTime) {
		this._invoiceTime = _invoiceTime;
	}

    @JsonIgnore
	public String getExpirationTime() {
		return _expirationTime;
	}
	
    @JsonProperty("expirationTime")
	public void setExpirationTime(String _expirationTime) {
		this._expirationTime = _expirationTime;
	}

    @JsonIgnore
	public String getCurrentTime() {
		return _currentTime;
	}
	
    @JsonProperty("currentTime")
	public void setCurrentTime(String _currentTime) {
		this._currentTime = _currentTime;
	}

    @JsonIgnore
	public String getBtcPaid() {
		return _btcPaid;
	}
	
    @JsonProperty("btcPaid")
	public void setBtcPaid(String _btcPaid) {
		this._btcPaid = _btcPaid;
	}

    @JsonIgnore
	public String getBtcDue() {
		return _btcDue;
	}
	
    @JsonProperty("btcDue")
	public void setBtcDue(String _btcDue) {
		this._btcDue = _btcDue;
	}

    @JsonIgnore
	public List<InvoiceTransaction> getTransactions() {
		return _transactions;
	}
	
    @JsonProperty("transactions")
	public void setTransactions(List<InvoiceTransaction> _transactions) {
		this._transactions = _transactions;
	}

    @JsonIgnore
	public String getRate() {
		return _rate;
	}
	
    @JsonProperty("rate")
	public void setRate(String _rate) {
		this._rate = _rate;
	}

    @JsonIgnore
	public Hashtable<String, String> getExRates() {
		return _exRates;
	}
	
    @JsonProperty("exRates")
	public void setExRates(Hashtable<String, String> _exRates) {
		this._exRates = _exRates;
	}

    @JsonIgnore
 	public String getExceptionStatus() {
 		return _exceptionStatus;
 	}
 	
     @JsonProperty("exceptionStatus")
 	public void setExceptionStatus(String _exceptionStatus) {
 		this._exceptionStatus = _exceptionStatus;
 	}

     @JsonIgnore
  	public InvoicePaymentUrls getPaymentUrls() {
  		return _paymentUrls;
  	}
  	
      @JsonProperty("paymentUrls")
  	public void setPaymentUrls(InvoicePaymentUrls _paymentUrls) {
  		this._paymentUrls = _paymentUrls;
  	}

}
