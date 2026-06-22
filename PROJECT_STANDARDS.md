# Project Setup & Standards

This document outlines the technical standards and team organization for the Personal Finance
Manager.

## 1. Technical Standards

### Data & Logic

* **Room Database:** All financial data must persist in Room. No passing large objects through
  Intents.
* **Repository Pattern:** UI communicates with a `FinanceRepository`, not the DB directly.
* **View Binding:** Enabled in `build.gradle`. Use `binding.viewName` instead of `findViewById`.
* **CurrencyFormatter:** A single utility class for money strings. **Never** hardcode currency
  symbols in layouts.

### UI & Styling

* **Material 3:** Use theme attributes (`?attr/colorPrimary`) for all colors to support Dark Mode
  natively.
* **Navigation:** Use Jetpack Navigation Component with a Single-Activity architecture.
* **Charts:** Use **MPAndroidChart** for spending visualizations.

---

## 2. Feature-Based Team Split

Instead of Logic vs. UI, we split by functional areas. Each developer handles their own Data, Logic,
and UI within their assigned features.

### Shared Infrastructure (Do Together First)

- [X] Initialize Git & `.gitignore`.
- [X] Define the `Transaction` entity and Database schema.
- [/] Set up the basic Material 3 Theme and Main Navigation Graph.

### Person A: Core Operations & Flow

- [/] **Transactions (Add/History):** RecyclerView, search/filter, and the entry system.
- [/] **Home Screen:** Summary cards.
- [/] **App Infrastructure:** Splash screen animation and Settings (Currency, PIN lock).

### Person B: Analysis & Planning

- [ ] **Statistics:** Pie/Bar charts for spending and monthly comparisons.
- [ ] **Budget Planner:** Limit setting, progress bars, and over-budget alerts.
- [ ] **Savings Goals:** Goal creation, deadline tracking, and progress visualization.
- [ ] **Data Tools:** Mock data generator for testing charts and budgets.

---

## 3. File Organization

To prevent merge conflicts and keep the project clean, we follow a feature-layered package
structure:

```text
com.mopr.personal_finance_manager/
├── data/
│   ├── local/           # Room DB, DAOs, Entities
│   ├── remote/          # Firestore
│   ├── repository/      # Repository implementation
│   └── model/           # POJOs and Enum types
├── ui/
│   ├── home/            # Summary cards, Recent 5 preview
│   ├── transactions/    # List (History) and Entry (Add/Edit)
│   ├── statistics/      # Charts and Comparison views
│   ├── budget/          # Progress bars and limits
│   ├── savings/         # Goals tracking
│   ├── settings/        # Currency toggle, PIN, Export/Import
│   └── common/          # Reusable UI components (Adapters, custom views, splash)
├── util/                # CurrencyFormatter, DateUtils, Constants
└── MainActivity.java    # Single activity container
```
