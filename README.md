# SpendLog

Haorong Song, student ID 3184339.

A small Android app for writing down spending. Coffee, lunch, the bus, Tesco. Each entry keeps a name, an amount, a category, a date, a note, and the location from the moment it was saved.

Milestone 1 uses three activities, not one activity swapping screens:

- `MainActivity` is the list and the month total.
- `AddExpenseActivity` is the add and edit form.
- `ExpenseDetailActivity` is one expense, with Edit, Delete, and Open in Maps.

The first two jumps are explicit intents. Open in Maps is an implicit `ACTION_VIEW` on a `geo:` uri. Expenses are stored in SQLite on the phone. Location is read once, on save.

## Open it

Open this folder in Android Studio (2026.2, AGP 9.2.1) and run the `app` configuration.

The first launch inserts five sample rows so the list is not empty. Bus ticket has no location, so Open in Maps on that row shows the "no location" toast. Coffee has coordinates, so Open in Maps can hand it to a maps app.

## Git

Remote URL is not set yet. After you create the GitHub or GitLab repository, put the URL in `GIT_REPOSITORY.txt` before you upload the Moodle zip.
