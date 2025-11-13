# EdgeViewer

Android app that captures camera frames, processes them using OpenCV (C++) via JNI (CMake), and renders processed frames using OpenGL ES 2.0. A minimal TypeScript web viewer displays a static processed frame.

## What to run
1. Open the Android project in Android Studio.
2. Ensure OpenCV native libs/headers are available (add OpenCV Android SDK under app/src/main/cpp/include and jniLibs or update CMakeLists).
3. Build & run on a device (camera permission required).
4. From the app, toggle Raw/Edge. To save a sample processed image, add code to write `Bitmap` to storage (left as exercise).

## Web
Open `web/index.html` (use `npm install` and `npm start` to serve locally).

## Notes
- Implementation uses `TextureView.getBitmap()` for simplicity.
- Native code assumes ARGB_8888 ordering — adjust cvtColor if device ordering differs.
