# CSE 489: Mobile Application Development — Assignment 2

Android app built with **Kotlin** in **Android Studio**, following the Android
Jetpack design and coding philosophy: fragments, Jetpack Navigation with a
navigation drawer, ViewBinding, and the Material 3 design system.

## How to open

1. Open **Android Studio** (Hedgehog/Koala or newer recommended).
2. `File → Open…` and select this project folder.
3. Let Gradle sync finish (an internet connection is required the first time).
4. Run the `app` configuration on an emulator or device (min SDK 24 / Android 7.0).

If the Gradle wrapper is not accepted on first sync, run
`gradle wrapper --gradle-version 8.9` once, or let Android Studio fix the wrapper.

## Project structure

| Piece | Where |
|---|---|
| Navigation drawer + toolbar | `MainActivity` + `res/layout/activity_main.xml` |
| Drawer menu (Broadcast Receiver, Image Scale, Video, Audio) | `res/menu/menu_drawer.xml` + `res/navigation/nav_graph.xml` |
| Drawer destination fragments | `BroadcastReceiverFragment`, `ImageScaleFragment`, `VideoFragment`, `AudioFragment` |
| Broadcast flow, second activity | `BroadcastReceiverActivity` |
| Broadcast flow, third activity | `BroadcastResultActivity` |

## Flow vs. the assignment sheet

**A. Broadcast Receiver (drawer item 1)**
- First screen shows a spinner (`Custom` / `System Battery`) and a **Proceed** button.
- **Custom** → second activity takes plain text input; **Send** opens the third
  activity, which registers a dynamic custom broadcast receiver and receives the
  text message via `BroadcastResultActivity.ACTION_CUSTOM_BROADCAST`.
- **System Battery** → second activity registers a receiver for
  `Intent.ACTION_BATTERY_CHANGED` and shows the live battery percentage; the
  third activity does nothing for this selection.

**B. Image Scale (drawer item 2)** — loads a sample image from the internet
(`https://picsum.photos/id/1015/1000/700`) and scales it with a pinch gesture
(`ScaleGestureDetector` + image `Matrix`).

**C. Video (drawer item 3)** — plays a sample video inside the app
(`VideoView`/MediaPlayer + `MediaController`):
`https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4`.

**D. Audio (drawer item 4)** — plays a sample audio file inside the app with
`MediaPlayer` and Play / Pause / Stop buttons:
`https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3`.

The `INTERNET` permission is declared in `AndroidManifest.xml`.
