package console;

import exception.DataFileException;
import exception.InvalidInputException;
import exception.StallOccupiedException;
import model.Stall;
import model.User;
import model.Vendor;
import service.MarketManager;

import java.util.ArrayList;
import java.util.List;

public class StallMenu {
    private final MarketManager manager;
    private final User user;

    public StallMenu(MarketManager manager, User user) {
        this.manager = manager;
        this.user = user;
    }

    public void run() {
        while (true) {
            ConsoleUtils.header("STALLS");
            listStalls(manager.getStalls());
            System.out.println();
            if (user.canDelete()) {
                System.out.println("  [1] Add stall");
                System.out.println("  [2] Edit stall");
                System.out.println("  [3] Delete stall");
            }
            System.out.println("  [4] Assign vendor");
            System.out.println("  [5] Release stall");
            System.out.println("  [6] Search");
            System.out.println("  [0] Back");
            int choice = ConsoleUtils.readInt("\n  Choose: ", 0, 6);
            switch (choice) {
                case 0 -> { return; }
                case 1 -> { if (user.canDelete()) add(); else notAllowed(); }
                case 2 -> { if (user.canDelete()) edit(); else notAllowed(); }
                case 3 -> { if (user.canDelete()) delete(); else notAllowed(); }
                case 4 -> assign();
                case 5 -> release();
                case 6 -> search();
            }
        }
    }

    private void notAllowed() {
        ConsoleUtils.error("Only admins can do that.");
        ConsoleUtils.pause();
    }

    private void listStalls(List<Stall> stalls) {
        List<String[]> rows = new ArrayList<>();
        for (Stall s : stalls) {
            Vendor v = s.getVendorId() == null ? null : manager.findVendor(s.getVendorId());
            rows.add(new String[]{
                    s.getId(), s.getSection(), s.getStallType(), s.getStatus(),
                    s.getBillingType(), ConsoleUtils.money(s.getRate()),
                    ConsoleUtils.money(s.computeRent()),
                    v == null ? "-" : v.getName()
            });
        }
        ConsoleUtils.table(
                new String[]{"ID", "Section", "Type", "Status", "Billing", "Rate", "Rent", "Vendor"},
                rows);
    }

    private void add() {
        ConsoleUtils.subheader("ADD STALL");
        try {
            String section = ConsoleUtils.readNonBlank("  Section: ");
            String size = ConsoleUtils.readNonBlank("  Size: ");
            System.out.println("  Category:  [1] DRY   [2] WET   [3] FOOD");
            int c = ConsoleUtils.readInt("  Choose: ", 1, 3);
            String category = c == 1 ? "DRY" : (c == 2 ? "WET" : "FOOD");
            System.out.println("  Billing:   [1] DAILY   [2] MONTHLY");
            int b = ConsoleUtils.readInt("  Choose: ", 1, 2);
            String billing = b == 1 ? "DAILY" : "MONTHLY";
            double rate = ConsoleUtils.readDouble("  Rate (PHP): ");
            Stall s = manager.addStall(section, size, category, billing, rate);
            ConsoleUtils.ok("Stall " + s.getId() + " added.");
        } catch (InvalidInputException | DataFileException e) {
            ConsoleUtils.error(e.getMessage());
        }
        ConsoleUtils.pause();
    }

    private void edit() {
        Stall s = ConsoleUtils.choose(manager.getStalls(),
                x -> x.getId() + "  " + x.getSection() + "  (" + x.getStatus() + ")",
                "EDIT STALL");
        if (s == null) return;
        ConsoleUtils.subheader("EDIT STALL " + s.getId());
        try {
            String section = promptOrDefault("  Section [" + s.getSection() + "]: ", s.getSection());
            String size = promptOrDefault("  Size [" + s.getSize() + "]: ", s.getSize());
            System.out.println("  Billing: [1] DAILY  [2] MONTHLY");
            int b = ConsoleUtils.readInt("  Choose (0 = keep [" + s.getBillingType() + "]): ", 0, 2);
            String billing = b == 0 ? s.getBillingType() : (b == 1 ? "DAILY" : "MONTHLY");
            String rateStr = ConsoleUtils.readLine("  Rate [" + s.getRate() + "]: ").trim();
            double rate = rateStr.isEmpty() ? s.getRate() : Double.parseDouble(rateStr);
            manager.updateStall(user, s, section, size, billing, rate);
            ConsoleUtils.ok("Stall updated.");
        } catch (NumberFormatException e) {
            ConsoleUtils.error("Rate must be a number.");
        } catch (InvalidInputException | DataFileException e) {
            ConsoleUtils.error(e.getMessage());
        }
        ConsoleUtils.pause();
    }

