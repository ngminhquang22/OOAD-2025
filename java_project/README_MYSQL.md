# MySQL Setup Instructions

The application has been updated to use MySQL instead of SQLite.

## Prerequisites

1.  **MySQL Server**: Ensure you have MySQL Server installed and running.
2.  **Database Creation**: The application is configured to create the database `GamingLoungeDB` automatically if it doesn't exist (`createDatabaseIfNotExist=true`).

## Configuration

Open `src/main/java/com/gaminglounge/utils/DatabaseHelper.java` and update the following constants with your MySQL credentials:

```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/GamingLoungeDB?createDatabaseIfNotExist=true";
private static final String USER = "root";
private static final String PASS = "password"; // <--- Change this to your actual password
```

## Running the Application

1.  Open the project in your IDE (VS Code, IntelliJ, etc.).
2.  Run `App.java` located in `src/main/java/com/gaminglounge/App.java`.
3.  The application will automatically connect to MySQL, create the tables, and seed initial data.

## Troubleshooting

*   **Connection Refused**: Check if MySQL service is running and listening on port 3306.
*   **Access Denied**: Check if the username and password in `DatabaseHelper.java` are correct.
*   **Driver Not Found**: Ensure Maven has downloaded the `mysql-connector-java` dependency. You may need to reload the Maven project.
