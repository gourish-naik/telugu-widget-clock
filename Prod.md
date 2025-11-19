# Production Build Instructions

This document outlines the steps to generate a production-ready APK for the Ghadiya app without using Android Studio. These instructions are for a Linux environment.

## Prerequisites

- Java Development Kit (JDK) 8 or higher
- Android SDK command-line tools

## Generating a Keystore

1.  **Generate a new keystore:**

    ```bash
    keytool -genkey -v -keystore ghadiya-release-key.keystore -alias ghadiya_alias -keyalg RSA -keysize 2048 -validity 10000
    ```

2.  **Follow the prompts:**

    - You will be prompted to create a password for the keystore and provide information for the key.
    - Remember the passwords you set, as you will need them to sign the app.

## Building the App

1.  **Create a `keystore.properties` file:**

    - In the `android/app` directory of your project, create a new file named `keystore.properties`.
    - Add the following lines to the file, replacing the placeholder values with your actual keystore information:

    ```properties
    storePassword=<your_keystore_password>
    keyPassword=<your_key_password>
    keyAlias=ghadiya_alias
    storeFile=../../ghadiya-release-key.keystore
    ```

2.  **Clean the project:**

    ```bash
    ./gradlew clean
    ```

3.  **Build the release APK:**

    ```bash
    ./gradlew assembleRelease
    ```

4.  **Find the APK:**

    - The signed APK will be located in the `android/app/build/outputs/apk/release` directory.

## Uploading a Logo

- The app's logo is located in the `app/src/main/res/mipmap-anydpi-v26` and `app/src/main/res/mipmap-hdpi`, `app/src/main/res/mipmap-mdpi`, etc. directories.
- To update the logo, replace the `ic_launcher.xml` and `ic_launcher_round.xml` files in the `mipmap-anydpi-v26` directory with your new logo files.
- You will also need to replace the `ic_launcher.png` and `ic_launcher_round.png` files in the other `mipmap` directories with your new logo files.
- Ensure that the new logo files are the correct size for each directory.
