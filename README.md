# Splitify 💸

> **Splitify** (SplitFlow) is a modern, offline-first Android application designed for seamlessly splitting expenses, tracking shared balances with friends, and analyzing personal spending.

---

## 📱 About The App

Splitify simplifies group expense management. Whether you're traveling with friends, sharing apartment rent, or dining out, Splitify calculates exact individual balances, tracks owed amounts, and generates exportable PDF statements—all without requiring an external server or account login.

---

## ✨ Key Features

- **Expense Splitting**: Log expenses with titles, amounts, dates, and categories (Food, Travel, Stay, Shopping, etc.), and split costs across friends.
- **Friend Balance Management**: Keep track of who owes whom with individual friend profiles and settlement calculation summaries.
- **Spending Analytics**: Visual breakdown of total spending by category with interactive filter chips.
- **PDF Statement Generation**: Export clean, professional PDF settlement statements locally using custom Android Canvas rendering.
- **SMS & Text Summaries**: Instantly generate and share text or SMS settlement summaries with a single tap.
- **Material 3 Design**: Fully responsive UI supporting Dynamic Light and Dark modes with seamless edge-to-edge layouts.

---

## 🤖 AI-Powered Development

> 💡 **Fully Developed Using Gemini Chat Agent in Android Studio**  
> Splitify was engineered end-to-end in collaboration with the **Gemini Chat Agent inside Android Studio**. The AI agent assisted across all phases of development, including:
> - **Architectural Setup**: Structuring Clean Architecture (Domain, Data, Presentation layers) and MVVM pattern.
> - **UI & Theme Engineering**: Crafting declarative Jetpack Compose layouts, edge-to-edge handling, and Material 3 design systems.
> - **Local Data Operations**: Designing Room Database schemas, DAOs, and repository patterns.
> - **Document Generation**: Implementing custom PDF Canvas rendering and FileProvider integration.
> - **Refactoring & Debugging**: Systematic code optimization, memory leakage prevention, and UI layout insets resolution.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose with Material 3 Design
- **Architecture Pattern**: Clean Architecture + MVVM (Model-View-ViewModel)
- **Local Database**: Room Database + KSP
- **Async & Reactive**: Kotlin Coroutines, StateFlow, Flow
- **Navigation**: Jetpack Navigation Compose
- **Document Export**: Android Canvas PDF Generation & `FileProvider`
- **Target SDK**: Android 15 (API 35) / Compile SDK 37

---

## 📂 Project Structure

```text
com.example.splitify/
├── data/           # Entities, Room DAOs, Database, Repository Implementations
├── domain/         # Domain Models, Repository Interfaces, Use Cases
├── ui/             # Composable Screens (History, Friends, Analytics, AddExpense) & ViewModels
├── di/             # Manual Dependency Injection (AppContainer)
└── util/           # PDF Exporting, Utilities, and Helpers
```

---

## 🚀 Getting Started

1. **Clone the Repository**
   ```bash
   git clone https://github.com/yashajagiya/Splitify-ExpenseTracker_and_Splitter.git
   ```
2. **Open in Android Studio**
   Open the root directory in **Android Studio (Ladybug or newer)**.
3. **Build & Run**
   Select the `app` run configuration and deploy to an Android device or emulator running **Android 7.0 (API 24)** or higher.

---

## 📄 License

Distributed under the Apache License 2.0. See [`LICENSE`](file:///C:/Users/yash/AndroidStudioProjects/Splitify/LICENSE) for more information.
