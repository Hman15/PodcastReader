# PodcastReader (ArticleReader)

An Android application that scrapes podcast articles from Vietnamese news sources (VnExpress and Dantri), displays them in a clean interface, and provides audio playback capabilities with download functionality.

## 📱 Solution Overview

PodcastReader is a native Android application built with modern Android development practices that solves the problem of accessing and listening to podcast content from popular Vietnamese news sources. The app provides:

- **Automated Content Scraping**: Fetches podcast articles from VnExpress and Dantri news sources using web scraping
- **Audio Playback**: Integrated audio player with persistent UI across all screens
- **Offline Listening**: Download audio files for offline playback
- **Clean UI**: Modern Material Design 3 interface built with Jetpack Compose
- **Background Playback**: Foreground service for continuous audio playback

## 🏗️ Code Architecture

The project follows **Clean Architecture** principles with clear separation of concerns across three main layers:

```
app/
└── src/main/java/com/hman/podcastreader/
    ├── data/              # Data Layer
    │   ├── dataSource/    # Local (Room) and Remote (Scrapers) data sources
    │   ├── local/         # Database DAOs and Entities
    │   ├── manager/       # Audio download management
    │   ├── mapper/        # Entity ↔ Domain model mappers
    │   └── repository/    # Repository implementations
    │
    ├── domain/            # Domain Layer
    │   ├── model/         # Business models (Article, DownloadedAudio)
    │   ├── repository/    # Repository interfaces
    │   └── usecase/       # Business logic use cases
    │
    ├── presentation/      # Presentation Layer
    │   ├── articlelist/   # Home screen with article list
    │   ├── articledetail/ # Article detail with WebView
    │   ├── audiolist/     # Downloaded audio list
    │   ├── audioplayer/   # Audio player components and service
    │   └── common/        # Shared UI components
    │
    ├── di/                # Dependency Injection (Hilt modules)
    ├── navigation/        # Navigation graph and main screen
    └── ui/theme/          # Material Design 3 theme
```

### Key Architectural Components

#### 1. **Data Layer**
- **Web Scrapers**: Custom scrapers for VnExpress and Dantri using JSoup and OkHttp
  - `VnExpressScraper`: Parses JSON data from article elements
  - `DantriScraper`: Extracts audio URLs and metadata from HTML
- **Room Database**: Local persistence for articles and downloaded audio
  - `ArticleEntity`: Cached article data
  - `DownloadedAudioEntity`: Downloaded audio file metadata
- **AudioDownloadManager**: Handles audio file downloads with progress tracking

#### 2. **Domain Layer**
- **Models**: Pure Kotlin data classes (`Article`, `DownloadedAudio`)
- **Use Cases**: Single-responsibility business logic
  - `GetArticlesUseCase`: Fetch and cache articles
  - `DownloadAudioUseCase`: Download audio files
  - `GetDownloadedAudiosUseCase`: Retrieve downloaded audio
  - `DeleteDownloadedAudioUseCase`: Remove downloaded files

#### 3. **Presentation Layer**
- **MVVM Pattern**: ViewModels manage UI state and business logic
- **Jetpack Compose**: Declarative UI with Material Design 3
- **State Management**: 
  - `UiState` sealed class for loading/success/error states
  - `GlobalAudioPlayerState` singleton for persistent player state
- **Audio Player**:
  - `AudioPlayerService`: Foreground service using Media3 ExoPlayer
  - `PersistentAudioPlayer`: Composable UI shown across all screens
  - `AudioPlayerViewModel`: Manages playback state

#### 4. **Dependency Injection**
- **Hilt**: Provides dependencies across all layers
  - `DatabaseModule`: Room database and DAOs
  - `NetworkModule`: OkHttpClient and scrapers
  - `RepositoryModule`: Repository implementations

### Navigation Structure

```
MainScreen (Scaffold with Bottom Navigation)
├── Home (ArticleListScreen)
│   └── ArticleDetail (WebView)
└── Audio (AudioListScreen)

PersistentAudioPlayer (Overlay on all screens)
```

## 🛠️ Technology Stack

| Category | Technology |
|----------|-----------|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose |
| **Architecture** | Clean Architecture + MVVM |
| **Dependency Injection** | Hilt |
| **Database** | Room |
| **Networking** | OkHttp |
| **Web Scraping** | JSoup |
| **Audio Playback** | Media3 ExoPlayer |
| **Image Loading** | Coil |
| **Navigation** | Jetpack Navigation Compose |
| **Async** | Kotlin Coroutines + Flow |
| **Build System** | Gradle (Kotlin DSL) |

## 🔧 Setup and Build Instructions

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17
- Android SDK 33 or higher
- Gradle 8.x

### Build Steps

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd ArticleReader
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the project directory

3. **Sync Gradle**
   - Android Studio should automatically sync Gradle
   - If not, click "File" → "Sync Project with Gradle Files"

4. **Build the project**
   ```bash
   ./gradlew build
   ```

5. **Run on device/emulator**
   - Connect an Android device or start an emulator
   - Click the "Run" button in Android Studio
   - Or use: `./gradlew installDebug`

### Configuration
- **Minimum SDK**: 33 (Android 13)
- **Target SDK**: 36
- **Compile SDK**: 36
- **Application ID**: `com.hman.podcastreader`

## ⚠️ Known issues

### 1. **Dantri.com requires user to manually play the audio in WebView to be able to detect audio**
**Problem**: User needs to manually click play the audio for the site to generate an audio src link.

**Root Cause**: 
- Dân Trí uses on-demand media loading:
<audio class="op-player__media" playsinline></audio>

️No src initially.
WebView can only fetch the src url only after user interaction:
audio.src = "https://acdn.dantri.com.vn/.../full_1.mp3"
audio.play()

### 2. **Cannot download the right audio when user navigates to other article in WebView**
**Problem**: When user clicks to navigate to other article in the WebView and download that article's 
audio, audio of the original article is downloaded.

**Root Cause**:
- When opening the WebView, audio's src link is only fetched once and it does not get fetched again when user
navigate to other article
