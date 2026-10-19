# SpendWise Personal Expense Tracker

A Java Swing expense tracker with MySQL storage, login and registration, income and expense records, and monthly budgets.

## Project layout

- `src/main/java/com/spendwise/` — Java application source
  - `database/` — MySQL connection
  - `gui/` — login, registration, and dashboard windows
  - `model/` — transactions, income, expense, budget, and operations interface
  - `service/` — authentication, transaction, and budget services
  - `util/` — password hashing helpers
- `database/schema.sql` — database structure export (place the exported SQL file here)
- `docs/` — UML diagram image and editable PlantUML source
- `pom.xml` — Maven project and MySQL Connector/J dependency

## Requirements

- Java 26 (the version configured by `pom.xml`)
- Apache Maven (NetBeans can manage Maven builds)
- MySQL Server
- MySQL Workbench (for setting up the schema)

## Database setup

1. Start MySQL Server.
2. Open `database/schema.sql` in MySQL Workbench and execute the script to create the `spendwise` database tables.
3. Set these Windows user environment variables to match your local MySQL account:
   - `SPENDWISE_DB_USER`
   - `SPENDWISE_DB_PASSWORD`
4. Restart NetBeans after setting the environment variables so the application can read them.

The application connects to `jdbc:mysql://localhost:3306/spendwise`. Keep real credentials out of source files and screenshots.

## Run in NetBeans

1. In NetBeans, choose **File → Open Project** and select this `SpendWise-Clean` folder.
2. Wait for Maven dependencies to load.
3. Run the project. The main class is `com.spendwise.spendwise.SpendWise`.

## Main features

- Register and log in to a user account
- Add, edit, view, filter, and delete income and expense transactions
- View balance, income, and expense summaries
- Set a monthly budget and review spending against it

## UML documentation

Open `docs/SpendWise_Class_Diagram.png` to view the diagram. Edit `docs/SpendWise_Class_Diagram.puml` with PlantUML-compatible tooling.
