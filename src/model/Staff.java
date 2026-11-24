package model;

public class Staff extends User {
    public Staff(String id, String username, String passwordHash, String fullName) {
        super(id, username, passwordHash, fullName);
    }

    @Override
    public String getRole() { return "STAFF"; }

    @Override
    public int getAccessLevel() { return 5; }

    @Override
    public boolean canDelete() { return false; }
}