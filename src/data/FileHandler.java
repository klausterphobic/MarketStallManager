package data;

import exception.DataFileException;
import model.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {
    public static final String DATA_DIR = "data";
    public static final String RECEIPTS_DIR = "receipts";
    private static final String USERS = "users.csv";
    private static final String VENDORS = "vendors.csv";
    private static final String STALLS = "stalls.csv";
    private static final String PAYMENTS = "payments.csv";
    private static final String PERMITS = "permits.csv";

    public FileHandler() throws DataFileException {
        ensureFiles();
    }

    private void ensureFiles() throws DataFileException {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            Files.createDirectories(Paths.get(RECEIPTS_DIR));
            createIfMissing(USERS, seedUsers());
            createIfMissing(VENDORS, seedVendors());
            createIfMissing(STALLS, seedStalls());
            createIfMissing(PAYMENTS, seedPayments());
            createIfMissing(PERMITS, seedPermits());
        } catch (IOException e) {
            throw new DataFileException("Could not create data files: " + e.getMessage(), e);
        }
    }

    private void createIfMissing(String name, String content) throws IOException {
        Path p = Paths.get(DATA_DIR, name);
        if (!Files.exists(p)) {
            Files.writeString(p, content, StandardCharsets.UTF_8);
        }
    }

    private String seedUsers() {
        return "U1,admin," + User.hash("admin123") + ",ADMIN,Market Administrator\n"
             + "U2,staff," + User.hash("staff123") + ",STAFF,Market Staff\n";
    }

    private String seedVendors() {
        return "V1,Maria Santos,09171234567,Fresh Vegetables,2024-01-15,S1\n"
             + "V2,Jose Cruz,09181234567,Dried Fish,2024-02-10,S2\n"
             + "V3,Ana Reyes,09191234567,Cooked Food,2024-03-05,S3\n";
    }

    private String seedStalls() {
        return "S1,Section A,Small,DRY,OCCUPIED,V1,MONTHLY,1500.0\n"
             + "S2,Section B,Medium,WET,OCCUPIED,V2,MONTHLY,1800.0\n"
             + "S3,Section C,Medium,FOOD,OCCUPIED,V3,DAILY,300.0\n"
             + "S4,Section A,Small,DRY,AVAILABLE,,MONTHLY,1500.0\n";
    }

    private String seedPayments() {
        LocalDate t = LocalDate.now();
        return "P1,V1,1500.0," + t.minusDays(45) + ",Monthly rent," + t.minusDays(15) + "\n"
             + "P2,V2,1800.0," + t.minusDays(10) + ",Monthly rent," + t.plusDays(20) + "\n"
             + "P3,V3,300.0," + t.minusDays(5) + ",Daily rent," + t.minusDays(3) + "\n";
    }

    private String seedPermits() {
        LocalDate t = LocalDate.now();
        return "PM1,V1,SANITARY,SP-1001," + t.minusDays(200) + "," + t.plusDays(10) + "\n"
             + "PM2,V1,BUSINESS,BP-2001," + t.minusDays(300) + "," + t.plusDays(200) + "\n"
             + "PM3,V2,SANITARY,SP-1002," + t.minusDays(400) + "," + t.minusDays(20) + "\n"
             + "PM4,V2,BUSINESS,BP-2002," + t.minusDays(100) + "," + t.plusDays(60) + "\n"
             + "PM5,V3,SANITARY,SP-1003," + t.minusDays(50) + "," + t.plusDays(25) + "\n"
             + "PM6,V3,BUSINESS,BP-2003," + t.minusDays(30) + "," + t.plusDays(150) + "\n";
    }

    private List<String> readLines(String fileName) throws DataFileException {
        try {
            Path p = Paths.get(DATA_DIR, fileName);
            if (!Files.exists(p)) return new ArrayList<>();
            List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
            List<String> clean = new ArrayList<>();
            for (String line : lines) {
                if (!line.isBlank()) clean.add(line);
            }
            return clean;
        } catch (IOException e) {
            throw new DataFileException("Could not read " + fileName + ": " + e.getMessage(), e);
        }
    }

    private void writeLines(String fileName, List<String> lines) throws DataFileException {
        try {
            Files.write(Paths.get(DATA_DIR, fileName), lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DataFileException("Could not save " + fileName + ": " + e.getMessage(), e);
        }
    }

    public List<User> loadUsers() throws DataFileException {
        List<User> list = new ArrayList<>();
        for (String line : readLines(USERS)) {
            String[] p = line.split(",", -1);
            if (p.length < 5) continue;
            if ("ADMIN".equalsIgnoreCase(p[3])) {
                list.add(new Admin(p[0], p[1], p[2], p[4]));
            } else {
                list.add(new Staff(p[0], p[1], p[2], p[4]));
            }
        }
        return list;
    }

    public void saveUsers(List<User> users) throws DataFileException {
        List<String> lines = new ArrayList<>();
        for (User u : users) lines.add(u.toCsv());
        writeLines(USERS, lines);
    }

    public List<Vendor> loadVendors() throws DataFileException {
        List<Vendor> list = new ArrayList<>();
        for (String line : readLines(VENDORS)) {
            Vendor v = Vendor.fromCsv(line);
            if (v != null) list.add(v);
        }
        return list;
    }

    public void saveVendors(List<Vendor> vendors) throws DataFileException {
        List<String> lines = new ArrayList<>();
        for (Vendor v : vendors) lines.add(v.toCsv());
        writeLines(VENDORS, lines);
    }

    public List<Stall> loadStalls() throws DataFileException {
        List<Stall> list = new ArrayList<>();
        for (String line : readLines(STALLS)) {
            Stall s = Stall.fromCsv(line);
            if (s != null) list.add(s);
        }
        return list;
    }

    public void saveStalls(List<Stall> stalls) throws DataFileException {
        List<String> lines = new ArrayList<>();
        for (Stall s : stalls) lines.add(s.toCsv());
        writeLines(STALLS, lines);
    }

    public List<Payment> loadPayments() throws DataFileException {
        List<Payment> list = new ArrayList<>();
        for (String line : readLines(PAYMENTS)) {
            Payment p = Payment.fromCsv(line);
            if (p != null) list.add(p);
        }
        return list;
    }

    public void savePayments(List<Payment> payments) throws DataFileException {
        List<String> lines = new ArrayList<>();
        for (Payment p : payments) lines.add(p.toCsv());
        writeLines(PAYMENTS, lines);
    }

    public List<Permit> loadPermits() throws DataFileException {
        List<Permit> list = new ArrayList<>();
        for (String line : readLines(PERMITS)) {
            Permit p = Permit.fromCsv(line);
            if (p != null) list.add(p);
        }
        return list;
    }

    public void savePermits(List<Permit> permits) throws DataFileException {
        List<String> lines = new ArrayList<>();
        for (Permit p : permits) lines.add(p.toCsv());
        writeLines(PERMITS, lines);
    }

    public String saveReceipt(String content, String fileName) throws DataFileException {
        try {
            Files.createDirectories(Paths.get(RECEIPTS_DIR));
            Path p = Paths.get(RECEIPTS_DIR, fileName);
            Files.writeString(p, content, StandardCharsets.UTF_8);
            return p.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new DataFileException("Could not save receipt: " + e.getMessage(), e);
        }
    }

    public void exportCsv(String filePath, List<String> lines) throws DataFileException {
        try {
            Files.write(Paths.get(filePath), lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DataFileException("Could not export CSV: " + e.getMessage(), e);
        }
    }
}