import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import java.util.Scanner;

public class Main {

    // ===== JDBC CONFIG / DBUtil MERGED =====
    private static final String PROPERTIES_FILE = "app.properties";
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(true);
            System.out.println("Connected to DB.");

            boolean running = true;
            while (running) {
                printMenu();
                int choice = readInt("Choose an option: ");

                switch (choice) {
                    case 1 -> viewPatients(conn);                     // SELECT (Patient)
                    case 2 -> viewDoctors(conn);                      // SELECT (Doctor)
                    case 3 -> viewHospitals(conn);                    // SELECT (Hospital) - 3rd key table
                    case 4 -> viewPatientMedicationView(conn);        // VIEW
                    case 5 -> insertMedication(conn);                 // INSERT
                    case 6 -> updatePatient(conn);                    // UPDATE
                    case 7 -> deletePatient(conn);                    // DELETE
                    case 8 -> scheduleAppointmentWithProc(conn);      // Stored Procedure
                    case 9 -> transactionTransferDoctorHospital(conn);// Transaction (COMMIT + ROLLBACK)
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

    /**
     * Loads app.properties, loads MySQL JDBC driver, and opens a Connection.
     * This covers Step 1: JDBC Setup and Connection Test.
     */
    private static Connection getConnection() throws SQLException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(PROPERTIES_FILE)) {
            props.load(fis);
        } catch (IOException e) {
            System.err.println("Error loading " + PROPERTIES_FILE + ": " + e.getMessage());
            throw new SQLException("Unable to load DB config", e);
        }

        String url = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String password = props.getProperty("db.password");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC Driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("Could not load MySQL driver: " + e.getMessage());
        }

        return DriverManager.getConnection(url, user, password);
    }

    // ===== MENU (Step 2 order: view -> insert -> update -> delete -> transaction) =====

    private static void printMenu() {
        System.out.println("\n==== Clinical Management System (G4) ====");
        System.out.println("1. View Patients");
        System.out.println("2. View Doctors");
        System.out.println("3. View Hospitals");
        System.out.println("4. View Patient Medications (VIEW)");
        System.out.println("5. Insert Medication");
        System.out.println("6. Update Patient");
        System.out.println("7. Delete Patient");
        System.out.println("8. Schedule Appointment (Stored Procedure)");
        System.out.println("9. Transaction: Transfer Doctor to New Hospital (COMMIT/ROLLBACK)");
        System.out.println("0. Exit");
    }

    // ===== Input validation helpers (Step 5) =====

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

    // ===== SELECTs for 3 key tables: Patient, Doctor, Hospital (Step 2/3) =====

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

    // Hospital SELECT to satisfy "3 key tables each have SELECT"
    private static void viewHospitals(Connection conn) {
        String sql = "SELECT HospitalID, Name, Address, PhoneNumber FROM Hospital";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n--- Hospitals ---");
            while (rs.next()) {
                System.out.printf("ID: %d | Name: %s | Address: %s | Phone: %s%n",
                        rs.getInt("HospitalID"),
                        rs.getString("Name"),
                        rs.getString("Address"),
                        rs.getString("PhoneNumber"));
            }
        } catch (SQLException e) {
            System.err.println("Error viewing hospitals: " + e.getMessage());
        }
    }

    // ===== VIEW (Step 6: one VIEW) =====

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

    // ===== INSERT (PreparedStatement, Step 3) =====

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

    // ===== UPDATE & DELETE (PreparedStatement, Step 3) =====

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

    // ===== Stored Procedure demo (Step 6: at least one procedure) =====

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

    // ===== Transactional workflow: move doctor to a different hospital (Step 4) =====
    /**
     * Transaction:
     *  - Verifies doctor exists.
     *  - Verifies current and new hospitals exist.
     *  - Verifies a DoctorHospital link exists for (doctor, currentHospital).
     *  - Updates DoctorHospital to point to new Hospital.
     *  - Updates Appointment rows to use new HospitalID for that doctor.
     *  - Uses COMMIT on success; ROLLBACK if anything fails or is invalid.
     */
    private static void transactionTransferDoctorHospital(Connection conn) {
        System.out.println("\n--- Transaction: Transfer Doctor to New Hospital ---");

        try {
            conn.setAutoCommit(false); // start transaction

            int doctorId          = readInt("DoctorID to transfer: ");
            int currentHospitalId = readInt("Current HospitalID: ");
            int newHospitalId     = readInt("New HospitalID: ");

            // Existence checks (these are all still inside the transaction)
            if (!doctorExists(conn, doctorId)) {
                System.out.println("No doctor found with that ID. Rolling back.");
                conn.rollback();
                conn.setAutoCommit(true);
                return;
            }
            if (!hospitalExists(conn, currentHospitalId)) {
                System.out.println("No hospital found with CURRENT HospitalID. Rolling back.");
                conn.rollback();
                conn.setAutoCommit(true);
                return;
            }
            if (!hospitalExists(conn, newHospitalId)) {
                System.out.println("No hospital found with NEW HospitalID. Rolling back.");
                conn.rollback();
                conn.setAutoCommit(true);
                return;
            }
            if (!doctorHospitalLinkExists(conn, doctorId, currentHospitalId)) {
                System.out.println("Doctor is not currently assigned to that hospital in DoctorHospital. Rolling back.");
                conn.rollback();
                conn.setAutoCommit(true);
                return;
            }

            // 1) Update DoctorHospital mapping
            String updateDH = "UPDATE DoctorHospital SET HospitalID = ? " +
                              "WHERE DoctorID = ? AND HospitalID = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateDH)) {
                ps.setInt(1, newHospitalId);
                ps.setInt(2, doctorId);
                ps.setInt(3, currentHospitalId);
                int rows = ps.executeUpdate();
                System.out.println("Updated DoctorHospital rows: " + rows);
            }

            // 2) Update Appointment records to reflect new hospital
            String updateAppt = "UPDATE Appointment " +
                                "SET HospitalID = ? " +
                                "WHERE DoctorID = ? AND HospitalID = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateAppt)) {
                ps.setInt(1, newHospitalId);
                ps.setInt(2, doctorId);
                ps.setInt(3, currentHospitalId);
                int rows = ps.executeUpdate();
                System.out.println("Updated Appointment rows: " + rows);
            }

            // Optional: simulate failure to demo rollback explicitly
            int simulate = readInt("Simulate failure and ROLLBACK? (1 = yes, 0 = no): ");
            if (simulate == 1) {
                System.out.println("Simulated error. Rolling back transaction.");
                conn.rollback();
            } else {
                conn.commit();
                System.out.println("Doctor transfer committed successfully.");
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

    // ===== Helper existence checks for transaction =====

    private static boolean doctorExists(Connection conn, int doctorId) throws SQLException {
        String sql = "SELECT 1 FROM Doctor WHERE DoctorID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static boolean hospitalExists(Connection conn, int hospitalId) throws SQLException {
        String sql = "SELECT 1 FROM Hospital WHERE HospitalID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hospitalId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static boolean doctorHospitalLinkExists(Connection conn, int doctorId, int hospitalId) throws SQLException {
        String sql = "SELECT 1 FROM DoctorHospital WHERE DoctorID = ? AND HospitalID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setInt(2, hospitalId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
