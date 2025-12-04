CS157A Final Project – Clinical Database Management System

Team Members:
Alex Than
Zayba Syed
Minh Tran
Sanaa Stanezai

This project is a console-based Java application that connects to a MySQL database using JDBC.
It supports viewing, inserting, updating, deleting records, and includes a transactional workflow with COMMIT and ROLLBACK to guarantee atomicity.

<img width="830" height="485" alt="Screenshot 2025-12-03 at 11 21 20 PM" src="https://github.com/user-attachments/assets/a48b3672-19e8-4b62-abe5-b9e0ffa36cc7" />



#Project Structure
CS157A_FinalProject_TeamName/
│
├── Main.java                    # Java console app with menu + JDBC + transactions
├── create_and_populate.sql      # All CREATE TABLE, INSERT sample data, view, procedure
├── app.properties               # Database connection info
├── README.md                    # Documentation (this file)
├── ai_log.md                    # AI collaboration record
├── Team-roles.txt               # Member contributions + reflections
└── video_demo.mp4               # Screen-recorded demo (≤ 6 minutes)

Connection info:

#Application built and run
1. Create Database:
    - Open MySQl workbench
    - CREATE DATABASE clinicManagement
    - USE clinicManagement
    - Create required tables and constraints
    - Populate tables
    - Run SQL Script

2. Setting Up app properties
    - Change Url depending on host schema
    - replace password with your own database credentials
    - app properties should look like
            db.url=jdbc:mysql://localhost:3306/clinical_management?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
            db.user=root
            db.password=PasswordHere

3. Compile Java files
     - Use command for Windows: javac -cp ".;mysql-connector-j-9.5.0.jar" Main.java
     - Use command for Mac: javac -cp ".:mysql-connector-j-9.5.0.jar" Main.java

4. Run
     - Use command for Windows: java -cp ".;mysql-connector-j-9.5.0.jar" Main
     - Use command for Mac: java -cp ".:mysql-connector-j-9.5.0.jar" Main

5. If the connection is successful, the console prints: "Connected to DB."
<img width="964" height="56" alt="Screenshot 2025-12-03 at 9 04 33 PM" src="https://github.com/user-attachments/assets/a4daa729-cef2-4b3f-b9e6-29312f095f37" />


How the Application Was Built (Step-by-Step)
1. Database Schema Design
Built using MySQL Workbench
Added tables such as Patient, Doctor, Medication, Appointment, Plan
Applied primary keys, foreign keys, UNIQUE constraints, CHECK constraints
Inserted sample records needed for testing

2. JDBC Setup
Loaded MySQL driver 
Created DBUtil.getConnection() to read from app.properties
Verified connectivity before implementing logic

3. Console Menu + Scanner
The menu in Main.java supports:
View Patients
View Doctors
View Medications (from VIEW)

Insert Medication
Update Patient
Delete Patient
Schedule Appointment (Stored Procedure)
Transaction Workflow (Commit/Rollback)

4. PreparedStatements Everywhere
All SQL operations use PreparedStatement to avoid SQL injection and to bind inputs safely.

5. Transaction Workflow
We created a combined workflow:
Schedule an appointment (via stored procedure)
Insert a medication for the same visit
If any part fails (e.g., constraint violation), the system: ROLLBACK
Otherwise: COMMIT

6. Input Validation + Error Handling
Validated integers, doubles, empty input
Provided detailed SQLException messages
Checked foreign key and CHECK constraint violations

MySQL & JDBC Versions
MySQL Server: 8.0
MySQL Workbench: 8.0
MySQL Connector/J: 8.4.0 (or any 8.x)
Java Version: Java 17+ recommended





