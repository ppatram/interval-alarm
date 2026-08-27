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
# interval-alarm
