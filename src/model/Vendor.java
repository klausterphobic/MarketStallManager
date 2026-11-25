package model;

import java.time.LocalDate;

public class Vendor implements Storable {
    private String id;
    private String name;
    private String contact;
    private String business;
    private LocalDate dateRegistered;
    private String stallId;
    private LocalDate paidUntil;

    public Vendor(String id, String name, String contact, String business,
                  LocalDate dateRegistered, String stallId) {
        this.id = id;
        this.name = name;
        this.contact = contact;
        this.business = business;
        this.dateRegistered = dateRegistered;
        this.stallId = stallId;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Vendor name is required.");
        this.name = name.trim();
    }
    public String getContact() { return contact; }
    public void setContact(String contact) {
        if (contact == null || !contact.trim().matches("^09\\d{9}$")) {
            throw new IllegalArgumentException("Contact must be 11 digits starting with 09.");
        }
        this.contact = contact.trim();
    }
    public String getBusiness() { return business; }
    public void setBusiness(String business) {
        if (business == null || business.isBlank()) throw new IllegalArgumentException("Business is required.");
        this.business = business.trim();
    }
    public LocalDate getDateRegistered() { return dateRegistered; }
    public void setDateRegistered(LocalDate dateRegistered) { this.dateRegistered = dateRegistered; }
    public String getStallId() { return stallId; }
    public void setStallId(String stallId) { this.stallId = stallId; }
    public LocalDate getPaidUntil() { return paidUntil; }
    public void setPaidUntil(LocalDate paidUntil) { this.paidUntil = paidUntil; }

    @Override
    public String toCsv() {
        return String.join(",", id, name, contact, business,
                dateRegistered == null ? "" : dateRegistered.toString(),
                stallId == null ? "" : stallId);
    }

    public static Vendor fromCsv(String line) {
        String[] p = line.split(",", -1);
        if (p.length < 6) return null;
        LocalDate reg = null;
        try {
            if (!p[4].isBlank()) reg = LocalDate.parse(p[4]);
        } catch (Exception ignored) { }
        String stallId = p[5].isBlank() ? null : p[5];
        return new Vendor(p[0], p[1], p[2], p[3], reg, stallId);
    }

    @Override
    public String toString() {
        return id + " - " + name;
    }
}