package com.bitpay.sdk.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.bitpay.sdk.controller.BitPayException;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
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
@JsonIgnoreProperties(ignoreUnknown = true)
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
        dest.writeLong(nonce);
        dest.writeString(guid);
        dest.writeString(token);
        dest.writeDouble(price);
        dest.writeString(currency);
        dest.writeString(posData);
        dest.writeString(notificationURL);
        dest.writeString(transactionSpeed);

        dest.writeInt(fullNotifications ? 1 : 0);
        dest.writeString(notificationEmail);
        dest.writeString(redirectURL);
        dest.writeString(orderId);
        dest.writeString(itemDesc);
        dest.writeString(itemCode);
        dest.writeInt(physical ? 1 : 0);

        dest.writeParcelable(buyerInfo, 0);

        dest.writeString(id);
        dest.writeString(url);
        dest.writeString(status);
        dest.writeString(btcPrice);
        dest.writeString(invoiceTime);
        dest.writeString(expirationTime);
        dest.writeString(currentTime);
        dest.writeString(btcPaid);
        dest.writeString(btcPrice);

        dest.writeString(rate);
        dest.writeString(exceptionStatus);

        dest.writeInt(transactions == null ? 0 : transactions.size());
        if (transactions != null) {
            for (InvoiceTransaction transaction : transactions) {
                dest.writeParcelable(transaction, 0);
            }
        }

        dest.writeInt(exRates == null ? 0 : exRates.size());
        if (exRates != null) {
            for (Map.Entry<String, String> entry : exRates.entrySet()) {
                dest.writeString(entry.getKey());
                dest.writeString(entry.getValue());
            }
        }
        dest.writeParcelable(paymentUrls, 0);
        if (refundAddresses == null) {
            dest.writeStringList(new ArrayList<String>());
        } else {
            dest.writeStringList(refundAddresses);
        }
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

            invoice.setBuyerInfo((BuyerInfo) in.readParcelable(getClass().getClassLoader()));

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
                invoice.transactions.add(in.<InvoiceTransaction>readParcelable(getClass().getClassLoader()));
            }

            invoice.exRates = new Hashtable<String, String>();
            read = in.readInt();
            for (int i = 0; i < read; i++) {
                String key = in.readString();
                String value = in.readString();
                invoice.exRates.put(key, value);
            }

            invoice.paymentUrls = in.readParcelable(getClass().getClassLoader());
            invoice.refundAddresses = new ArrayList<String>();
            in.readStringList(invoice.refundAddresses);

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
		
	private Long nonce = 0L;
	private String guid = "";
	private String token = "";
	
	private Double price;
	private String currency;
	private String posData = "";
	private String notificationURL = "";
	private String transactionSpeed = "";
	private boolean fullNotifications = false;
	private String notificationEmail = "";
	private String redirectURL = "";
	private String orderId = "";
	private String itemDesc = "";
	private String itemCode = "";
	private boolean physical = false;

	private BuyerInfo buyerInfo;
	
	private String id;
	private String url;
	private String status;
	private String btcPrice;
	private String invoiceTime;
	private String expirationTime;
	private String currentTime;
	private String btcPaid;
	private String btcDue;
	private List<InvoiceTransaction> transactions;
	private String rate;
	private Hashtable<String, String> exRates;
	private String exceptionStatus;
	private InvoicePaymentUrls paymentUrls;
    private String confirmations;
    private List<String> refundAddresses;
	
    public Invoice() {}

    /**
     * Create an invoice with a price and a currency
     *
     * @param price the price to be paid
     * @param currency the ISO code for the currency this invoice is established in
     */
    public Invoice(Double price, String currency)
    {
        this.price = price;
        this.currency = currency;
    }

    // API fields
    //

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public Long getNonce() {
		return nonce;
	}

	public void setNonce(Long nonce) {
		this.nonce = nonce;
	}

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

    // Required fields
    //

    @JsonProperty
	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

    @JsonProperty
	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) throws BitPayException {
        if (currency.length() != 3)
        {
            throw new BitPayException("Error: currency code must be exactly three characters");
        }
		this.currency = currency;
	}

    // Optional fields
    //
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public String getItemDesc() {
		return itemDesc;
	}

	public void setItemDesc(String itemDesc) {
		this.itemDesc = itemDesc;
	}

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public String getItemCode() {
		return itemCode;
	}

	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public String getPosData() {
		return posData;
	}

	public void setPosData(String posData) {
		this.posData = posData;
	}

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public String getNotificationURL() {
		return notificationURL;
	}

	public void setNotificationURL(String notificationURL) {
		this.notificationURL = notificationURL;
	}

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public String getTransactionSpeed() {
		return transactionSpeed;
	}

	public void setTransactionSpeed(String transactionSpeed) {
		this.transactionSpeed = transactionSpeed;
	}

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public Boolean getFullNotifications() {
		return fullNotifications;
	}

	public void setFullNotifications(Boolean fullNotifications) {
		this.fullNotifications = fullNotifications;
	}

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public String getNotificationEmail() {
		return notificationEmail;
	}

	public void setNotificationEmail(String notificationEmail) {
		this.notificationEmail = notificationEmail;
	}

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public String getRedirectURL() {
		return redirectURL;
	}

	public void setRedirectURL(String redirectURL) {
		this.redirectURL = redirectURL;
	}

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public Boolean getPhysical() {
		return physical;
	}

	public void setPhysical(boolean physical) {
		this.physical = physical;
	}

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @JsonProperty("buyerFields")
	public BuyerInfo getBuyerInfo() {
		return buyerInfo;
	}

    @JsonProperty("buyerFields")
	public void setBuyerInfo(BuyerInfo buyerInfo) {
		this.buyerInfo = buyerInfo;
	}

    // Response fields
    //

    @JsonIgnore
    public String getBIP21due() {
        return getPaymentUrls().getBIP21().substring(0, paymentUrls.getBIP21().indexOf("?amount=") + "?amount=".length()) + getBtcDue();
    }

    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getBtcPrice() {
		return btcPrice;
	}

	public void setBtcPrice(String btcPrice) {
		this.btcPrice = btcPrice;
	}

	public String getInvoiceTime() {
		return invoiceTime;
	}

	public void setInvoiceTime(String invoiceTime) {
		this.invoiceTime = invoiceTime;
	}

	public String getExpirationTime() {
		return expirationTime;
	}

	public void setExpirationTime(String expirationTime) {
		this.expirationTime = expirationTime;
	}

	public String getCurrentTime() {
		return currentTime;
	}

	public void setCurrentTime(String currentTime) {
		this.currentTime = currentTime;
	}

	public String getBtcPaid() {
		return btcPaid;
	}

	public void setBtcPaid(String btcPaid) {
		this.btcPaid = btcPaid;
	}

	public String getBtcDue() {
		return btcDue;
	}

	public void setBtcDue(String btcDue) {
		this.btcDue = btcDue;
	}

    public List<InvoiceTransaction> getTransactions() {
		return transactions;
	}

	public void setTransactions(List<InvoiceTransaction> transactions) {
		this.transactions = transactions;
	}

	public String getRate() {
		return rate;
	}

	public void setRate(String rate) {
		this.rate = rate;
	}

	public Hashtable<String, String> getExRates() {
		return exRates;
	}

	public void setExRates(Hashtable<String, String> exRates) {
		this.exRates = exRates;
	}

 	public String getExceptionStatus() {
 		return exceptionStatus;
 	}

 	public void setExceptionStatus(String exceptionStatus) {
 		this.exceptionStatus = exceptionStatus;
 	}

  	public InvoicePaymentUrls getPaymentUrls() {
  		return paymentUrls;
  	}

  	public void setPaymentUrls(InvoicePaymentUrls paymentUrls) {
  		this.paymentUrls = paymentUrls;
  	}

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public List<String> getRefundAddresses() {
        return refundAddresses;
    }

    public void setRefundAddresses(List<String> refundAddresses) {
        this.refundAddresses = refundAddresses;
    }
}
