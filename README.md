# NewsFeedAssignment

NewsFeedAssignment is an Android news reader built with Jetpack Compose and GNews. It provides category-based headlines, remote search, offline bookmarks, cached home-feed browsing, and article detail actions.

## Toolchain

- Gradle Wrapper 8.10.2
- Android Gradle Plugin 8.7.3
- Kotlin 2.0.21
- Java 17 bytecode target
- Android compile and target SDK 35
- Minimum SDK 26

## Configure the GNews API key

The API key is read locally at build time and is not checked into source control. Create a `local.properties` file in the project root and add:

```properties
GNEWS_API_KEY=your-gnews-api-key
```

You can also provide the key through the `GNEWS_API_KEY` environment variable. Keep `local.properties` private and never commit it.

The app uses the key for local development and demo builds. Release builds intentionally use an empty key in this assignment configuration.

## Build and test

On Windows, make sure `local.properties` also contains the path to your Android SDK, for example:

```properties
sdk.dir=C:\\Users\\your-name\\AppData\\Local\\Android\\Sdk
```

Run the standard verification tasks with:

```powershell
.\gradlew.bat clean test lint assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Project structure

- `:app` — application startup, Hilt configuration, activity, theme, and root navigation.
- `:core` — domain models, repository contracts, search processing, and bookmark use cases.
- `:data` — GNews networking, DTO mapping, Room persistence, repositories, and Paging components.
- `:feature-news` — Compose screens, ViewModels, navigation, article detail, WebView, and actions.

Home data flows from GNews through the Room cache and Paging into Compose. Bookmarks are stored separately, remain available offline, and can be searched locally. Remote catalogue search requires network access.
