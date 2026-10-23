# Clear Home: a minimal TV launcher for Android 6 TVs

A dark, uncluttered replacement home screen: clock, big app name, a Favorites row and an All apps row.
Banner art is used when an app provides it (YouTube, Netflix etc. do), otherwise its icon.

## Build the APK (on a PC or Mac)
1. Install Android Studio and open this folder (`ClearHome`).
2. Let Gradle sync (it downloads what it needs; JDK 17 is bundled with Android Studio).
3. Menu: Build > Build Bundle(s) / APK(s) > Build APK(s).
4. The file appears at `app/build/outputs/apk/debug/app-debug.apk`.

## Install on the TV
1. TV: Settings > Security & restrictions > enable Unknown sources.
2. Copy `app-debug.apk` to a USB stick, plug it in, and open it with a file manager app on the TV
   (install one from the Play Store if the TV has none, e.g. X-plore).
3. Press Home. If Android asks which home app to use, choose Clear Home and tap Always.

## Using it
- Arrow keys move, OK opens an app.
- Hold OK (or press Menu) on an app to pin or unpin it from Favorites.
- Back and Home both return to the start of the launcher.

## If Android never asks which home app to use
Some TVs hide the chooser. With ADB over network you can disable the stock launcher:

    adb connect <tv-ip>:5555
    adb shell pm disable-user --user 0 com.google.android.leanbacklauncher

To get it back: `adb shell pm enable com.google.android.leanbacklauncher`.
Install Clear Home first, so the TV always has a launcher when you do this.

## Uninstall
Settings > Apps > Clear Home > Uninstall. The stock home screen returns.
