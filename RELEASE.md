# Releasing the Android SDK for distribution

## Prerequisites

1. You must have [Android Studio](http://developer.android.com/sdk/index.html) from Google.

2. You must clone this repo:

```bash
$ git clone https://github.com/bitpay/android-sdk
```

2. Check out the branch you wish to release to the world. At the time of this writing, we release from the master branch.

```bash
$ git checkout master
```

3. Open Android Studio and "create a new project" by using the sources that you cloned in the above steps. Once the project opens, depending on the state of Android Studio and the Gradle build system, you may be asked to update Gradle, sync build.gradle, etc. Please watch the "messages" window for explicit instructions. If you run into problems, open a Github issue on this project.

4. Build the project by going to the "Build" menu and choose "Build APK". This will generate build artifacts for the test project as well as a .aar file for the android-sdk.aar library. This is what we need to distribute. This aar file will most likely be located in "./android-sdk/build/outputs/aar/android-sdk-[debug|release].aar". If not, then:

```bash
$ find . -name "*.aar"
```

5. move this file to the dist directory and delete the last aar file from the last release.

```bash
$ rm -fr dist/* && mv ./android-sdk/build/outputs/aar/android-sdk-release.aar dist/
```

6. Edit the android-sdk/build.gradle file. Change the value of the "version" key to have "-release" appended to it. Example: "version = 1.0.1-release" instead of "version = 1.0.1". This will the cryptographic signing when running gradle uploadArchive (we'll get to this later).

7. Commit the changes and push to the upstream master branch (bitpay/android-sdk).

```bash
$ git add dist/ android-sdk/build.gradle && git commit -m"Adding artifacts for release."
$ git push upstream master #your remote might be called origin
```
and tag the release sha

```bash
$ git tag <tag> #e.g. git tag v1.0.1
$ git push upstream <tag>
```

8. If you don't have a GPG signing key, then proceed with steps 8 and 9. Ensure you have GnuPG (gnu privacy guard).

```bash
$ gpg --version
```

Here is sample output from my machine:

> gpg (GnuPG) 1.4.19<br>
> Copyright (C) 2015 Free Software Foundation, Inc.<br>
> License GPLv3+: GNU GPL version 3 or later [http://gnu.org/licenses/gpl.html](http://gnu.org/licenses/gpl.html)<br>
> This is free software: you are free to change and redistribute it.<br>
> There is NO WARRANTY, to the extent permitted by law.<br>
><br>
> Home: ~/.gnupg<br>
> Supported algorithms:<br>
> Pubkey: RSA, RSA-E, RSA-S, ELG-E, DSA<br>
> Cipher: IDEA, 3DES, CAST5, BLOWFISH, AES, AES192, AES256, TWOFISH,<br>
>         CAMELLIA128, CAMELLIA192, CAMELLIA256<br>
> Hash: MD5, SHA1, RIPEMD160, SHA256, SHA384, SHA512, SHA224<br>
> Compression: Uncompressed, ZIP, ZLIB, BZIP2<br>

Getting GnuPG for Mac:

```bash
$ brew install gnupg
```

Note: there is no need to get GnuPG 2.x, Version 1 works fine.

Getting GnuPG for Linux Unbuntu or Debian:

```bash
$ sudo apt-get install gnupg
```

9. Create a signing and encryption key (although only a  signing key is required):

```bash
$ gpg --gen-key
```

Just choose the defaults, RSA and RSA, 2048, does not expire, fill in your name, email, choose a password, please don't use an empty password.

You should then have a signing key.

Grab the keyID that you just created:

```bash
$ gpg --list-keys
```

You should get output that looks similar to:

> pub   2048R/EF6BDB7F 2015-03-23<br>
> uid   Chris Kleeschulte<br>
> sub   2048R/CE81F194 2015-03-23<br>

The keyId in the example above is "EF6BDB7F" above. Yours will be different, but in the same position.

## Release all the things

Background first. You will be doing three major steps. First, creating a properties file with credentials. Second, running a gradle "task" and checking the output for errors. Third, going to Sonatype's web interface to release the staged artifacts.

1. Edit "gradle.properties" located in the root of the android-sdk project. Add the following keys:

> sonatypeUsername=bitpay<br>
> sonatypePassword=some password<br>
> signing.keyId=your key id<br>
> signing.password=your password<br>
> signing.secretKeyRingFile=/Users/yourusername/.gnupg/secring.gpg<br>

As for the "sonatypePassword", if you don't know it, [Reset it here](https://issues.sonatype.org/secure/ForgotLoginDetails.jspa). The username is bitpay. The password should come to "integrations@bitpay.com" so if you have access to this email box, then you can reset this.

Note: NEVER commit your gradle.properties file! This could send sensitive information to a public place.

2. run the uploadArchives gradle task

```bash
$ sh gradlew android-sdk:uploadArchives
```

the output should resemble this:

> :android-sdk:preBuild UP-TO-DATE<br>
> :android-sdk:preReleaseBuild UP-TO-DATE<br>
> :android-sdk:compileReleaseNdk UP-TO-DATE<br>
> :android-sdk:compileLint<br>
> :android-sdk:copyReleaseLint UP-TO-DATE<br>
> :android-sdk:mergeReleaseProguardFiles UP-TO-DATE<br>
> :android-sdk:packageReleaseRenderscript UP-TO-DATE<br>
> :android-sdk:checkReleaseManifest<br>
> :android-sdk:prepareReleaseDependencies<br>
> :android-sdk:compileReleaseRenderscript UP-TO-DATE<br>
> :android-sdk:generateReleaseResValues UP-TO-DATE<br>
> :android-sdk:generateReleaseResources UP-TO-DATE<br>
> :android-sdk:packageReleaseResources UP-TO-DATE<br>
> :android-sdk:compileReleaseAidl UP-TO-DATE<br>
> :android-sdk:generateReleaseBuildConfig UP-TO-DATE<br>
> :android-sdk:generateReleaseAssets UP-TO-DATE<br>
> :android-sdk:mergeReleaseAssets UP-TO-DATE<br>
> :android-sdk:processReleaseManifest UP-TO-DATE<br>
> :android-sdk:processReleaseResources UP-TO-DATE<br>
> :android-sdk:generateReleaseSources UP-TO-DATE<br>
> :android-sdk:compileReleaseJavaWithJavac UP-TO-DATE<br>
> :android-sdk:processReleaseJavaRes UP-TO-DATE<br>
> :android-sdk:transformResourcesWithMergeJavaResForRelease UP-TO-DATE<br>
> :android-sdk:transformClassesAndResourcesWithSyncLibJarsForRelease UP-TO-DATE<br>
> :android-sdk:mergeReleaseJniLibFolders UP-TO-DATE<br>
> :android-sdk:transformNative_libsWithMergeJniLibsForRelease UP-TO-DATE<br>
> :android-sdk:transformNative_libsWithSyncJniLibsForRelease UP-TO-DATE<br>
> :android-sdk:bundleRelease UP-TO-DATE<br>
> :android-sdk:signArchives UP-TO-DATE<br>
> :android-sdk:uploadArchives<br>
> Could not find metadata com.bitpay:android-sdk/maven-metadata.xml in remote<br> (https://oss.sonatype.org/service/local/staging/deploy/maven2/)<br>
><br>
> BUILD SUCCESSFUL<br>
><br>
> Total time: 10.066 secs<br>

The key here is seeing: android-sdk:uploadArchives without errors. If all that goes ok, then proceed. If not, you may not have permission to login to oss.sonatype (400-level errors) or maybe your gpg key password is wrong or the path to the secret key is off.

3. Login to [sonatype](https://oss.sonatype.org/index.html#nexus-search;quick~bitpay).

In the upper right corner there is a login link. Once logged in, click "Staging Repositories" under "Build Promotion", you should see a tab called "Staging Repositories" in the main window. Search the list for "combitpay". It may be the last one. It may be called "combitpay" with some other characters trailing after combitpay.

Click the check box on combitpay. Look under the "content" tab below the list. Ensure the artifact is the one you expected. It should be something like "android-sdk-<tag>-release". If so, click "Close" above the list in the "Staging Repositories". A text box will be presented for comments. No comments are needed, just close.

This will go through some checks and if all is well, the "Release" button will be clickable. If so, click "Release", again a text box will be presented. No need for comments, just release.

If the close operation fails, it could be that the artifact isn't signed properly. Check the output of gradle android-sdk:uploadArchives again. Ensure that :android-sdk:signArchives does not say "SKIPPED". If so, this is your problem. Check that you added "-release" to android-sdk/build.gradle version key. Click "Drop" on the Staging Repository and repeat the above steps.

Rejoice.
