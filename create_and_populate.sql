-- CS157A Final Project - Clinical Management System (Group G4)
-- Minh Tran, Zayba Syed, Alex Than, Sanaa Stanezai

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS DoctorHospital;
DROP TABLE IF EXISTS Appointment;
DROP TABLE IF EXISTS Medication;
DROP TABLE IF EXISTS Patient;
DROP TABLE IF EXISTS Doctor;
DROP TABLE IF EXISTS Hospital;
DROP TABLE IF EXISTS InsurancePlan;
DROP VIEW IF EXISTS PatientMedicationView;
DROP PROCEDURE IF EXISTS schedule_appointment;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================
-- 1. TABLE CREATION
-- =========================

CREATE TABLE InsurancePlan (
    PlanID INT PRIMARY KEY AUTO_INCREMENT,
    Provider VARCHAR(100) NOT NULL,
    Coverage VARCHAR(200)
);

CREATE TABLE Hospital (
    HospitalID INT PRIMARY KEY AUTO_INCREMENT, 
    Name VARCHAR(100) NOT NULL,
    Address VARCHAR(200) NOT NULL,
    PhoneNumber VARCHAR(20) NOT NULL
);

CREATE TABLE Doctor (
    DoctorID INT PRIMARY KEY AUTO_INCREMENT,
    Name VARCHAR(100) NOT NULL,
    Discipline VARCHAR(100),
    Email VARCHAR(100),
    PhoneNumber VARCHAR(20)
);

CREATE TABLE Patient (
    PatientID INT PRIMARY KEY AUTO_INCREMENT,
    Name VARCHAR(100) NOT NULL,
    Birthdate DATE,
    Email VARCHAR(100),
    PhoneNumber VARCHAR(20),
    Address VARCHAR(200),
    PlanID INT,
    CONSTRAINT fk_pat_plan
      FOREIGN KEY (PlanID) 
      REFERENCES InsurancePlan(PlanID)
      ON UPDATE CASCADE
      ON DELETE SET NULL
);

CREATE TABLE Medication (
  MedicationID INT PRIMARY KEY AUTO_INCREMENT,               
  PatientID    INT NOT NULL,
  DoctorID     INT NOT NULL,                  
  Name         VARCHAR(120) NOT NULL,
  Cost         DECIMAL(8,2) NOT NULL,
  Status       VARCHAR(10) NOT NULL,          
  Dosage       VARCHAR(50),
  Frequency    VARCHAR(50),
  PlanID       INT,                      
  CONSTRAINT ck_med_cost   CHECK (Cost >= 0 AND Cost <= 1000),
  CONSTRAINT ck_med_status CHECK (Status IN ('Paused','Completed','Ongoing')),
  CONSTRAINT fk_med_doctor
    FOREIGN KEY (DoctorID)
    REFERENCES Doctor(DoctorID)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT fk_med_patient
    FOREIGN KEY (PatientID)
    REFERENCES Patient(PatientID)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT fk_med_plan
    FOREIGN KEY (PlanID)
    REFERENCES InsurancePlan(PlanID)
    ON UPDATE CASCADE
    ON DELETE SET NULL        
);

CREATE TABLE Appointment (
  PatientID   INT NOT NULL,
  DoctorID    INT NOT NULL,
  HospitalID  INT NOT NULL,
  ApptDate    DATE NOT NULL,
  ApptTime    TIME NOT NULL,
  VisitReason VARCHAR(200),
  Cost        DECIMAL(8,2) NOT NULL,
  PlanID      INT NULL,
  PRIMARY KEY (PatientID, ApptDate, ApptTime),

  CONSTRAINT fk_appt_patient
    FOREIGN KEY (PatientID)
    REFERENCES Patient(PatientID)
    ON UPDATE CASCADE
    ON DELETE CASCADE,

  CONSTRAINT fk_appt_doctor
    FOREIGN KEY (DoctorID)
    REFERENCES Doctor(DoctorID)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,

  CONSTRAINT fk_appt_hospital
    FOREIGN KEY (HospitalID)
    REFERENCES Hospital(HospitalID)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,

  CONSTRAINT fk_appt_plan
    FOREIGN KEY (PlanID)
    REFERENCES InsurancePlan(PlanID)
    ON UPDATE CASCADE
    ON DELETE SET NULL,

  CONSTRAINT ck_appt_cost CHECK (Cost >= 0 AND Cost <= 5000),

  CONSTRAINT uq_doctor_slot UNIQUE (DoctorID, ApptDate, ApptTime)
);

CREATE TABLE DoctorHospital(
   HospitalID INT NOT NULL, 
   DoctorID   INT NOT NULL,

   CONSTRAINT fk_hosp
     FOREIGN KEY (HospitalID)
     REFERENCES Hospital(HospitalID)
     ON UPDATE CASCADE
     ON DELETE CASCADE,

   CONSTRAINT fk_doc
     FOREIGN KEY (DoctorID)
     REFERENCES Doctor(DoctorID)
     ON UPDATE CASCADE
     ON DELETE CASCADE,

   PRIMARY KEY (HospitalID, DoctorID)
);

-- =========================
-- 2. TRIGGER
-- =========================

