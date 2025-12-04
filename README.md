
# CS157A Final Project – Clinical Database Management System

### Team Members:
- Alex Than  
- Zayba Syed  
- Minh Tran  
- Sanaa Stanezai  

This project is a console-based Java application that connects to a MySQL database using JDBC.  
It supports viewing, inserting, updating, deleting records, and includes a transactional workflow with COMMIT and ROLLBACK to guarantee atomicity.

<img width="669" height="399" alt="Console App Menu" src="https://github.com/user-attachments/assets/e56fffe9-ed52-4089-9fe0-0e1190185a9a" />

# Project Structure

```
CS157A_FinalProject_G4/
│
├── Main.java                    # Java console app with menu + JDBC + transactions
├── create_and_populate.sql      # All CREATE TABLE, INSERT sample data, view, procedure
├── app.properties               # Database connection info
├── README.md                    # Documentation (this file)
├── ai_log.md                    # AI collaboration record
├── Team-roles.txt               # Member contributions + reflections
└── video_demo.mp4               # Screen-recorded demo (≤ 6 minutes)
```


# Connection info:

# Application built and run
## 1. Create Database:
- Open MySQl workbench  
- `CREATE DATABASE clinicManagmnent`  
- `USE clinicManagment`  
- Create required tables and constraints  
- Populate tables  
- Run SQL Script  

## 2. Setting Up app properties
- Change Url depending on host schema  
- Replace password with your own database credentials  

**app.properties should look like:**
db.url=jdbc:mysql://localhost:3306/clinical_management?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.user=root
db.password=PasswordHere


## 3. Compile Java files
**Windows:**
javac -cp ".;mysql-connector-j-9.5.0.jar" Main.java
**Mac:**
javac -cp ".:mysql-connector-j-9.5.0.jar" Main.java

## 4. Run
**Windows:**
java -cp ".;mysql-connector-j-9.5.0.jar" Main
**Mac:**
java -cp ".:mysql-connector-j-9.5.0.jar" Main

## 5. If the connection is successful, the console prints:
"Connected to DB."

<img width="964" height="56" alt="Screenshot 2025-12-03 at 9 04 33 PM" src="https://github.com/user-attachments/assets/a4daa729-cef2-4b3f-b9e6-29312f095f37" />


# How the Application Was Built (Step-by-Step)
## 1. Database Schema Design
- Built using MySQL Workbench  
- Added tables such as Patient, Doctor, Medication, Appointment, Plan  
- Applied primary keys, foreign keys, UNIQUE constraints, CHECK constraints  
- Inserted sample records needed for testing  

## 2. JDBC Setup
- Loaded MySQL driver  
- Created DBUtil.getConnection() to read from app.properties  
- Verified connectivity before implementing logic  

## 3. Console Menu + Scanner

The menu in Main.java supports:
- View Patients  
- View Doctors
- View Hospitals
- Viwe Medications 
- View PatientMedications (a View)  
- Insert Medication  
- Update Patient  
- Delete Patient  
- Schedule Appointment (Stored Procedure)  
- Transactional Workflow - Transfer Doctor to New Hospital (Commit/Rollback)
- View Appointments
- View DoctorHospital Assignments

## 4. PreparedStatements
- All SQL operations use PreparedStatement to avoid SQL injection and to bind inputs safely.  

## 5. Transactional Workflow
We created a combined workflow:
- Check if a DoctorID is attached to a valid row in Doctor
- Check if a current HospitalID is attached to a valid row in Hospital
- Check if a new HospitalID is attached to a valid row in Hospital
- Update row containing DoctorID, current HospitalID to become DoctorID, new HospitalID

If any part fails (e.g. foreign key doesn't exist in host table), the system: **ROLLBACK**  
Otherwise: **COMMIT**  

## 6. Input Validation + Error Handling
- Validated integers, doubles, empty input  
- Provided detailed SQLException messages  
- Checked foreign key and CHECK constraint violations  

# MySQL & JDBC Versions
- MySQL Server: 8.0  
- MySQL Workbench: 8.0  
- MySQL Connector/J: 8.4.0 (or any 8.x)  
- Java Version: Java 17+ recommended  






