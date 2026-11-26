package console;

import exception.DataFileException;
import exception.InvalidInputException;
import model.Stall;
import model.User;
import model.Vendor;
import service.MarketManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VendorMenu {
    private final MarketManager manager;
    private final User user;

    public VendorMenu(MarketManager manager, User user) {
        this.manager = manager;
        this.user = user;
    }

    public void run() {
        while (true) {
            ConsoleUtils.header("VENDORS");
            listVendors(manager.getVendors());
            System.out.println();
            System.out.println("  [1] Add vendor");
            System.out.println("  [2] Edit vendor");
            System.out.println("  [3] Search vendors");
            if (user.canDelete()) System.out.println("  [4] Delete vendor");
            System.out.println("  [0] Back");
            int choice = ConsoleUtils.readInt("\n  Choose: ", 0, user.canDelete() ? 4 : 3);
            switch (choice) {
                case 0 -> { return; }
                case 1 -> add();
                case 2 -> edit();
                case 3 -> search();
                case 4 -> { if (user.canDelete()) delete(); }
            }
        }
    }

    private void listVendors(List<Vendor> vendors) {
        List<String[]> rows = new ArrayList<>();
        for (Vendor v : vendors) {
            Stall s = v.getStallId() == null ? null : manager.findStall(v.getStallId());
            String status = v.getPaidUntil() == null ? "NO PAYMENT"
                    : (v.getPaidUntil().isBefore(LocalDate.now()) ? "OVERDUE" : "OK");
            rows.add(new String[]{
                    v.getId(), v.getName(), v.getContact(), v.getBusiness(),
                    s == null ? "-" : s.getId(),
                    v.getPaidUntil() == null ? "-" : v.getPaidUntil().toString(),
                    status
            });
        }
        ConsoleUtils.table(
                new String[]{"ID", "Name", "Contact", "Business", "Stall", "Paid Until", "Status"},
                rows);
    }

    private void add() {
        ConsoleUtils.subheader("ADD VENDOR");
        try {
            String name = ConsoleUtils.readNonBlank("  Name: ");
            String contact = ConsoleUtils.readNonBlank("  Contact (11 digits, 09...): ");
            String business = ConsoleUtils.readNonBlank("  Business / Products: ");
            LocalDate registered = ConsoleUtils.readDate("  Date registered (yyyy-mm-dd, blank = today): ", true);
            if (registered == null) registered = LocalDate.now();
            Vendor v = manager.addVendor(name, contact, business, registered);
            ConsoleUtils.ok("Vendor " + v.getId() + " added.");
        } catch (InvalidInputException | DataFileException e) {
            ConsoleUtils.error(e.getMessage());
        }
        ConsoleUtils.pause();
    }

    private void edit() {
        Vendor v = ConsoleUtils.choose(manager.getVendors(),
                x -> x.getId() + "  " + x.getName(), "EDIT VENDOR");
        if (v == null) return;
        ConsoleUtils.subheader("EDIT VENDOR " + v.getId());
        try {
            String name = promptOrDefault("  Name [" + v.getName() + "]: ", v.getName());
            String contact = promptOrDefault("  Contact [" + v.getContact() + "]: ", v.getContact());
            String business = promptOrDefault("  Business [" + v.getBusiness() + "]: ", v.getBusiness());
            LocalDate reg = ConsoleUtils.readDate("  Registered [" + v.getDateRegistered() + "]: ", true);
            if (reg == null) reg = v.getDateRegistered();
            manager.updateVendor(v, name, contact, business, reg);
            ConsoleUtils.ok("Vendor updated.");
        } catch (InvalidInputException | DataFileException e) {
            ConsoleUtils.error(e.getMessage());
        }
        ConsoleUtils.pause();
    }

    private void search() {
        String term = ConsoleUtils.readNonBlank("  Search (name/contact/business/id): ").toLowerCase();
        List<Vendor> matches = new ArrayList<>();
        for (Vendor v : manager.getVendors()) {
            if (v.getId().toLowerCase().contains(term)
                    || v.getName().toLowerCase().contains(term)
                    || v.getContact().toLowerCase().contains(term)
                    || v.getBusiness().toLowerCase().contains(term)) {
                matches.add(v);
            }
        }
        ConsoleUtils.subheader("SEARCH RESULTS (" + matches.size() + ")");
        listVendors(matches);
        ConsoleUtils.pause();
    }

    private void delete() {
        Vendor v = ConsoleUtils.choose(manager.getVendors(),
                x -> x.getId() + "  " + x.getName(), "DELETE VENDOR");
        if (v == null) return;
        if (!ConsoleUtils.confirm("  Delete " + v.getName()
                + "? Related payments and permits will also be deleted.")) return;
        try {
            manager.deleteVendor(user, v);
            ConsoleUtils.ok("Vendor deleted.");
        } catch (InvalidInputException | DataFileException e) {
            ConsoleUtils.error(e.getMessage());
        }
        ConsoleUtils.pause();
    }

    private String promptOrDefault(String prompt, String current) {
        String s = ConsoleUtils.readLine(prompt).trim();
        return s.isEmpty() ? current : s;
    }
}