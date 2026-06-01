# Grocery Shopping Android App

Welcome to the **Grocery Shopping App**, a state-of-the-art native Android e-commerce application designed with a high-performance offline-first architecture. It features clean search capabilities, reactive cart modifications, and secure local checkout transactions backed by **Jetpack Compose (Material 3)**, **MVVM Architecture**, **Room Database SQLite**, and **SharedPreferences**.

---

## 🚀 Key Highlights & Features
- **🔒 Secured Profile Lock**: Enter details on startup to browse items. Inputs are fully verified and persist across app sessions.
- **🍉 Real-time Categorization**: Live filter grocery products (Fruits, Vegetables, Dairy, Bakery, etc.) combined instantly with keyword typing.
- **🛒 Persistent Shopping Cart**: Modify product quantities, review dynamic subtotals, and update orders safely with automated inventory checks.
- **☁️ Real-time Cloud Synchronization**: Backup user profiles, active cart states, and historical transaction ledgers to Firebase Cloud Firestore dynamically.
- **📄 Relational Local Invoice Ledger**: Safely save completed transactions with a custom unique reference code (`ORDxxxxx`), exact local timestamps, and sub-item records.
- **🧪 Fast Performance Testing**: Engineered with JVM-based unit tests, Robolectric simulations, and Roborazzi screenshot verification to ensure high code stability.

---

## 📂 Quick-Start Development Guide

### 1. Prerequisites
- **Java SE Development Kit**: JDK 17 (Mandatory).
- **IDE**: Android Studio Koala/Jellyfish or newer.
- **Target OS**: Compatible with Android 7.0 (API Level 24) up to Android 16 (API Level 36).

### 2. Set Up the Project
1. Clone this repository:
   ```bash
   git clone https://github.com/your-username/grocery-shopping.git
   cd grocery-shopping
   ```
2. Open Android Studio and choose **Open an Existing Project**, selecting this repository's root directory.
3. Allow Android Studio to download dependencies and sync Gradle.

### 3. Run and Verify
- To run the application on your emulator, click the **Run App** play button.
- To execute the automated backend unit test suites:
  ```bash
  gradle :app:testDebugUnitTest
  ```
- To capture reference mock UI screenshots:
  ```bash
  gradle :app:recordRoborazziDebug
  ```

---

## 🗄️ "Where is the Database?" (SQLite Inspection Guideline)

The application uses **Room Database** to store active carts and finalized purchase tickets locally in SQLite on the device.
- **Storage Location**: `/data/data/com.example/databases/grocery_database` on the target device.
- **How to Inspect Real-time Data**:
  1. Boot your virtual emulator.
  2. Open Android Studio and navigate to `View -> Tool Windows -> App Inspection` -> **Database Inspector**.
  3. Select the `grocery_database` process to dynamically read and edit local database tables (`orders`, `cart_items`, `order_details`) as you interact with the app.

---

## 📚 Technical Documentation Hub

For in-depth explanations of modules, custom setup guides, architectural workflow diagrams, troubleshooting tips, and instructions for adding new features, please consult our main developer guide:

👉 **[Go to DOCUMENTATION.md](./DOCUMENTATION.md)**

Enjoy editing and building with this modern, responsive, and performance-oriented Android shopping environment!
