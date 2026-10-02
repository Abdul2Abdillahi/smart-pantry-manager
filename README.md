# Smart Pantry Manager

An Android app, written in Java, that helps reduce food waste by tracking the
ingredients you have at home and suggesting recipes you can make using only
those ingredients. A recipe is only suggested if every ingredient it needs is
already in your pantry in the required quantity.

## Features (in development)
- Add, edit and delete pantry items (name, quantity, unit, optional expiry date)
- Pantry list view
- 20 pre-loaded recipes
- Suggested Recipes screen using strict ingredient matching
- Recipe detail screen with ingredients and method
- Settings screen

## Database choice: SQLite (SQLiteOpenHelper)
Pantry data is personal and belongs on the user's own device, so a local
database fits better than a cloud one. SQLite works fully offline, needs no
account or server setup, and keeps data after the app is closed.

## Tech stack
- Java
- Android Studio
- SQLite via SQLiteOpenHelper
- Minimum SDK: API 26 (Android 8.0)

## Setup and running
1. Clone this repository:
   `git clone https://github.com/Abdul2Abdillahi/smart-pantry-manager.git`
2. Open the project folder in Android Studio.
3. Wait for Gradle to finish syncing.
4. Run the app on an emulator or physical device (API 26 or higher).

## Author
Abdul Abdillahi, 402111750
Mobile App Development 700, Richfield Graduate Institute of Technology