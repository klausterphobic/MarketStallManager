package console;

import exception.DataFileException;
import exception.InvalidInputException;
import model.Permit;
import model.User;
import model.Vendor;
import service.MarketManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PermitMenu {
    private final MarketManager manager;
    private final User user;

    public PermitMenu(MarketManager manager, User user) {
        this.manager = manager;
        this.user = user;
    }

    public void run() {
        while (true) {
            ConsoleUtils.header("PERMITS");
            listPermits(manager.getPermits());
            System.out.println();
            System.out.println("  [1] Add permit");
            System.out.println("  [2] Update / renew permit");
            if (user.canDelete()) System.out.println("  [3] Delete permit");
            System.out.println("  [4] Filter by status");
            System.out.println("  [0] Back");
            int choice = ConsoleUtils.readInt("\n  Choose: ", 0, 4);
            switch (choice) {
                case 0 -> { return; }
                case 1 -> add();
                case 2 -> update();
                case 3 -> { if (user.canDelete()) delete(); }
                case 4 -> filter();
            }
        }
    }

    private void listPermits(List<Permit> permits) {
        List<String[]> rows = new ArrayList<>();
        for (Permit p : permits) {
            Vendor v = manager.findVendor(p.getVendorId());
            rows.add(new String[]{
                    p.getId(), v == null ? p.getVendorId() : v.getName(),
                    p.getPermitType(), p.getPermitNumber(),
                    p.getIssueDate().toString(), p.getExpiryDate().toString(),
                    p.getDaysUntilExpiry() + "d", p.getStatus()
            });
        }
        ConsoleUtils.table(
                new String[]{"ID", "Vendor", "Type", "Permit No.", "Issued", "Expiry", "Days", "Status"},
                rows);
    }

    private void add() {
        Vendor v = ConsoleUtils.choose(manager.getVendors(),
                x -> x.getId() + "  " + x.getName(), "ADD PERMIT -> PICK VENDOR");
        if (v == null) return;
        ConsoleUtils.subheader("ADD PERMIT FOR " + v.getName());
        try {
            System.out.println("  Type:  [1] SANITARY   [2] BUSINESS");
            int t = ConsoleUtils.readInt("  Choose: ", 1, 2);
            String type = t == 1 ? "SANITARY" : "BUSINESS";
            String number = ConsoleUtils.readNonBlank("  Permit number: ");
            LocalDate issue = ConsoleUtils.readDate("  Issue date (yyyy-mm-dd, blank = today): ", true);
            if (issue == null) issue = LocalDate.now();
            LocalDate expiry = ConsoleUtils.readDate("  Expiry date (yyyy-mm-dd): ", false);
            Permit p = manager.addPermit(v.getId(), type, number, issue, expiry);
            ConsoleUtils.ok("Permit " + p.getId() + " added (" + p.getStatus() + ").");
        } catch (InvalidInputException | DataFileException e) {
            ConsoleUtils.error(e.getMessage());
        }
        ConsoleUtils.pause();
    }

    private void update() {
        Permit p = ConsoleUtils.choose(manager.getPermits(),
                x -> x.getId() + "  " + x.getPermitType() + "  " + x.getPermitNumber()
                        + "  (" + x.getStatus() + ")", "UPDATE / RENEW PERMIT");
        if (p == null) return;
        ConsoleUtils.subheader("UPDATE PERMIT " + p.getPermitNumber());
        try {
            String number = ConsoleUtils.readLine("  Permit number [" + p.getPermitNumber() + "]: ").trim();
            if (number.isEmpty()) number = p.getPermitNumber();
            LocalDate issue = ConsoleUtils.readDate("  Issue date [" + p.getIssueDate() + "]: ", true);
            if (issue == null) issue = p.getIssueDate();
            LocalDate expiry = ConsoleUtils.readDate("  Expiry date [" + p.getExpiryDate() + "]: ", true);
            if (expiry == null) expiry = p.getExpiryDate();
            manager.updatePermit(p, number, issue, expiry);
            ConsoleUtils.ok("Permit updated. Status: " + p.getStatus());
        } catch (InvalidInputException | DataFileException e) {
            ConsoleUtils.error(e.getMessage());
        }
        ConsoleUtils.pause();
    }

    private void delete() {
        Permit p = ConsoleUtils.choose(manager.getPermits(),
                x -> x.getId() + "  " + x.getPermitType() + "  " + x.getPermitNumber(),
                "DELETE PERMIT");
        if (p == null) return;
        if (!ConsoleUtils.confirm("  Delete permit " + p.getPermitNumber() + "?")) return;
        try {
            manager.deletePermit(user, p);
            ConsoleUtils.ok("Permit deleted.");
        } catch (InvalidInputException | DataFileException e) {
            ConsoleUtils.error(e.getMessage());
        }
        ConsoleUtils.pause();
    }

    private void filter() {
        System.out.println("  [1] VALID   [2] EXPIRING   [3] EXPIRED");
        int c = ConsoleUtils.readInt("  Choose: ", 1, 3);
        String status = c == 1 ? "VALID" : (c == 2 ? "EXPIRING" : "EXPIRED");
        List<Permit> matches = new ArrayList<>();
        for (Permit p : manager.getPermits()) if (p.getStatus().equals(status)) matches.add(p);
        ConsoleUtils.subheader("PERMITS WITH STATUS " + status + " (" + matches.size() + ")");
        listPermits(matches);
        ConsoleUtils.pause();
    }
}