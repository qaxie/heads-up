# HeadsUp

HeadsUp lets you choose which apps are allowed to show heads-up notifications — the banners that slide down over whatever you're doing. Apps you don't want interrupting you are silenced, but their notifications still arrive quietly in the shade.

---

## Features

- Allow or silence heads-up notifications per app
- Pause all suppression with a single global toggle
- Search through your installed apps
- See how many notifications were suppressed today
- Settings persist across restarts and reboots

---

## Screenshots

_Coming soon_

---

## Installation

### Requirements
- Android 7.0 (API 24) or higher
- Android Studio with a connected device or emulator

### Build from source

1. Clone the repository
   ```bash
   git clone https://github.com/qaxie/heads-up.git
   ```
2. Open the project in Android Studio
3. Run the app on your device or emulator
   ```bash
   ./gradlew installDebug
   ```
4. Grant notification access when prompted

---

## Usage

1. Open HeadsUp and grant notification access
2. Tap **Manage Apps** to see all installed apps
3. Toggle apps OFF to silence their heads-up banners
4. Use the global toggle on the home screen to pause suppression entirely

---

## Development

> **This project is a work in progress.**

HeadsUp is being built using a **spec-driven AI coding** approach. Rather than letting AI generate the app freely, the behaviour of each screen and feature is defined upfront in specification documents (see `specs/`). The AI follows these specs when writing code — keeping the implementation grounded and intentional instead of just guessing what the app should do.

The specs cover user stories, acceptance criteria, screen states, and edge cases. Code is only written once a spec is agreed on.

---

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE).

---

## Contributing

Contributions are welcome. Feel free to open an issue or submit a pull request.
