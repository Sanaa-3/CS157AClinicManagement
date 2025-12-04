CS157A Final Project – Clinical Database Management System

This project is a console-based Java application that connects to a MySQL database using JDBC.
It supports viewing, inserting, updating, deleting records, and includes a transactional workflow with COMMIT and ROLLBACK to guarantee atomicity.

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
    - CREATE DATABASE clinicManagmnent
    - USE clinicManagment
    - Create required tables and constraints
    - Populate tables
    - Run SQL Script

2. Setting Up app properties
    - Change Url depending on host schema
    - replace password with your own database credentials

3. Compile Java files
     - Compile DBUTIL to set up connection by parsing app properties 
     - Use command: javac -cp ".;mysql-connector-j-9.5.0.jar" Main.java DBUtil.java

4. Run
     - Use command:  java -cp ".;mysql-connector-j-9.5.0.jar" Main


