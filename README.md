# BitPay Android SDK

This SDK allows your application to quickly create an invoice, show the user an option to pay you,
and track the status of the payment. Accept bitcoins in your app with 10 lines of code!

## Installing

You'll need to add this SDK as a dependency to your Android project. This process varies according
to the build system you're using.

### Gradle

If you're using Android Studio or gradle to build your app, add the following
to your `build.gradle` file:

    compile 'com.bitpay:sdk-android:0.1.1@aar'

Remember to add the maven central repository at the beginning of the file:

    repositories {
        mavenCentral()
    }


### Maven dependency

If you're using maven, add the following dependency:
```xml
<dependency>
  <groupId>com.bitpay</groupId>
  <artifactId>sdk-android</artifactId>
  <version>0.1.1</version>
  <packaging>aar</packaging>
</dependency>
```

### AAR library

A `.aar` library is provided in the `dist` folder, so you can include it in your Android proyect.

## Setup credentials

### 1. Create a Bitpay Account
Please go to https://bitpay.com to create an account.

### 2. Generate an Application Key

Go to [*My Account* > *API Tokens*](https://bitpay.com/api-tokens) section. Under *Tokens* create a new token with label `mobile` and facade `Point-of-Sale`.

Open the bin folder and excecute the pairing utility using the created token.
```bash
    $ cd bin
    $ npm install bitauth
    $ ./createClientKey <pairing code, 7 letters>
    Your client key is:
    70163c90f18df866d7a4ec3b8f7215f0013e3f81749f6222938a1f4d9ce3e97e
```
This key can now be used to instantiate a Bitpay client object.

## Sample Code and Usage

### Creating a BitPayClient
```java
String clientKey = "00000000000000000000000";
new BitPayAndroid.GetBitPayClientTask() {
    @Override
    protected void onPostExecute(BitPayAndroid bitpay) {
        // ...
    }
}.execute(clientKey);
```

Inside the `onPostExecute` method, you can use all of the [BitPay Java SDK](https://github.com/unChaz/BitPayJavaClient)
(the original BitPay object is accessed through `bitpay.mBitpay`).

#### Using promises
```java
String clientKey = "00000000000000000000000";
BitPayAndroid.getClient(clientKey).then(new BitpayPromiseCallback() {

    public void onSuccess(BitPayAndroid bitpay) {
        // ...
    }

    public void onError(BitPayException e) {
        // ...
    }
});
```

### Creating a new invoice
```java
Invoice invoice = new Invoice(200, "USD");
new BitPayAndroid.CreateInvoiceTask(bitpay) {
    @Override
    protected void onPostExecute(Invoice invoice) {
        // ...
    }
}.execute(invoice);
```

#### Using promises
```java
bitpay.createInvoice(new Invoice(200, "USD")).then(new InvoicePromiseCallback() {

    public void onSuccess(Invoice invoice) {
        // ...
    }
    public void onError(BitPayException e) {
        // ...
    }
});
```

### The InvoiceActivity class

This class helps your customer track the status of his payment, shows a QR
code and a button to send him to his wallet, so he can pay.
```java
Intent invoiceIntent = new Intent(this, InvoiceActivity.class);
invoiceIntent.putExtra(InvoiceActivity.INVOICE_ID, invoice.getId());
startActivity(invoiceIntent);
```

## More Samples and Documentation

### Sample Project
Take a look at [this project](https://github.com/eordano/bitpay-android-sample)
where an integration with a mock application is shown.

### BitPay's API docs
To read more about invoices refer to the BitPay's [API documentation](https://test.bitpay.com/downloads/bitpayApi.pdf)


## Troubleshooting

Contact support via [our official helpdesk](https://support.bitpay.com) or [ask the community](https://bitpay.com/bitpay/android-sdk/issues).

## License

Code released under [the MIT license](https://github.com/bitpay/bitcore/blob/master/LICENSE).
