# ZeroBars 📶🚫

**ZeroBars** is an Android utility app designed to alert you immediately when your device loses cellular network connection. It runs a foreground service to monitor connectivity even when the app is closed or the screen is off.

## Features
- **Real-time Monitoring:** Detects loss of Cellular Transport.
- **Push Notifications:** Alerts you with a high-priority notification when signal is lost.
- **Activity Log:** Keeps a history of when connection was lost and restored.
- **Modern UI:** Built with Jetpack Compose and Material 3.

## Getting Started

### Prerequisites
- Android Studio Hedgehog or newer.
- JDK 17.
- Android SDK API 34.

### Building Locally
1. Clone the repository.
2. Open in Android Studio.
3. Run `./gradlew assembleDebug` to build the APK.

### CI/CD & Deployment
This repository is set up with **GitHub Actions** to automatically build the APK on every push to `main`.

1. Go to the **Actions** tab in your GitHub repository.
2. Click on the latest workflow run.
3. Scroll down to **Artifacts**.
4. Download `ZeroBars-Debug.zip`.
5. Extract and install `app-debug.apk` on your Pixel 8 Pro.
