# 🎅 Family Secret Santa

A personal Android app to manage family Secret Santa gift exchanges — no ads, no accounts, no third-party services. Everything lives on your device.

## Features

- **Multiple groups** — create a new group each year; all history is saved
- **Participant management** — add, edit, or remove participants (first name, last name, phone number)
- **Smart random assignment** — guaranteed derangement algorithm ensures nobody draws themselves and there are no duplicate assignments
- **Exclusions** — set two-way (`↔`) or one-way (`→`) exclusions between participants before generating assignments
- **SMS notifications** — sends each participant their assignment via text message directly from the app (no Messages app pop-up)
- **Resend** — resend the assignment message to all participants at once, or to a single person individually
- **Assignment lookup** — tap any participant to see who they have, on demand
- **Fully offline** — no internet connection required, no backend, no account

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Navigation:** Navigation Compose
- **Database:** Room (local SQLite)
- **Architecture:** MVVM (ViewModel + StateFlow + Repository)
- **Min SDK:** API 26 (Android 8.0)

---

## Installation

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (latest stable recommended)
- An Android device running **Android 8.0 (API 26) or higher**
- USB cable or a way to transfer an APK to your device (email, Google Drive, etc.)

### Step 1 — Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/secret-santa.git
cd secret-santa
```

### Step 2 — Open in Android Studio

- Launch Android Studio
- Select **File → Open** and navigate to the cloned folder
- Wait for the initial Gradle sync to complete

### Step 3 — Personalize the app *(optional)*

**Change the package name** *(only needed if you plan to have multiple versions installed side by side)*

In `app/build.gradle.kts`, update `applicationId`:
```kotlin
applicationId = "com.yourname.secretsanta"
```

Then right-click the `com.kevin.secretsanta` package in Android Studio → Refactor → Rename, and update it to match.

### Step 4 — Build a signed APK

- **Build → Generate Signed App Bundle / APK...**
- Select **APK** (not App Bundle)
- Click **Create new...** to create a keystore if you don't have one
  - Save your `.jks` file somewhere safe — you need it for all future updates
  - **Do not commit your keystore to GitHub**
- Select build variant **release**
- Click **Finish**

The APK will be output to `app/release/app-release.apk`.

### Step 5 — Enable unknown sources on your Android device

Since this APK isn't from the Play Store, you need to allow installation from unknown sources once:

- Go to **Settings → Apps → Special app access → Install unknown apps**
- Find the app you'll use to open the APK (Files, Chrome, etc.) and enable **Allow from this source**

### Step 6 — Install the APK

Transfer `app-release.apk` to your device (USB, email, Google Drive, etc.), open it, and tap **Install**.

---

## How to use

1. **Create a group** — tap `+` on the home screen, enter a group title, year, and your name (used in the message text)
2. **Add participants** — tap `+` on the group screen to add each person's name and phone number (you can include yourself)
3. **Set exclusions** *(optional)* — tap **Manage Exclusions** to add rules:
   - **Two-way (`↔`)** — neither person can be assigned to the other
   - **One-way (`→`)** — Person A cannot get Person B, but Person B can still get Person A
4. **Generate assignments** — tap **Generate Assignments**; the algorithm respects all exclusions and guarantees no self-assignments
5. **Send texts** — tap **Resend All** to text everyone their assignment at once, or tap the send icon `✉` on any individual participant
6. **Look up an assignment** — tap the search icon `🔍` on any participant to see who they have

The message sent to each person looks like:
> Hey [FirstName], it's [OrganizerName]. For the [Year] family Secret Santa, you have [RecipientFirstName] [RecipientLastName].

---

## Important notes

### SMS permission
The app uses `SEND_SMS` directly via `SmsManager`, which works for sideloaded personal-use apps. Android will prompt you to grant SMS permission the first time you try to send. This permission is **not** approved for Play Store distribution without being the default SMS app.

### Keystore
Keep your `.jks` keystore file backed up and **never commit it to GitHub**. If you lose it, you'll have to uninstall and reinstall the app (wiping your local database) to install a new signed version.

### Carrier throttling
Carriers can occasionally rate-limit or flag rapid-fire SMS sends. For typical family group sizes (under 20 people) this is rarely an issue.

---

## License

[MIT](LICENSE)
