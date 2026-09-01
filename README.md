# E-Shifts

E-Shifts is a Java application for managing workplace shifts.

The application supports two interfaces:

- JavaFX GUI
- Command-Line Interface (CLI)

## Requirements

Before running the application, install:

- JDK 23
- MySQL Server 8+
- MySQL Workbench
- Internet connection on the first execution, so Maven can download dependencies

## Database setup

1. Open MySQL Workbench.
2. Create a schema named `eshifts_v2`.
3. Select the `eshifts_v2` schema as the active database.
4. Open and execute the following script:

   ```text
   database/e-shifts_v2_scema.sql
   ```

5. Configure the database URL and username in:

   ```text
   src/main/resources/db.properties
   ```

   Example:

   ```properties
   db.url=jdbc:mysql://localhost:3306/eshifts_v2
   db.user=root
   ```



## Run on Windows

Open the project folder and double-click:

```text
run-app.bat
```

Alternatively, open PowerShell inside the project root and run:

```powershell
.\run-app.bat
```

The application compiles automatically and asks which interface to start:

```text
[1] GUI
[2] CLI
```

Enter `1` to launch the JavaFX GUI or `2` to launch the CLI.

