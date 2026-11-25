package model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public abstract class Permit implements Storable {
    public static final String VALID = "VALID";
    public static final String EXPIRING = "EXPIRING";
    public static final String EXPIRED = "EXPIRED";

    private String id;
    private String vendorId;
    private String permitNumber;
    private LocalDate issueDate;
    private LocalDate expiryDate;

    protected Permit(String id, String vendorId, String permitNumber,
                     LocalDate issueDate, LocalDate expiryDate) {
        this.id = id;
        this.vendorId = vendorId;
        this.permitNumber = permitNumber;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
    }

    public String getId() { return id; }
    public String getVendorId() { return vendorId; }
    public String getPermitNumber() { return permitNumber; }
    public void setPermitNumber(String permitNumber) { this.permitNumber = permitNumber; }
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public abstract String getPermitType();

    public String getStatus() {
        if (expiryDate == null) return EXPIRED;
        LocalDate today = LocalDate.now();
        if (expiryDate.isBefore(today)) return EXPIRED;
        long days = ChronoUnit.DAYS.between(today, expiryDate);
        return days <= 30 ? EXPIRING : VALID;
    }

    public long getDaysUntilExpiry() {
        if (expiryDate == null) return -9999;
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    @Override
    public String toCsv() {
        return String.join(",", id, vendorId, getPermitType(), permitNumber,
                issueDate.toString(), expiryDate.toString());
    }

    public static Permit fromCsv(String line) {
        String[] p = line.split(",", -1);
        if (p.length < 6) return null;
        try {
            LocalDate issue = LocalDate.parse(p[4]);
            LocalDate expiry = LocalDate.parse(p[5]);
            if ("SANITARY".equalsIgnoreCase(p[2])) {
                return new SanitaryPermit(p[0], p[1], p[3], issue, expiry);
            } else {
                return new BusinessPermit(p[0], p[1], p[3], issue, expiry);
            }
        } catch (Exception e) {
            return null;
        }
    }
}