    private void delete() {
        Stall s = ConsoleUtils.choose(manager.getStalls(),
                x -> x.getId() + "  " + x.getSection() + "  (" + x.getStatus() + ")",
                "DELETE STALL");
        if (s == null) return;
        if (!ConsoleUtils.confirm("  Delete stall " + s.getId() + "?")) return;
        try {
            manager.deleteStall(user, s);
            ConsoleUtils.ok("Stall deleted.");
        } catch (InvalidInputException | DataFileException e) {
            ConsoleUtils.error(e.getMessage());
        }
        ConsoleUtils.pause();
    }

    private void assign() {
        List<Stall> available = new ArrayList<>();
        for (Stall s : manager.getStalls()) if (!s.isOccupied()) available.add(s);
        if (available.isEmpty()) {
            ConsoleUtils.error("No available stalls.");
            ConsoleUtils.pause();
            return;
        }
        Stall stall = ConsoleUtils.choose(available,
                x -> x.getId() + "  " + x.getSection() + "  (" + x.getStallType() + ")",
                "ASSIGN VENDOR -> PICK STALL");
        if (stall == null) return;

        List<Vendor> free = new ArrayList<>();
        for (Vendor v : manager.getVendors()) if (v.getStallId() == null) free.add(v);
        if (free.isEmpty()) {
            ConsoleUtils.error("Every vendor already has a stall.");
            ConsoleUtils.pause();
            return;
        }
        Vendor v = ConsoleUtils.choose(free, x -> x.getId() + "  " + x.getName(),
                "ASSIGN VENDOR -> PICK VENDOR");
        if (v == null) return;

        try {
            manager.assignStall(stall.getId(), v.getId());
            ConsoleUtils.ok("Vendor " + v.getName() + " assigned to stall " + stall.getId() + ".");
        } catch (StallOccupiedException | InvalidInputException | DataFileException e) {
            ConsoleUtils.error(e.getMessage());
        }
        ConsoleUtils.pause();
    }

    private void release() {
        List<Stall> occupied = new ArrayList<>();
        for (Stall s : manager.getStalls()) if (s.isOccupied()) occupied.add(s);
        if (occupied.isEmpty()) {
            ConsoleUtils.error("No occupied stalls.");
            ConsoleUtils.pause();
            return;
        }
        Stall s = ConsoleUtils.choose(occupied,
                x -> x.getId() + "  " + x.getSection() + "  (vendor: "
                        + (x.getVendorId() == null ? "-" : x.getVendorId()) + ")",
                "RELEASE STALL");
        if (s == null) return;
        if (!ConsoleUtils.confirm("  Release stall " + s.getId() + "?")) return;
        try {
            manager.releaseStall(s.getId());
            ConsoleUtils.ok("Stall released.");
        } catch (InvalidInputException | DataFileException e) {
            ConsoleUtils.error(e.getMessage());
        }
        ConsoleUtils.pause();
    }

    private void search() {
        String term = ConsoleUtils.readNonBlank("  Search (id/section/status/vendor): ").toLowerCase();
        List<Stall> matches = new ArrayList<>();
        for (Stall s : manager.getStalls()) {
            Vendor v = s.getVendorId() == null ? null : manager.findVendor(s.getVendorId());
            String vendorName = v == null ? "" : v.getName().toLowerCase();
            if (s.getId().toLowerCase().contains(term)
                    || s.getSection().toLowerCase().contains(term)
                    || s.getStatus().toLowerCase().contains(term)
                    || vendorName.contains(term)) {
                matches.add(s);
            }
        }
        ConsoleUtils.subheader("SEARCH RESULTS (" + matches.size() + ")");
        listStalls(matches);
        ConsoleUtils.pause();
    }

    private String promptOrDefault(String prompt, String current) {
        String s = ConsoleUtils.readLine(prompt).trim();
        return s.isEmpty() ? current : s;
    }
}