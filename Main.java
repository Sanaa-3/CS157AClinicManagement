import java.sql.*;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(true);
            System.out.println("Connected to DB.");

            boolean running = true;
            while (running) {
                printMenu();
                int choice = readInt("Choose an option: ");

                switch (choice) {
                    case 1 -> viewPatients(conn);                    // SELECT
                    case 2 -> viewDoctors(conn);                     // SELECT
                    case 3 -> viewPatientMedicationView(conn);       // VIEW
                    case 4 -> insertMedication(conn);                // INSERT
                    case 5 -> scheduleAppointmentWithProc(conn);     // Stored Procedure
                    case 6 -> transactionAppointmentPlusMedication(conn); // COMMIT + ROLLBACK
                    case 7 -> updatePatient(conn);                   // UPDATE
                    case 8 -> deletePatient(conn);                   // DELETE
                    case 0 -> {
                        System.out.println("Exiting...");
                        running = false;
                    }
                    default -> System.out.println("Invalid choice, try again.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Fatal DB error: " + e.getMessage());
        }
    }

    private static void printMenu() {
        System.out.println("\n==== Clinical Management System (G4) ====");
        System.out.println("1. Select All Patients");
        System.out.println("2. Select All Doctors");
        System.out.println("3. Select All From Patient Medications View");
        System.out.println("4. Insert Medication");
        System.out.println("5. Schedule Appointment (Stored Procedure)");
        System.out.println("6. Transaction: Appointment + Medication (COMMIT/ROLLBACK)");
        System.out.println("7. Update Patient");
        System.out.println("8. Delete Patient");
        System.out.println("0. Exit");
    }

    // Input validation helpers

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    private static double readPositiveDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                double value = Double.parseDouble(line);
                if (value < 0) {
                    System.out.println("Value must be non-negative.");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("Input cannot be empty.");
        }
    }

    // Selects

    private static void viewPatients(Connection conn) {
        String sql = "SELECT PatientID, Name, Birthdate, Email, PhoneNumber, Address, PlanID FROM Patient";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n--- Patients ---");
            while (rs.next()) {
                System.out.printf("ID: %d | Name: %s | DOB: %s | Email: %s | Phone: %s | Address: %s | PlanID: %s%n",
                        rs.getInt("PatientID"),
                        rs.getString("Name"),
                        rs.getDate("Birthdate"),
                        rs.getString("Email"),
                        rs.getString("PhoneNumber"),
                        rs.getString("Address"),
                        rs.getString("PlanID"));
            }
        } catch (SQLException e) {
            System.err.println("Error viewing patients: " + e.getMessage());
        }
    }

    private static void viewDoctors(Connection conn) {
        String sql = "SELECT DoctorID, Name, Discipline, Email, PhoneNumber FROM Doctor";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n--- Doctors ---");
            while (rs.next()) {
                System.out.printf("ID: %d | Name: %s | Discipline: %s | Email: %s | Phone: %s%n",
                        rs.getInt("DoctorID"),
                        rs.getString("Name"),
                        rs.getString("Discipline"),
                        rs.getString("Email"),
                        rs.getString("PhoneNumber"));
            }
        } catch (SQLException e) {
            System.err.println("Error viewing doctors: " + e.getMessage());
        }
    }

    private static void viewPatientMedicationView(Connection conn) {
        String sql = "SELECT * FROM PatientMedicationView";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n--- Patient Medications (from VIEW) ---");
            while (rs.next()) {
                System.out.printf(
                    "Patient #%d (%s) | Medication #%d (%s) | Status: %s | Cost: %.2f | Doctor: %s%n",
                    rs.getInt("PatientID"),
                    rs.getString("PatientName"),
                    rs.getInt("MedicationID"),
                    rs.getString("MedicationName"),
                    rs.getString("Status"),
                    rs.getDouble("Cost"),
                    rs.getString("PrescribingDoctor")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error viewing patient medications: " + e.getMessage());
        }
    }

    // Insert Medication

    private static void insertMedication(Connection conn) {
        System.out.println("\n--- Insert Medication ---");
        int patientId = readInt("PatientID: ");
        int doctorId  = readInt("DoctorID: ");
        String name   = readNonEmpty("Medication name: ");
        double cost   = readPositiveDouble("Cost (0 - 1000): ");
        String status = readNonEmpty("Status (Paused/Completed/Ongoing): ");
        String dosage = readNonEmpty("Dosage (e.g., '10mg'): ");
        String freq   = readNonEmpty("Frequency (e.g., 'Once daily'): ");
        System.out.print("PlanID (optional, press Enter for NULL): ");
        String planIdStr = scanner.nextLine().trim();

        Integer planId = null;
        if (!planIdStr.isEmpty()) {
            try {
                planId = Integer.parseInt(planIdStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid PlanID, using NULL.");
            }
        }

        String sql = "INSERT INTO Medication (PatientID, DoctorID, Name, Cost, Status, Dosage, Frequency, PlanID) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setInt(2, doctorId);
            ps.setString(3, name);
            ps.setDouble(4, cost);
            ps.setString(5, status);
            ps.setString(6, dosage);
            ps.setString(7, freq);
            if (planId == null) {
                ps.setNull(8, Types.INTEGER);
            } else {
                ps.setInt(8, planId);
            }

            int rows = ps.executeUpdate();
            System.out.println("Inserted " + rows + " medication record(s).");
        } catch (SQLException e) {
            System.err.println("Error inserting medication: " + e.getMessage());
            if ("23000".equals(e.getSQLState())) {
                System.out.println("Constraint violation (FK/CHECK). " +
                        "Check patient/doctor IDs, cost range, and status value.");
            }
        }
    }

    // Stored Procedure Demo

    private static void scheduleAppointmentWithProc(Connection conn) {
        System.out.println("\n--- Schedule Appointment via Stored Procedure ---");
        int patientId  = readInt("PatientID: ");
        int doctorId   = readInt("DoctorID: ");
        int hospitalId = readInt("HospitalID: ");
        String date    = readNonEmpty("Date (YYYY-MM-DD): ");
        String time    = readNonEmpty("Time (HH:MM:SS): ");
        String reason  = readNonEmpty("Visit reason: ");
        double cost    = readPositiveDouble("Cost: ");

        String call = "{ CALL schedule_appointment(?, ?, ?, ?, ?, ?, ?) }";

        try (CallableStatement cs = conn.prepareCall(call)) {
            cs.setInt(1, patientId);
            cs.setInt(2, doctorId);
            cs.setInt(3, hospitalId);
            cs.setString(4, date);
            cs.setString(5, time);
            cs.setString(6, reason);
            cs.setDouble(7, cost);

            cs.execute();
            System.out.println("Appointment scheduled successfully.");
        } catch (SQLException e) {
            System.err.println("Error scheduling appointment: " + e.getMessage());
            if ("23000".equals(e.getSQLState())) {
                System.out.println("Constraint violation (e.g., double-booked doctor or invalid IDs).");
            }
        }
    }

    // Transactional Workflow

    private static void transactionAppointmentPlusMedication(Connection conn) {
        System.out.println("\n--- Transaction: Appointment + Medication ---");

        try {
            conn.setAutoCommit(false); // start transaction

            int patientId  = readInt("PatientID: ");
            int doctorId   = readInt("DoctorID: ");
            int hospitalId = readInt("HospitalID: ");
            String date    = readNonEmpty("Date (YYYY-MM-DD): ");
            String time    = readNonEmpty("Time (HH:MM:SS): ");
            String reason  = readNonEmpty("Visit reason: ");
            double cost    = readPositiveDouble("Appointment cost: ");

            String call = "{ CALL schedule_appointment(?, ?, ?, ?, ?, ?, ?) }";
            try (CallableStatement cs = conn.prepareCall(call)) {
                cs.setInt(1, patientId);
                cs.setInt(2, doctorId);
                cs.setInt(3, hospitalId);
                cs.setString(4, date);
                cs.setString(5, time);
                cs.setString(6, reason);
                cs.setDouble(7, cost);
                cs.execute();
            }

            System.out.println("\nNow add a medication for this visit.");
            String medName  = readNonEmpty("Medication name: ");
            double medCost  = readPositiveDouble("Medication cost (0 - 1000): ");
            String status   = readNonEmpty("Status (Paused/Completed/Ongoing): ");
            String dosage   = readNonEmpty("Dosage: ");
            String freq     = readNonEmpty("Frequency: ");
            System.out.print("PlanID for medication (optional, Enter for NULL): ");
            String medPlanStr = scanner.nextLine().trim();

            Integer medPlanId = null;
            if (!medPlanStr.isEmpty()) {
                try {
                    medPlanId = Integer.parseInt(medPlanStr);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid PlanID, using NULL.");
                }
            }

            String medSql = "INSERT INTO Medication " +
                    "(PatientID, DoctorID, Name, Cost, Status, Dosage, Frequency, PlanID) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(medSql)) {
                ps.setInt(1, patientId);
                ps.setInt(2, doctorId);
                ps.setString(3, medName);
                ps.setDouble(4, medCost);
                ps.setString(5, status);
                ps.setString(6, dosage);
                ps.setString(7, freq);
                if (medPlanId == null) {
                    ps.setNull(8, Types.INTEGER);
                } else {
                    ps.setInt(8, medPlanId);
                }
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            System.err.println("Transaction error: " + e.getMessage());
            try {
                conn.rollback();
                System.out.println("Rolled back due to error.");
            } catch (SQLException ex) {
                System.err.println("Rollback failed: " + ex.getMessage());
            }
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Failed to reset autocommit: " + e.getMessage());
            }
        }
    }

    // Update and Delete for Patient

    private static void updatePatient(Connection conn) {
        System.out.println("\n--- Update Patient ---");
        int id = readInt("Enter PatientID to update: ");

        String newAddress = readNonEmpty("New address: ");
        String newPhone   = readNonEmpty("New phone: ");

        String sql = "UPDATE Patient SET Address = ?, PhoneNumber = ? WHERE PatientID = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newAddress);
            ps.setString(2, newPhone);
            ps.setInt(3, id);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                System.out.println("No patient found with that ID.");
            } else {
                System.out.println("Updated " + rows + " patient(s).");
            }
        } catch (SQLException e) {
            System.err.println("Error updating patient: " + e.getMessage());
        }
    }

    private static void deletePatient(Connection conn) {
        System.out.println("\n--- Delete Patient ---");
        int id = readInt("Enter PatientID to delete: ");

        String sql = "DELETE FROM Patient WHERE PatientID = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                System.out.println("No patient found with that ID.");
            } else {
                System.out.println("Deleted " + rows + " patient(s).");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting patient: " + e.getMessage());
            if ("23000".equals(e.getSQLState())) {
                System.out.println("Cannot delete: this patient is referenced by other records (e.g., Medication/Appointment).");
            }
        }
    }
}
