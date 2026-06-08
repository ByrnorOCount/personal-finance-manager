# Day 1 Project Setup & Standards

This document outlines the technical standards and team organization for the Personal Finance Manager.

## 1. Technical Standards

### Data & Logic
*   **Room Database:** All financial data must persist in Room. No passing large objects through Intents.
*   **Repository Pattern:** UI communicates with a `FinanceRepository`, not the DB directly.
*   **View Binding:** Enabled in `build.gradle`. Use `binding.viewName` instead of `findViewById`.
*   **CurrencyFormatter:** A single utility class for money strings. **Never** hardcode currency symbols in layouts.

### UI & Styling
*   **Material 3:** Use theme attributes (`?attr/colorPrimary`) for all colors to support Dark Mode natively.
*   **Navigation:** Use Jetpack Navigation Component with a Single-Activity architecture.
*   **Charts:** Use **MPAndroidChart** for spending visualizations.

---

## 2. Feature-Based Team Split

Instead of Logic vs. UI, we split by functional areas. Each developer handles their own Data, Logic, and UI within their assigned features.

### Shared Infrastructure (Do Together First)
- [ ] Initialize Git & `.gitignore`.
- [ ] Define the `Transaction` entity and Database schema.
- [ ] Set up the basic Material 3 Theme and Main Navigation Graph.

### Person A: Transaction Management
- [ ] **Transaction List:** RecyclerView to show history with search/filter.
- [ ] **Entry System:** "Add Transaction" screen with category selection and date picker.
- [ ] **Data Logic:** CRUD operations in DAO/Repository for transactions.

### Person B: Dashboard & Analytics
- [ ] **Visual Analytics:** Implement MPAndroidChart for spending by category.
- [ ] **Summary Cards:** Total balance, monthly income vs. expense overview.
- [ ] **Mock Data Generator:** System to generate 30+ transactions for chart testing.

---

## 3. File Organization

To prevent merge conflicts and keep the project clean, we follow a feature-layered package structure:

```text
com.mopr.personal_finance_manager/
├── data/
│   ├── local/           # Room DB, DAOs, Entities
│   ├── repository/      # Repository implementation
│   └── model/           # POJOs and Enum types
├── ui/
│   ├── dashboard/       # Dashboard Fragment & ViewModel
│   ├── transactions/    # List and Entry Fragments & ViewModels
│   └── common/          # Reusable UI components (Adapters, custom views)
├── util/                # CurrencyFormatter, DateUtils, Constants
└── MainActivity.java    # Single activity container
```
