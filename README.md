# HealthMate – Personal Health and Wellness Tracker

## Overview

HealthMate is a native Android mobile application designed to help users record and monitor their daily health and wellness activities. The application allows users to manage their daily steps, calories burned and water intake through a simple and user-friendly interface.

This application was developed for the Mobile Application Development (CI6330) coursework at Kingston University.

## Features

- Daily health activity dashboard.
- Add, view, update and delete health records.
- Calendar-based searching and date filtering.
- View complete health record history.
- Seven-day health activity statistics.
- Customizable daily water intake goal.
- Water intake progress indicator.
- Prevention of duplicate daily records.
- Prevention of future-date records.
- Input validation and error handling.
- Confirmation before deletion or discarding unsaved changes.
- Preservation of unfinished form input during screen rotation.
- Offline data storage and persistence.

## Technologies Used

| Technology | Purpose |
|---|---|
| Java | Application development |
| Android Studio | Development environment |
| SQLite | Local database storage |
| MVVM | Application architecture |
| AndroidX LiveData | State management and UI updates |
| Material Design | User interface components |
| SharedPreferences | Water goal storage |
| Gradle | Build and dependency management |
| JUnit | Automated testing |
| Git and GitHub | Version control |

## Application Architecture

HealthMate follows the Model-View-ViewModel (MVVM) architectural pattern to maintain separation between the user interface, application logic and data storage.

- **View:** Displays application screens and collects user input.
- **ViewModel:** Manages application state, validation and database operations.
- **Model and Data Layer:** Represents health records and manages persistent storage using SQLite.

## Project Structure

```text
app/src/main/java/com/example/healthmate/
├── dashboard/
│   └── DashboardScreen.java
├── data/
│   ├── DatabaseHelper.java
│   └── WaterGoalPreferences.java
├── model/
│   ├── HealthRecord.java
│   └── HealthSummary.java
├── records/
│   ├── RecordsScreen.java
│   └── RecordFormScreen.java
├── shared/
│   ├── HealthViewModel.java
│   └── ScreenUi.java
└── MainActivity.java
```

## Database

HealthMate uses a local SQLite database containing a `health_records` table.

| Field | Data Type | Description |
|---|---|---|
| id | INTEGER | Primary key |
| date | TEXT | Health record date |
| steps | INTEGER | Daily steps walked |
| calories | INTEGER | Daily calories burned in kcal |
| water | INTEGER | Daily water intake in ml |

Each date is restricted to one health record. Users can update an existing record to reflect their total daily activities.

## Installation and Setup

### Requirements

- Android Studio
- Android SDK
- Android device or emulator running Android 8.0 (API 26) or later

### Running the Application

1. Clone this repository.
2. Open the HealthMate project in Android Studio.
3. Synchronize the Gradle project.
4. Connect an Android device or start an Android emulator.
5. Build and run the application.

A fresh installation starts with an empty health records database. Users can manually add health records through the application.

## Testing

The application was tested using a Pixel 7a Android emulator and a Samsung A24 physical device.

Testing covered database operations, health statistics calculations, water goal preferences, input validation, navigation, data persistence and the complete CRUD workflow.

| Test Category | Successful Executions |
|---|---:|
| Unit tests | 4/4 |
| Instrumented tests across two devices | 30/30 |
| Total | 34/34 |

All automated test executions completed successfully with no reported failures or skipped tests.

## Privacy and Data Storage

HealthMate operates offline and stores health records locally on the device. No external server, account registration or internet connection is required for its core functionality.

## Author

S. Hashini Dewmini

Kingston University

Mobile Application Development – CI6330

Academic Year 2026