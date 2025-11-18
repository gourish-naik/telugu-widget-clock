# Instructions to Build and Run the Telugu Clock Widget App

## 1. SDK Setup Steps

### 1.1. Install Android SDK Command-Line Tools

1.  **Download the SDK:**
    Go to the [Android Studio downloads page](https://developer.android.com/studio) and scroll down to "Command line tools only". Download the appropriate package for your operating system.

2.  **Create an SDK directory:**
    Create a directory where you want to install the Android SDK. For example:
    ```bash
    mkdir -p ~/Android/sdk
    ```

3.  **Unzip the tools:**
    Unzip the downloaded file into the directory you just created. The unzipped folder will be named `cmdline-tools`. Rename it to `latest`:
    ```bash
    unzip commandlinetools-*.zip -d ~/Android/sdk
    mv ~/Android/sdk/cmdline-tools ~/Android/sdk/latest
    ```

### 1.2. Set ANDROID_HOME and PATH

1.  **Set the `ANDROID_HOME` environment variable:**
    This variable should point to your SDK directory. Add the following lines to your shell profile file (e.g., `~/.bashrc`, `~/.zshrc`):
    ```bash
    export ANDROID_HOME=$HOME/Android/sdk
    export PATH=$PATH:$ANDROID_HOME/latest/bin:$ANDROID_HOME/platform-tools
    ```

2.  **Apply the changes:**
    Source your profile file to apply the changes in your current terminal session:
    ```bash
    source ~/.bashrc 
    # Or source ~/.zshrc
    ```

### 1.3. Install SDK Packages

1.  **Accept licenses:**
    Before you can download packages, you need to accept the SDK licenses:
    ```bash
    yes | sdkmanager --licenses
    ```

2.  **Install platform-tools, build-tools, and platforms:**
    Use `sdkmanager` to install the necessary packages. For this project, you need:
    *   `platform-tools`: Contains `adb` and other essential tools.
    *   `build-tools`: The specific version required by the project (e.g., `34.0.0`).
    *   `platforms`: The Android API level to compile against (e.g., `android-34`).

    ```bash
    sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
    ```

## 2. Build Steps

### 2.1. Create a Keystore (for Release Builds)

If you want to create a signed release APK, you need a keystore.

1.  **Generate a keystore:**
    Use the `keytool` command to generate a private key.
    ```bash
    keytool -genkey -v -keystore my-release-key.keystore -alias my-alias -keyalg RSA -keysize 2048 -validity 10000
    ```
    You will be prompted to enter a password and other information. Remember the password.

2.  **Create `keystore.properties`:**
    Create a file named `keystore.properties` in the root of your project and add the following, replacing the values with your own:
    ```
    storePassword=your_store_password
    keyPassword=your_key_password
    keyAlias=my-alias
    storeFile=my-release-key.keystore
    ```
    **Important:** Add `keystore.properties` to your `.gitignore` file to avoid committing it to version control.

### 2.2. Build the APK

1.  **Clean the project (optional but recommended):**
    ```bash
    ./gradlew clean
    ```

2.  **Build the debug APK:**
    This will create an unsigned APK that can be installed on a device with USB debugging enabled.
    ```bash
    ./gradlew assembleDebug
    ```
    The APK will be located at `app/build/outputs/apk/debug/app-debug.apk`.

3.  **Build the release APK (requires keystore):**
    To build a signed release APK, you need to configure the `app/build.gradle` file to use your keystore. Add the following to your `android` block in `app/build.gradle`:
    ```groovy
    android {
        // ... other config
        
        signingConfigs {
            release {
                if (project.hasProperty('keystore.properties')) {
                    def props = new Properties()
                    props.load(new FileInputStream(rootProject.file('keystore.properties')))
                    storeFile file(props['storeFile'])
                    storePassword props['storePassword']
                    keyAlias props['keyAlias']
                    keyPassword props['keyPassword']
                }
            }
        }

        buildTypes {
            release {
                // ... other config
                signingConfig signingConfigs.release
            }
        }
    }
    ```
    Now you can build the release APK:
    ```bash
    ./gradlew assembleRelease
    ```
    The signed APK will be at `app/build/outputs/apk/release/app-release.apk`.

## 3. Final Build and Install Steps

### 3.1. Install the APK via ADB

1.  **Enable USB Debugging on your device:**
    Go to `Settings > About phone` and tap `Build number` 7 times to enable Developer options. Then go to `Settings > Developer options` and enable `USB debugging`.

2.  **Connect your device:**
    Connect your Android device to your computer via USB.

3.  **Verify the device is connected:**
    Run `adb devices` to see a list of connected devices.
    ```bash
    adb devices
    ```

4.  **Install the APK:**
    Use `adb install` to install the APK on your device.
    *   For debug:
        ```bash
        adb install app/build/outputs/apk/debug/app-debug.apk
        ```
    *   For release:
        ```bash
        adb install app/build/outputs/apk/release/app-release.apk
        ```

### 3.2. Add the Widget to Your Home/Lock Screen

1.  **Home Screen:**
    *   Long-press on an empty area of your home screen.
    *   Select "Widgets".
    *   Find "Telugu Clock Widget" in the list and drag it to your home screen.
    *   The configuration screen will appear. Adjust the settings and press "Save".

2.  **Lock Screen (Android 14+):**
    *   From your lock screen, long-press the clock.
    *   Tap "Customize".
    *   Select "Widgets".
    *   Find and add the "Telugu Clock Widget".
    *   The configuration screen will open. Configure and save.
```
