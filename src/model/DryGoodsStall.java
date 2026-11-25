package model;

public class DryGoodsStall extends Stall {
    public DryGoodsStall(String id, String section, String size, String category,
                         String status, String vendorId, String billingType, double rate) {
        super(id, section, size, category, status, vendorId, billingType, rate);
    }

    @Override
    public double computeRent() { return getRate(); }

    @Override
    public String getStallType() { return "Dry Goods"; }
}