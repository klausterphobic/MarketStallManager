package model;

public abstract class Stall implements Storable {
    public static final String AVAILABLE = "AVAILABLE";
    public static final String OCCUPIED = "OCCUPIED";

    private String id;
    private String section;
    private String size;
    private String category;
    private String status;
    private String vendorId;
    private String billingType;
    private double rate;

    protected Stall(String id, String section, String size, String category,
                    String status, String vendorId, String billingType, double rate) {
        this.id = id;
        this.section = section;
        this.size = size;
        this.category = category;
        this.status = status;
        this.vendorId = vendorId;
        this.billingType = billingType;
        this.rate = rate;
    }

    public String getId() { return id; }
    public String getSection() { return section; }
    public void setSection(String section) {
        if (section == null || section.isBlank()) throw new IllegalArgumentException("Section is required.");
        this.section = section.trim();
    }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public String getCategory() { return category; }
    public String getStatus() { return status; }
    public void setStatus(String status) {
        if (!AVAILABLE.equals(status) && !OCCUPIED.equals(status)) {
            throw new IllegalArgumentException("Invalid stall status.");
        }
        this.status = status;
    }
    public String getVendorId() { return vendorId; }
    public void setVendorId(String vendorId) { this.vendorId = vendorId; }
    public String getBillingType() { return billingType; }
    public void setBillingType(String billingType) {
        if (!"DAILY".equals(billingType) && !"MONTHLY".equals(billingType)) {
            throw new IllegalArgumentException("Billing type must be DAILY or MONTHLY.");
        }
        this.billingType = billingType;
    }
    public double getRate() { return rate; }
    public void setRate(double rate) {
        if (rate < 0) throw new IllegalArgumentException("Rate cannot be negative.");
        this.rate = rate;
    }

    public boolean isOccupied() { return OCCUPIED.equalsIgnoreCase(status); }

    public abstract double computeRent();
    public abstract String getStallType();

    @Override
    public String toCsv() {
        return String.join(",",
                id, section, size, category, status,
                vendorId == null ? "" : vendorId,
                billingType, String.valueOf(rate));
    }

    public static Stall fromCsv(String line) {
        String[] p = line.split(",", -1);
        if (p.length < 8) return null;
        double rate = 0;
        try { rate = Double.parseDouble(p[7]); } catch (NumberFormatException ignored) { }
        String vendorId = p[5].isBlank() ? null : p[5];
        return switch (p[3].toUpperCase()) {
            case "WET" -> new WetStall(p[0], p[1], p[2], p[3], p[4], vendorId, p[6], rate);
            case "FOOD" -> new FoodStall(p[0], p[1], p[2], p[3], p[4], vendorId, p[6], rate);
            default -> new DryGoodsStall(p[0], p[1], p[2], p[3], p[4], vendorId, p[6], rate);
        };
    }

    @Override
    public String toString() {
        return id + " (" + section + ", " + getStallType() + ")";
    }
}