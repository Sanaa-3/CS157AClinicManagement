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

ALTER TABLE Appointment
ADD CONSTRAINT chk_future_date
    CHECK (ApptDate >= CURDATE());
