package console;

import exception.InvalidLoginException;
import model.User;
import service.MarketManager;

public class LoginMenu {

    public User login(MarketManager manager) {
        while (true) {
            ConsoleUtils.header("SIGN IN");
            System.out.println("  [1] Admin login");
            System.out.println("  [2] Staff login");
            System.out.println("  [0] Exit program");
            int roleChoice = ConsoleUtils.readInt("\n  Choose role: ", 0, 2);
            if (roleChoice == 0) return null;

            String role = roleChoice == 1 ? "ADMIN" : "STAFF";
            System.out.println("\n  --- " + (role.equals("ADMIN") ? "Administrator" : "Staff") + " Login ---");

            for (int attempt = 1; attempt <= 3; attempt++) {
                String username = ConsoleUtils.readLine("  Username: ").trim();
                String password = ConsoleUtils.readLine("  Password: ");
                try {
                    User user = manager.login(username, password, role);
                    ConsoleUtils.ok("Welcome, " + user.getFullName() + " (" + user.getRole() + ")");
                    ConsoleUtils.pause();
                    return user;
                } catch (InvalidLoginException e) {
                    ConsoleUtils.error(e.getMessage());
                    if (attempt < 3) {
                        System.out.println("  (" + (3 - attempt) + " attempt(s) remaining)");
                    }
                }
            }
            ConsoleUtils.error("Too many failed attempts. Returning to role selection.");
            ConsoleUtils.pause();
        }
    }
}