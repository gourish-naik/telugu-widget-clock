# Gemini Code Assistant Context

This document provides context for the Gemini Code Assistant to understand the project and provide relevant assistance.

## Project Overview

This is an Android application for a customizable clock widget. The widget displays the time using Telugu numerals and can be placed on the home screen and the lock screen (on Android 14+).

The project is built using the Android SDK and Gradle, without relying on Android Studio.

### Key Features:

*   **Telugu Clock Widget:** Displays the current time using Telugu numerals.
*   **Customization:** The widget can be customized for font size, color, and position through a configuration activity.
*   **Periodic Updates:** The widget updates every minute using a `WorkManager` task.
*   **Home and Lock Screen:** The widget is designed to work on both the home screen and the lock screen.

### Technologies Used:

*   **Android SDK:** The application is built using the Android SDK tools.
*   **Gradle:** The project uses Gradle for building and dependency management.
*   **Java:** The application logic is written in Java.
*   **XML:** The layouts and widget configuration are defined in XML.
*   **WorkManager:** Used for scheduling periodic updates to the widget.
*   **SharedPreferences:** Used for storing widget configuration.

### Architecture:

The project follows a standard Android application architecture:

*   **`AppWidgetProvider`:** The `TeluguClockWidget` class is the main entry point for the widget, handling updates and lifecycle events.
*   **`Activity`:** The `TeluguClockWidgetConfigureActivity` class provides a user interface for customizing the widget.
*   **`WorkManager`:** The `ClockUpdateWorker` class is a periodic worker that updates the widget every minute.
*   **`RemoteViews`:** The widget's user interface is built using `RemoteViews`, which are updated by the `AppWidgetProvider` and the `WorkManager`.
*   **Resources:** The project includes various resource files for layouts, drawables, strings, and other application assets.

## Building and Running

The project can be built and run from the command line using Gradle and the Android Debug Bridge (ADB).

### Building the APK:

*   **Build Debug APK:**
    ```bash
    ./gradlew assembleDebug
    ```
    The debug APK will be located at `app/build/outputs/apk/debug/app-debug.apk`.

*   **Build Release APK:**
    To build a release APK, you need to create a keystore and configure the `app/build.gradle` file. Detailed instructions are available in the `INSTRUCTIONS.md` file.
    ```bash
    ./gradlew assembleRelease
    ```

### Installing the APK:

*   **Install via ADB:**
    ```bash
    adb install app/build/outputs/apk/debug/app-debug.apk
    ```

### Running Tests:

*   **Run Unit Tests:**
    ```bash
    ./gradlew test
    ```

*   **Run Instrumented Tests:**
    ```bash
    ./gradlew connectedAndroidTest
    ```

## Development Conventions

*   **Code Style:** The code follows standard Java and Android coding conventions.
*   **Testing:** The project includes unit tests and instrumented tests.
*   **Dependencies:** Dependencies are managed using Gradle.
*   **Versioning:** The project uses semantic versioning.
*   **Documentation:** The `INSTRUCTIONS.md` file provides detailed instructions for setting up the development environment, building, and running the application.
