package model;

import java.time.LocalDate;

public class Payment implements Storable {
    private String id;
    private String vendorId;
    private double amount;
    private LocalDate paymentDate;
    private String periodCovered;
    private LocalDate paidUntil;

    public Payment(String id, String vendorId, double amount, LocalDate paymentDate,
                   String periodCovered, LocalDate paidUntil) {
        this.id = id;
        this.vendorId = vendorId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.periodCovered = periodCovered;
        this.paidUntil = paidUntil;
    }

    public String getId() { return id; }
    public String getVendorId() { return vendorId; }
    public double getAmount() { return amount; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public String getPeriodCovered() { return periodCovered; }
    public LocalDate getPaidUntil() { return paidUntil; }

    @Override
    public String toCsv() {
        return String.join(",", id, vendorId, String.valueOf(amount),
                paymentDate.toString(), periodCovered, paidUntil.toString());
    }

    public static Payment fromCsv(String line) {
        String[] p = line.split(",", -1);
        if (p.length < 6) return null;
        try {
            return new Payment(p[0], p[1], Double.parseDouble(p[2]),
                    LocalDate.parse(p[3]), p[4], LocalDate.parse(p[5]));
        } catch (Exception e) {
            return null;
        }
    }
}