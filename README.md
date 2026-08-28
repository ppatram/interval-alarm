# Interval Reminder — Alarm Edition

## Build compatibility
- Android Gradle Plugin: 8.7.3
- Gradle: 8.9
- Kotlin: 2.0.21
- JDK: 17
- compileSdk/targetSdk: 35

This project intentionally pins Gradle 8.9. AGP 8.7 requires Gradle 8.9; do not use Gradle 9.x with this project.

In Android Studio, use the Gradle wrapper configuration supplied with the project. If Android Studio asks to upgrade Gradle, choose **Do not upgrade** for this project.

The app schedules exact alarms, restores them after reboot, skips bedtime, and uses a foreground service only while an alarm is actively ringing.

## Development History & User Requests
Below is a log of the features and fixes implemented during development:

1.  **Build Fixes**: Resolved unresolved references to `padding` and missing `Settings` imports.
2.  **Deployment**: Guided installation and debugging on Pixel devices.
3.  **Custom Branding**: Renamed app to "Recurring Timer", added custom bell icon (later updated to user-provided JPG).
4.  **ADB & Shell Support**: Provided instructions for ADB installation and shell process identification.
5.  **Smart Scheduling**: Added "Days of the Week" selection and 24-hour time input.
6.  **UI Compactness**: Replaced bulky time pickers with streamlined text input (HH:mm).
7.  **Multi-Alarm System**: Migrated from SharedPreferences to a Room Database, allowing for multiple named alarms.
8.  **List Management**: Created a clean list view with Material Design slider toggles to enable/disable alarms.
9.  **Sound Customization**: Integrated system RingtoneManager to allow picking unique sounds for each alarm.
10. **UI Styling**: Adjusted layout for status bar clearance and added purple branding to the header.
11. **Auto-Stop Logic**: Explored and eventually reverted an auto-stop timer in favor of manual stop.

# interval-alarm
