package model;

public class Admin extends User {
    public Admin(String id, String username, String passwordHash, String fullName) {
        super(id, username, passwordHash, fullName);
    }

    @Override
    public String getRole() { return "ADMIN"; }

    @Override
    public int getAccessLevel() { return 10; }

    @Override
    public boolean canDelete() { return true; }
}