DELIMITER $$
CREATE TRIGGER App_BI_DefaultPlan
BEFORE INSERT ON Appointment
FOR EACH ROW
BEGIN
  IF NEW.PlanID IS NULL THEN
    SET NEW.PlanID = (
      SELECT p.PlanID
      FROM Patient p
      WHERE p.PatientID = NEW.PatientID
    );
  END IF;
END$$
DELIMITER ;

-- =========================
-- 3. VIEW (NEW)
-- =========================

CREATE VIEW PatientMedicationView AS
SELECT 
    p.PatientID,
    p.Name AS PatientName,
    m.MedicationID,
    m.Name AS MedicationName,
    m.Status,
    m.Cost,
    d.Name AS PrescribingDoctor
FROM Medication m
JOIN Patient p ON m.PatientID = p.PatientID
JOIN Doctor d ON m.DoctorID = d.DoctorID
ORDER BY p.PatientID, m.Status;

-- =========================
-- 4. STORED PROCEDURE (NEW)
-- =========================

DELIMITER $$
CREATE PROCEDURE schedule_appointment(
    IN p_patient INT,
    IN p_doctor INT,
    IN p_hospital INT,
    IN p_date DATE,
    IN p_time TIME,
    IN p_reason VARCHAR(200),
    IN p_cost DECIMAL(8,2)
)
BEGIN
    INSERT INTO Appointment 
        (PatientID, DoctorID, HospitalID, ApptDate, ApptTime, VisitReason, Cost)
    VALUES 
        (p_patient, p_doctor, p_hospital, p_date, p_time, p_reason, p_cost);
END$$
DELIMITER ;

-- =========================
-- 5. INDEX
-- =========================

CREATE INDEX idx_doctorname_discipline
ON Doctor (Name, Discipline);

-- =========================
-- 6. SAMPLE DATA
-- =========================

INSERT INTO InsurancePlan (Provider, Coverage)
VALUES
('Blue Shield', 'Comprehensive coverage including dental and vision'),
('Kaiser Permanente', 'Basic medical and emergency coverage'),
('Aetna', 'Premium plan with full hospitalization and medication coverage'),
('UnitedHealth', 'Standard coverage with partial prescription benefits');

INSERT INTO Hospital (Name, Address, PhoneNumber)
VALUES
('Sunnyvale General Hospital', '123 Main St, Sunnyvale, CA', '408-555-1010'),
('Bay Area Medical Center', '456 Elm St, San Jose, CA', '408-555-2020'),
('Santa Clara Health Clinic', '789 Oak St, Santa Clara, CA', '408-555-3030');

INSERT INTO Doctor (Name, Discipline, Email, PhoneNumber)
VALUES
('Dr. Sarah Lee', 'Cardiology', 'slee@sunnyvalehealth.org', '408-111-2222'),
('Dr. David Kim', 'Pediatrics', 'dkim@bayareahealth.org', '408-333-4444'),
('Dr. Emily Johnson', 'Dermatology', 'ejohnson@santaclarahealth.org', '408-555-6666'),
('Dr. Robert Smith', 'Orthopedics', 'rsmith@sunnyvalehealth.org', '408-777-8888');

INSERT INTO Patient (Name, Birthdate, Email, PhoneNumber, Address, PlanID)
VALUES
('Alice Nguyen', '1995-06-15', 'alice.nguyen@email.com', '408-900-1111', '100 Apple Way, Sunnyvale, CA', 1),
('Brian Chen', '1988-03-22', 'brian.chen@email.com', '408-900-2222', '200 Banana Blvd, San Jose, CA', 2),
('Carlos Garcia', '1979-11-02', 'carlos.g@email.com', '408-900-3333', '300 Cherry Ct, Santa Clara, CA', 3),
('Diana Patel', '2000-01-30', 'dpatel@email.com', '408-900-4444', '400 Date Dr, Cupertino, CA', NULL);

INSERT INTO DoctorHospital (HospitalID, DoctorID)
VALUES
(1, 1),
(1, 4),
(2, 2),
(3, 3),
(2, 1);

INSERT INTO Medication (PatientID, DoctorID, Name, Cost, Status, Dosage, Frequency, PlanID)
VALUES
(1, 1, 'Atorvastatin', 120.00, 'Ongoing',   '10mg',            'Once daily',    1),
(2, 2, 'Amoxicillin',   45.00, 'Completed', '500mg',           'Twice daily',   2),
(3, 3, 'Hydrocortisone Cream', 25.50, 'Paused',    'Apply thin layer', 'Twice daily',   3),
(4, 4, 'Ibuprofen',     15.00, 'Ongoing',   '200mg',           'Every 6 hours', NULL);

INSERT INTO Appointment (PatientID, DoctorID, HospitalID, ApptDate, ApptTime, VisitReason, Cost, PlanID)
VALUES
(1, 1, 1, '2025-11-10', '09:00:00', 'Routine heart check-up', 200.00, 1),
(2, 2, 2, '2025-11-11', '10:30:00', 'Flu symptoms',           120.00, 2),
(3, 3, 3, '2025-11-12', '13:00:00', 'Skin rash evaluation',    90.00, 3),
(4, 4, 1, '2025-11-13', '15:00:00', 'Knee pain consultation', 150.00, NULL);
