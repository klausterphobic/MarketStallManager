package console;

import exception.DataFileException;
import model.User;
import service.MarketManager;

public class ConsoleMain {
    public static void main(String[] args) {
        System.out.println();
        System.out.println("  ================================================================");
        System.out.println("   LOCAL PUBLIC MARKET STALL MANAGEMENT SYSTEM  (console)");
        System.out.println("  ================================================================");
        MarketManager manager;
        try {
            manager = new MarketManager();
        } catch (DataFileException e) {
            System.out.println("  Could not start: " + e.getMessage());
            return;
        }
        LoginMenu loginMenu = new LoginMenu();
        while (true) {
            User user = loginMenu.login(manager);
            if (user == null) {
                System.out.println("\n  Goodbye.\n");
                return;
            }
            new MainMenu(manager, user).run();
            System.out.println("\n  Logged out.\n");
        }
    }
}