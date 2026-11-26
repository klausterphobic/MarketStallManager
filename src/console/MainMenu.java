package console;

import model.User;
import service.MarketManager;

public class MainMenu {
    private final MarketManager manager;
    private final User user;

    public MainMenu(MarketManager manager, User user) {
        this.manager = manager;
        this.user = user;
    }

    public void run() {
        while (true) {
            ConsoleUtils.header("MAIN MENU (Core Modules)");
            System.out.println("  Signed in: " + user.getFullName() + " (" + user.getRole() + ")");
            System.out.println();
            System.out.println("  [1] Vendors");
            System.out.println("  [2] Stalls");
            if (user.canDelete()) {
                System.out.println("  [3] Manage Users");
            }
            System.out.println("  [0] Logout");
            System.out.println();
            int max = user.canDelete() ? 3 : 2;
            int choice = ConsoleUtils.readInt("  Choose: ", 0, max);
            switch (choice) {
                case 0 -> { return; }
                case 1 -> new VendorMenu(manager, user).run();
                case 2 -> new StallMenu(manager, user).run();
                case 3 -> { if (user.canDelete()) new UserMenu(manager, user).run(); }
            }
        }
    }
}
