# SMG Real Estate

Small Android App for property listings. (Compose + Material 3 + Hilt). Uses a public mock JSON API for property data and Room for bookmarks.

**Running it**  
Open the project in Android Studio, sync Gradle, run `app` on an emulator or phone (API 26+). Needs network for the first load.

**Firebase**  
`app/google-services.json` has to match your Firebase app id (`applicationId` in Gradle). Analytics: bookmark toggles and search (debounced) fire custom events.

**API**  
Remote base URL lives in the Hilt network module.
