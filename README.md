# Network Monitor App
## Overview
Network Monitor is an Android application that dynamically detects network state changes and notifies users when their device loses internet connectivity and when it is restored. The app provides visual feedback and actionable options to help users manage their network connections.

## Features
- Real-time Network Monitoring: Detects when internet connectivity is lost or restored
- Visual Status Indicators: Displays network status with intuitive icons and text
- Toast Notifications: Shows toast messages for connectivity changes
- Snackbar with Retry Option: Provides a convenient way to check connectivity again
- Network Settings Dialog: Prompts users to enable WiFi or Mobile Data when disconnected
- Direct Access to System Settings: Quick access to device network settings

## Technical Implementation
- BroadcastReceiver: Dynamically registers a receiver to monitor network connectivity changes
- ConnectivityManager: Uses Android's system service to check network status
- Material Design Components: Implements Snackbar and AlertDialog for user interactions
- Proper Lifecycle Management: Registers receiver in onResume() and unregisters in onPause() to prevent memory leaks

## Requirements
- Android SDK 26+ (Android 8.0 Oreo or higher)
- Java 8+
- AndroidX libraries
## How to Use

1. Launch the app to see your current network status.
2. The app will automatically notify you when network connectivity changes.
3. If connection is lost:
   - A toast message will appear.
   - A Snackbar with a **"Retry"** button will be shown.
   - An AlertDialog will prompt you to enable WiFi or Mobile Data.
   - Use the **"Network Settings"** button to directly access your device's network settings.

## Screenshots
![screenshot](https://github.com/user-attachments/assets/ecc2cadf-de01-4581-9f93-93aa0f664300)

![screenshot_2](https://github.com/user-attachments/assets/a5204373-266f-4a77-a567-f241449bf213)




