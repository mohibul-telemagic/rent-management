# RentEase Senior (Android)

A Jetpack Compose Android app focused on elderly-friendly rent operations with backend sync:

- Login with backend account
- Add properties
- Add units
- Add tenants by selecting a unit
- Upload NID image later from mobile
- Dashboard for current month:
  - Total collected
  - Total pending
  - Unpaid tenants list
- Record payments
- Create/send invoices with utility/other bill lines
- Open native SMS app prefilled with invoice download link

## Tech

- Kotlin + Jetpack Compose (Material 3)
- Retrofit + Spring backend APIs
- Compose state + coroutine calls

## Project location

`android-app/`

## Open and run

1. Open `android-app` in Android Studio (Hedgehog+ recommended).
2. Let Gradle sync and download dependencies.
3. Ensure backend is reachable from emulator at `http://10.0.2.2:8080`.
4. Run on an emulator/device (Android 8.0+, API 26+).

## Invoice Link Configuration

API base URL is configured by `BuildConfig.API_BASE_URL` in `app/build.gradle.kts`.
Default: `http://127.0.0.1:8080/api/v1/` (use `adb reverse tcp:8080 tcp:8080` for emulator to host backend).

## Native SMS behavior

The app uses `Intent.ACTION_SENDTO` with `smsto:` so users send via their default SMS app (no direct SMS permission required).

## Main files

- `app/src/main/java/com/rentease/seniorrent/MainActivity.kt`
- `app/src/main/java/com/rentease/seniorrent/Api.kt`
- `app/src/main/java/com/rentease/seniorrent/SessionStore.kt`

## Notes

- Data is backend-first; local storage is only used for session tokens.
- Invoice SMS text comes from backend and includes one-time download link.

## Emulator Connectivity (Important)

On this machine, `10.0.2.2:8080` may time out. Use adb reverse and emulator localhost instead:

1. Start backend on host port `8080`.
2. Run:
   - `"C:\Users\mohibul\AppData\Local\Android\Sdk\platform-tools\adb.exe" -s emulator-5554 reverse tcp:8080 tcp:8080`
3. App base URL is set to `http://127.0.0.1:8080/api/v1/`.

If emulator id changes, replace `emulator-5554` with your current id from `adb devices`.

