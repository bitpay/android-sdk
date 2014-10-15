# BitPay Android SDK

This SDK allows your application to quickly create an invoice, show the user an option to pay you,
and track the status of the payment. Accept bitcoins in your app with 10 lines of code!

## Installing

You'll need to add this SDK as a dependency to your Android project. This process varies according
to the build system you're using.

### Gradle

If you're using Android Studio or gradle to build your app, add the following
to your `build.gradle` file:

    compile 'com.bitpay:sdk-android:0.6.4@aar'

Additionaly, you'll have to specify these dependencies (soon to be bundled together with the sdk):

    compile 'com.google:bitcoinj:0.11.3'
    compile 'com.fasterxml.jackson.core:jackson-databind:2.4.2'
    compile 'com.fasterxml.jackson.core:jackson-annotations:2.2.3'
    compile 'org.apache.httpcomponents:httpclient-android:4.3.5'
    compile 'commons-codec:commons-codec:1.9'
    compile 'com.google.zxing:core:2.0'

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
  <version>0.6.4</version>
  <packaging>aar</packaging>
</dependency>
```

### AAR library

A `.aar` library is provided in the `dist` folder, so you can include it in your Android proyect.

## Setup credentials

### 1. Create a Bitpay Account
Please go to https://bitpay.com to create an account.

### 2. Generate an Application Token

Go to [*My Account* > *API Tokens*](https://bitpay.com/api-tokens) section. Click on the _Add New Token_ button and make a token with the `Point-of-Sale` capability for multiple clients. You can then include this token with your application.

#### (Advanced) Pairing a 'Merchant' Token

If you need access to all of the capabilities of the API, you'll need to create a token under the `Mechant` facade. Follow the same steps and pair your token with a command line tool located in the `bin` folder.

```bash
    $ cd bin
    $ npm install bitauth
    $ ./getClientToken <pairing code, 7 letters>
    Successfully paired. Your client token is: 70163c90f18df866d7a4ec3b8f7215f0013e3f81749f6222938a1f4d9ce3e97e
```

## Sample Code and Usage

### Instantiating BitPayAndroid
```java
String clientToken = "Token from the previous section";
new BitPayAndroid.GetClientWithTokenTask() {
    @Override
    protected void onPostExecute(BitPayAndroid bitpay) {
        // ...
    }
}.execute(clientToken);
```

This class inherits from `BitPay` so you can use all the methods of the [BitPay Java SDK](https://github.com/unChaz/BitPayJavaClient).

#### Using promises
```java
String clientToken = "00000000000000000000000";
BitPayAndroid.withToken(clientToken).then(new BitpayPromiseCallback() {

    public void onSuccess(BitPayAndroid bitpay) {
        // ...
    }

    public void onError(BitPayException e) {
        // ...
    }
});
```

### Use the testing server
```java
String clientToken = "00000000000000000000000";
BitPayAndroid.withToken(clientToken, "https://test.bitpay.com/").then(
    // ...
);
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

### Check if the user has a bitcoin wallet
```java
if (BitPayAndroid.isWalletAvailable(mContext)) {
    // ...
}
```

## More Samples and Documentation

### Sample Project
Take a look at [this project](https://github.com/bitpay/android-sdk-sample)
where an integration with a mock application is shown.

### BitPay's API docs
To read more about invoices refer to the BitPay's [API documentation](https://bitpay.com/api)


## Troubleshooting

Contact support via [our official helpdesk](https://support.bitpay.com) or [ask the community](https://bitpay.com/bitpay/android-sdk/issues).

## License

Code released under [the MIT license](https://github.com/bitpay/android-sdk/blob/master/LICENSE).
