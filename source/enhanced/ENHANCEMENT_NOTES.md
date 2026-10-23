# CS 499 Enhancement History

Artifact: CS 360 Weight Tracker Android Application  
Student: Samuel Ilyayev

## Milestone Two: Software Design and Engineering

The software design enhancement reorganized the completed CS 360 application so the Android Activities are no longer responsible for every part of the program. The project now uses repository, service, session, notification, validation, and security classes. Passwords for new accounts are stored with PBKDF2 hashing, legacy plain-text passwords are upgraded after a successful login, and reusable validation and error handling were added. Unit tests were also added for validation, password hashing, and authentication behavior.

## Milestone Three: Algorithms and Data Structures

The algorithms and data structures enhancement adds historical analysis to the existing weight records instead of only storing and displaying them.

### Added algorithmic features

- Added an immutable `WeightRecord` model for analysis outside the database layer.
- Added `WeightAnalyzer` to sort records by their actual dates instead of relying on database insertion order.
- Added inclusive date-range filtering. The sorted collection is searched with lower-bound and upper-bound binary searches to locate the requested range.
- Added one-pass calculations for starting weight, current weight, total change, average, minimum, and maximum.
- Added a seven-entry moving average using an `ArrayDeque` as a sliding window. The running sum is updated by adding the newest value and removing the oldest value instead of recalculating each window from the beginning.
- Added trend classification as Increasing, Decreasing, or Stable.
- Added goal-distance and goal-progress calculations.
- Added a progress-analysis section to the main screen and date-range controls for viewing and analyzing a selected period.
- Added unit tests for chronological sorting, range filtering, moving-average calculations, summary statistics, trend behavior, and goal progress.

### Complexity notes

Chronological sorting is O(n log n). After sorting, summary statistics and the moving-average series are calculated in O(n) time. The sliding window uses at most seven weight values, so its working memory remains constant for the configured window size. Date-range boundaries are located with binary search in O(log n), followed by O(k) time to copy the k matching records.

## Verification note

The pure Java analysis classes were compiled and exercised with a smoke test that verified chronological sorting, date-range filtering, the moving average, trend direction, and goal-progress calculations. The full Android project should also be opened and built in Android Studio before final submission so the Android-specific integration can be verified on the development machine.

## Milestone Four: Databases

The database enhancement upgrades the local SQLite persistence layer from schema version 1 to version 2 while preserving the software engineering and algorithm improvements from the earlier milestones.

### Added database features

- Added a non-destructive version 1 to version 2 migration so existing users, weight entries, and goal values are carried forward instead of dropping all tables during an upgrade.
- Changed the user credential column to `password_hash` to match how credentials are now stored.
- Simplified the goals table to a one-to-one relationship where `user_id` is the primary key.
- Enabled SQLite foreign key enforcement and added `ON DELETE CASCADE` relationships from weight and goal records to users.
- Added database-level weight and goal constraints that match the application's accepted range of 20 to 1500 pounds.
- Added an index on `(user_id, entry_date, id)` to support user-specific history and date-range queries.
- Added `DatabaseDateCodec` so dates shown as `MM/dd/yyyy` in the interface are stored as ISO `yyyy-MM-dd` values. ISO storage lets SQLite sort and compare dates correctly.
- Added parameterized date-range queries that return only the records needed by the selected user and range.
- Added user ownership to update and delete conditions so one signed-in user cannot modify another user's weight entry by entry ID alone.
- Combined an optional goal change and a new weight entry into one SQLite transaction so both changes succeed or both are rolled back.
- Changed the repository contract to return `WeightRecord` objects instead of exposing a database-specific nested record type to the service layer.
- Disabled Android application backup so the local database and saved session data are not copied through the platform backup feature.
- Added unit tests for date conversion and Android instrumentation tests for chronological range queries, ownership checks, constraints, transactional saving, and version 1 data migration.

### Verification note

The pure Java date conversion, validation, password security, and weight analysis classes can be compiled independently. The database integration tests require an Android device or emulator because they use Android SQLite APIs. The full project should be opened and built in Android Studio before final ePortfolio submission.
