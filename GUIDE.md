# Using the BitPay Android SDK

## Prerequisites

You must have a BitPay merchant account to use this SDK.  It's free to [sign-up for a BitPay merchant account](https://bitpay.com/start).

Once you have a BitPay merchant account, you will need [a working BitPay Access Token](/api/getting-access.html) – this can be done either via the SDK (pairing process) or manually in [the BitPay Dashboard](https://bitpay.com/tokens).


## Installing

You'll need to add this SDK as a dependency to your Android project. This process varies according to the build system you're using.

### Gradle

If you're using Android Studio or gradle to build your app, add the following to your `build.gradle` file:

```gradle
compile 'com.bitpay:android-sdk:1.0.0@aar'
```

Additionaly, you'll have to specify these dependencies:

```gradle
compile 'com.google:bitcoinj:0.11.3'
compile 'com.fasterxml.jackson.core:jackson-databind:2.4.2'
compile 'com.fasterxml.jackson.core:jackson-annotations:2.2.3'
compile 'org.apache.httpcomponents:httpclient-android:4.3.5'
compile 'commons-codec:commons-codec:1.9'
compile 'com.google.zxing:core:2.0'
```

Remember to add the maven central repository at the beginning of the file:

```java
    repositories {
        mavenCentral()
    }
```

### Maven dependency

If you're using maven, add the following dependency:

```xml
<dependency>
  <groupId>com.bitpay</groupId>
  <artifactId>android-sdk</artifactId>
  <version>1.0.0</version>
  <packaging>aar</packaging>
</dependency>
```

### AAR library

A `.aar` library is provided in the `dist` folder, so you can include it in your Android project.

## Setup credentials

### 1. Create a BitPay Account

Please go to https://bitpay.com to create an account.

### 2. Generate an Application Token

Go to [*My Account* > *API Tokens*](https://bitpay.com/api-tokens) section. Click on the _Add New Token_ button and make a token with the `Point-of-Sale` capability for multiple clients. You can then include this token with your application.

#### (Advanced) Pairing a 'Merchant' Token

If you need access to all of the capabilities of the API, you'll need to create a token under the `Merchant` facade. Follow the same steps and pair your token with a command line tool located in the `bin` folder.

```bash
$ cd bin
$ npm install bitauth
$ ./getClientToken <pairing code, 7 letters>
Successfully paired. Your client token is: 70163c90f18df866d7a4ec3b8f7215f0013e3f81749f6222938a1f4d9ce3e97e
```

## Sample Code and Usage

### Instantiating BitPayAndroid:

```java
String clientToken = "Token from the previous section";
new BitPayAndroid.GetClientWithTokenTask() {
    @Override
    protected void onPostExecute(BitPayAndroid bitpay) {
        // ...
    }
}.execute(clientToken);
```

This class inherits from `BitPay` so you can use all the methods of the [BitPay Java Client Library](https://github.com/bitpay/java-bitpay-client).

#### Using promises:

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

### Use the testing server:

```java
String clientToken = "00000000000000000000000";
BitPayAndroid.withToken(clientToken, "https://test.bitpay.com/").then(
    // ...
);
```

### Creating a new invoice:

```java
Invoice invoice = new Invoice(200, "USD");
new BitPayAndroid.CreateInvoiceTask(bitpay) {
    @Override
    protected void onPostExecute(Invoice invoice) {
        // ...
    }
}.execute(invoice);
```

#### Using promises:

```java
bitpay.createNewInvoice(new Invoice(200, "USD")).then(new InvoicePromiseCallback() {

    public void onSuccess(Invoice invoice) {
        // ...
    }
    public void onError(BitPayException e) {
        // ...
    }
});
```

### The InvoiceActivity class

This class helps your customer track the status of his payment, shows a QR code and a button to send him to his wallet, so he can pay.

```java
Intent invoiceIntent = new Intent(this, InvoiceActivity.class);
invoiceIntent.putExtra(InvoiceActivity.INVOICE_ID, invoice.getId());
startActivity(invoiceIntent);
```

### Check if the user has a bitcoin wallet:

```java
if (BitPayAndroid.isWalletAvailable(mContext)) {
    // ...
}
```
