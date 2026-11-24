package console;

import model.Admin;
import model.Staff;
import model.User;

public class ConsoleMain {
    public static void main(String[] args) {
        System.out.println();
        System.out.println("  ================================================================");
        System.out.println("   LOCAL PUBLIC MARKET STALL MANAGEMENT SYSTEM  (console v0.1)");
        System.out.println("  ================================================================");

        // Day 1 verification of User inheritance, encapsulation, and SHA-256 hashing
        User admin = new Admin("U1", "admin", User.hash("admin123"), "Market Administrator");
        User staff = new Staff("U2", "staff", User.hash("staff123"), "Market Staff");

        ConsoleUtils.header("SYSTEM INITIALIZATION CHECK");
        ConsoleUtils.info("Loaded role: " + admin.getFullName() + " [" + admin.getRole() + "] | canDelete=" + admin.canDelete());
        ConsoleUtils.info("Loaded role: " + staff.getFullName() + " [" + staff.getRole() + "] | canDelete=" + staff.canDelete());
        ConsoleUtils.ok("Core domain foundation and custom exceptions initialized.");
    }
}
