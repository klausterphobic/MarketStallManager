package console;

import exception.DataFileException;
import exception.InvalidInputException;
import model.Payment;
import model.Stall;
import model.User;
import model.Vendor;
import service.MarketManager;
import service.Receipt;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PaymentMenu {
    private final MarketManager manager;
    private final User user;

    public PaymentMenu(MarketManager manager, User user) {
        this.manager = manager;
        this.user = user;
    }

    public void run() {
        while (true) {
            ConsoleUtils.header("PAYMENTS");
            listPayments(manager.getPayments());
            System.out.println();
            System.out.println("  [1] Record payment");
            System.out.println("  [2] View receipt");
            if (user.canDelete()) System.out.println("  [3] Delete payment");
            System.out.println("  [0] Back");
            int choice = ConsoleUtils.readInt("\n  Choose: ", 0, user.canDelete() ? 3 : 2);
            switch (choice) {
                case 0 -> { return; }
                case 1 -> record();
                case 2 -> view();
                case 3 -> { if (user.canDelete()) delete(); }
            }
        }
    }

    private void listPayments(List<Payment> payments) {
        List<String[]> rows = new ArrayList<>();
        for (Payment p : payments) {
            Vendor v = manager.findVendor(p.getVendorId());
            rows.add(new String[]{
                    p.getId(), v == null ? p.getVendorId() : v.getName(),
                    ConsoleUtils.money(p.getAmount()),
                    p.getPaymentDate().toString(),
                    p.getPeriodCovered(), p.getPaidUntil().toString()
            });
        }
        ConsoleUtils.table(
                new String[]{"ID", "Vendor", "Amount", "Date", "Period", "Paid Until"},
                rows);
    }

    private void record() {
        Vendor v = ConsoleUtils.choose(manager.getVendors(),
                x -> x.getId() + "  " + x.getName(), "RECORD PAYMENT -> PICK VENDOR");
        if (v == null) return;

        Stall s = v.getStallId() == null ? null : manager.findStall(v.getStallId());
        ConsoleUtils.subheader("RECORD PAYMENT FOR " + v.getName());
        if (s != null) {
            ConsoleUtils.info("Assigned stall: " + s.getId() + "  rate: "
                    + ConsoleUtils.money(s.computeRent()));
        } else {
            ConsoleUtils.info("This vendor has no stall assigned.");
        }

        try {
            double amount = ConsoleUtils.readDouble("  Amount (PHP): ");
            LocalDate date = ConsoleUtils.readDate("  Payment date (yyyy-mm-dd, blank = today): ", true);
            if (date == null) date = LocalDate.now();
            String period = ConsoleUtils.readNonBlank("  Period covered (e.g. May 2025 rent): ");
            LocalDate paidUntil = ConsoleUtils.readDate("  Paid-until (yyyy-mm-dd): ", false);
            Receipt r = manager.recordPayment(user, v.getId(), amount, date, period, paidUntil);
            ConsoleUtils.ok("Payment recorded.");
            System.out.println();
            System.out.println(r.getText());
            System.out.println("  Receipt saved to: " + r.getFilePath());
        } catch (InvalidInputException | DataFileException e) {
            ConsoleUtils.error(e.getMessage());
        }
        ConsoleUtils.pause();
    }

    private void view() {
        Payment p = ConsoleUtils.choose(manager.getPayments(),
                x -> x.getId() + "  " + x.getPaymentDate() + "  " + ConsoleUtils.money(x.getAmount()),
                "VIEW RECEIPT");
        if (p == null) return;
        String text = manager.viewReceipt(user, p);
        System.out.println();
        System.out.println(text);
        ConsoleUtils.pause();
    }

    private void delete() {
        Payment p = ConsoleUtils.choose(manager.getPayments(),
                x -> x.getId() + "  " + x.getPaymentDate() + "  " + ConsoleUtils.money(x.getAmount()),
                "DELETE PAYMENT");
        if (p == null) return;
        if (!ConsoleUtils.confirm("  Delete payment " + p.getId() + "?")) return;
        try {
            manager.deletePayment(user, p);
            ConsoleUtils.ok("Payment deleted.");
        } catch (InvalidInputException | DataFileException e) {
            ConsoleUtils.error(e.getMessage());
        }
        ConsoleUtils.pause();
    }
}