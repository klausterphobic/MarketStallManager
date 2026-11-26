package console;

import exception.DataFileException;
import exception.InvalidInputException;
import model.User;
import service.MarketManager;

import java.util.ArrayList;
import java.util.List;

public class UserMenu {
    private final MarketManager manager;
    private final User actor;

    public UserMenu(MarketManager manager, User actor) {
        this.manager = manager;
        this.actor = actor;
    }

    public void run() {
        while (true) {
            ConsoleUtils.header("MANAGE USERS");
            listUsers();
            System.out.println();
            System.out.println("  [1] Add user");
            System.out.println("  [2] Edit user");
            System.out.println("  [3] Delete user");
            System.out.println("  [0] Back");
            int choice = ConsoleUtils.readInt("\n  Choose: ", 0, 3);
            switch (choice) {
                case 0 -> { return; }
                case 1 -> add();
                case 2 -> edit();
                case 3 -> delete();
            }
        }
    }

    private void listUsers() {
        List<String[]> rows = new ArrayList<>();
        for (User u : manager.getUsers()) {
            rows.add(new String[]{u.getId(), u.getUsername(), u.getFullName(), u.getRole()});
        }
        ConsoleUtils.table(new String[]{"ID", "Username", "Full Name", "Role"}, rows);
    }

    private void add() {
        ConsoleUtils.subheader("ADD USER");
        try {
            String username = ConsoleUtils.readNonBlank("  Username: ");
            String fullName = ConsoleUtils.readNonBlank("  Full name: ");
            String password = ConsoleUtils.readNonBlank("  Password (min 6 chars): ");
            System.out.println("  Role: [1] ADMIN   [2] STAFF");
            int c = ConsoleUtils.readInt("  Choose: ", 1, 2);
            String role = c == 1 ? "ADMIN" : "STAFF";
            User u = manager.addUser(actor, username, password, fullName, role);
            ConsoleUtils.ok("User " + u.getUsername() + " created.");
        } catch (InvalidInputException | DataFileException e) {
            ConsoleUtils.error(e.getMessage());
        }
        ConsoleUtils.pause();
    }

    private void edit() {
        User target = ConsoleUtils.choose(manager.getUsers(),
                u -> u.getId() + "  " + u.getUsername() + "  (" + u.getRole() + ")",
                "EDIT USER");
        if (target == null) return;
        ConsoleUtils.subheader("EDIT USER " + target.getUsername());
        try {
            String name = ConsoleUtils.readLine("  Full name [" + target.getFullName() + "]: ").trim();
            if (name.isEmpty()) name = target.getFullName();
            System.out.println("  Leave blank to keep the current password.");
            String password = ConsoleUtils.readLine("  New password: ");
            manager.updateUser(actor, target, name, password);
            ConsoleUtils.ok("User updated.");
        } catch (InvalidInputException | DataFileException e) {
            ConsoleUtils.error(e.getMessage());
        }
        ConsoleUtils.pause();
    }

    private void delete() {
        User target = ConsoleUtils.choose(manager.getUsers(),
                u -> u.getId() + "  " + u.getUsername() + "  (" + u.getRole() + ")",
                "DELETE USER");
        if (target == null) return;
        if (!ConsoleUtils.confirm("  Delete user " + target.getUsername() + "?")) return;
        try {
            manager.deleteUser(actor, target);
            ConsoleUtils.ok("User deleted.");
        } catch (InvalidInputException | DataFileException e) {
            ConsoleUtils.error(e.getMessage());
        }
        ConsoleUtils.pause();
    }
}