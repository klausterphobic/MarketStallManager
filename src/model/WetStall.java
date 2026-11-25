package model;

public class WetStall extends Stall {
    public WetStall(String id, String section, String size, String category,
                    String status, String vendorId, String billingType, double rate) {
        super(id, section, size, category, status, vendorId, billingType, rate);
    }

    @Override
    public double computeRent() { return getRate() * 1.10; }

    @Override
    public String getStallType() { return "Wet"; }
}