package service;

import data.FileHandler;
import exception.*;
import model.*;

import java.time.LocalDate;
import java.util.*;

public class MarketManager {
    private final FileHandler fileHandler;
    private final List<User> users = new ArrayList<>();
    private final List<Vendor> vendors = new ArrayList<>();
    private final List<Stall> stalls = new ArrayList<>();

    public MarketManager() throws DataFileException {
        this.fileHandler = new FileHandler();
        users.addAll(fileHandler.loadUsers());
        stalls.addAll(fileHandler.loadStalls());
        vendors.addAll(fileHandler.loadVendors());
    }

    // ---------------------------------------------------------------- login

    public User login(String username, String password, String role) throws InvalidLoginException {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            throw new InvalidLoginException("Please enter your username and password.");
        }
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username.trim())
                    && u.getRole().equalsIgnoreCase(role)
                    && u.checkPassword(password)) {
                return u;
            }
        }
        throw new InvalidLoginException("Invalid credentials for " + role.toLowerCase() + " login.");
    }

    // -------------------------------------------------------------- getters

    public List<User> getUsers() { return users; }
    public List<Vendor> getVendors() { return vendors; }
    public List<Stall> getStalls() { return stalls; }

    public Vendor findVendor(String id) {
        for (Vendor v : vendors) if (v.getId().equals(id)) return v;
        return null;
    }

    public Stall findStall(String id) {
        for (Stall s : stalls) if (s.getId().equals(id)) return s;
        return null;
    }

    public User findUser(String id) {
        for (User u : users) if (u.getId().equals(id)) return u;
        return null;
    }

    // -------------------------------------------------------------- vendors

    public Vendor addVendor(String name, String contact, String business, LocalDate registered)
            throws InvalidInputException, DataFileException {
        validateVendorFields(name, contact, business);
        String id = nextId("V", vendors.stream().map(Vendor::getId).toList());
        Vendor v = new Vendor(id, clean(name), clean(contact), clean(business), registered, null);
        vendors.add(v);
        fileHandler.saveVendors(vendors);
        return v;
    }

    public void updateVendor(Vendor v, String name, String contact, String business, LocalDate registered)
            throws InvalidInputException, DataFileException {
        validateVendorFields(name, contact, business);
        v.setName(clean(name));
        v.setContact(clean(contact));
        v.setBusiness(clean(business));
        v.setDateRegistered(registered);
        fileHandler.saveVendors(vendors);
    }

    public void deleteVendor(User actor, Vendor v) throws InvalidInputException, DataFileException {
        if (!actor.canDelete()) throw new InvalidInputException("Staff accounts cannot delete records.");
        if (v == null) return;
        if (v.getStallId() != null) {
            Stall s = findStall(v.getStallId());
            if (s != null) {
                s.setVendorId(null);
                s.setStatus(Stall.AVAILABLE);
            }
        }
        vendors.remove(v);
        fileHandler.saveVendors(vendors);
        fileHandler.saveStalls(stalls);
    }

    private void validateVendorFields(String name, String contact, String business)
            throws InvalidInputException {
        if (name == null || name.isBlank()) throw new InvalidInputException("Vendor name is required.");
        if (business == null || business.isBlank())
            throw new InvalidInputException("Business type/products is required.");
        if (contact == null || !contact.trim().matches("^09\\d{9}$"))
            throw new InvalidInputException("Contact number must be 11 digits starting with 09 (e.g., 09171234567).");
    }

    // --------------------------------------------------------------- stalls

    public Stall addStall(String section, String size, String category, String billingType, double rate)
            throws InvalidInputException, DataFileException {
        if (section == null || section.isBlank()) throw new InvalidInputException("Section is required.");
        if (!"DRY".equals(category) && !"WET".equals(category) && !"FOOD".equals(category))
            throw new InvalidInputException("Category must be DRY, WET, or FOOD.");
        if (!"DAILY".equals(billingType) && !"MONTHLY".equals(billingType))
            throw new InvalidInputException("Billing type must be DAILY or MONTHLY.");
        if (rate <= 0) throw new InvalidInputException("Rate must be greater than zero.");
        String id = nextId("S", stalls.stream().map(Stall::getId).toList());
        Stall s = createStall(id, clean(section), clean(size), category, Stall.AVAILABLE,
                null, billingType, rate);
        stalls.add(s);
        fileHandler.saveStalls(stalls);
        return s;
    }

    public void updateStall(User actor, Stall s, String section, String size,
                            String billingType, double rate)
            throws InvalidInputException, DataFileException {
        if (!actor.canDelete())
            throw new InvalidInputException("Only admins can edit stall details and rates.");
        if (section == null || section.isBlank()) throw new InvalidInputException("Section is required.");
        if (!"DAILY".equals(billingType) && !"MONTHLY".equals(billingType))
            throw new InvalidInputException("Billing type must be DAILY or MONTHLY.");
        if (rate <= 0) throw new InvalidInputException("Rate must be greater than zero.");
        s.setSection(clean(section));
        s.setSize(clean(size));
        s.setBillingType(billingType);
        s.setRate(rate);
        fileHandler.saveStalls(stalls);
    }

    public void deleteStall(User actor, Stall s) throws InvalidInputException, DataFileException {
        if (!actor.canDelete()) throw new InvalidInputException("Staff accounts cannot delete records.");
        if (s == null) return;
        if (s.getVendorId() != null) {
            Vendor v = findVendor(s.getVendorId());
            if (v != null) v.setStallId(null);
        }
        stalls.remove(s);
        fileHandler.saveStalls(stalls);
        fileHandler.saveVendors(vendors);
    }

    public void assignStall(String stallId, String vendorId)
            throws StallOccupiedException, InvalidInputException, DataFileException {
        Stall stall = findStall(stallId);
        Vendor vendor = findVendor(vendorId);
        if (stall == null) throw new InvalidInputException("Stall not found.");
        if (vendor == null) throw new InvalidInputException("Vendor not found.");
        if (stall.isOccupied())
            throw new StallOccupiedException("Stall " + stallId + " is already occupied.");
        if (vendor.getStallId() != null && !vendor.getStallId().isBlank())
            throw new InvalidInputException("Vendor already has a stall assigned.");
        stall.setVendorId(vendorId);
        stall.setStatus(Stall.OCCUPIED);
        vendor.setStallId(stallId);
        fileHandler.saveStalls(stalls);
        fileHandler.saveVendors(vendors);
    }

    public void releaseStall(String stallId) throws InvalidInputException, DataFileException {
        Stall stall = findStall(stallId);
        if (stall == null) throw new InvalidInputException("Stall not found.");
        if (!stall.isOccupied()) throw new InvalidInputException("Stall is already available.");
        String vendorId = stall.getVendorId();
        stall.setVendorId(null);
        stall.setStatus(Stall.AVAILABLE);
        if (vendorId != null) {
            Vendor v = findVendor(vendorId);
            if (v != null) v.setStallId(null);
        }
        fileHandler.saveStalls(stalls);
        fileHandler.saveVendors(vendors);
    }

    private Stall createStall(String id, String section, String size, String category,
                              String status, String vendorId, String billingType, double rate) {
        return switch (category) {
            case "WET" -> new WetStall(id, section, size, category, status, vendorId, billingType, rate);
            case "FOOD" -> new FoodStall(id, section, size, category, status, vendorId, billingType, rate);
            default -> new DryGoodsStall(id, section, size, category, status, vendorId, billingType, rate);
        };
    }

    // ---------------------------------------------------------------- users

    public User addUser(User actor, String username, String password, String fullName, String role)
            throws InvalidInputException, DataFileException {
        if (!actor.canDelete()) throw new InvalidInputException("Only admins can manage users.");
        if (username == null || username.isBlank())
            throw new InvalidInputException("Username is required.");
        if (password == null || password.length() < 6)
            throw new InvalidInputException("Password must be at least 6 characters.");
        if (fullName == null || fullName.isBlank())
            throw new InvalidInputException("Full name is required.");
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username.trim()))
                throw new InvalidInputException("Username already exists.");
        }
        String id = nextId("U", users.stream().map(User::getId).toList());
        String hash = User.hash(password);
        User u = "ADMIN".equals(role)
                ? new Admin(id, clean(username), hash, clean(fullName))
                : new Staff(id, clean(username), hash, clean(fullName));
        users.add(u);
        fileHandler.saveUsers(users);
        return u;
    }

    public void updateUser(User actor, User target, String fullName, String newPassword)
            throws InvalidInputException, DataFileException {
        if (!actor.canDelete()) throw new InvalidInputException("Only admins can manage users.");
        if (fullName == null || fullName.isBlank())
            throw new InvalidInputException("Full name is required.");
        target.setFullName(clean(fullName));
        if (newPassword != null && !newPassword.isBlank()) {
            if (newPassword.length() < 6)
                throw new InvalidInputException("Password must be at least 6 characters.");
            target.setPasswordHash(User.hash(newPassword));
        }
        fileHandler.saveUsers(users);
    }

    public void deleteUser(User actor, User target) throws InvalidInputException, DataFileException {
        if (!actor.canDelete()) throw new InvalidInputException("Only admins can manage users.");
        if (target == null) return;
        if (target.getId().equals(actor.getId()))
            throw new InvalidInputException("You cannot delete your own account.");
        if ("ADMIN".equals(target.getRole())) {
            long admins = users.stream().filter(u -> "ADMIN".equals(u.getRole())).count();
            if (admins <= 1) throw new InvalidInputException("At least one admin account must remain.");
        }
        users.remove(target);
        fileHandler.saveUsers(users);
    }

    private String nextId(String prefix, Collection<String> ids) {
        int max = 0;
        for (String id : ids) {
            if (id != null && id.startsWith(prefix)) {
                try {
                    max = Math.max(max, Integer.parseInt(id.substring(prefix.length())));
                } catch (NumberFormatException ignored) { }
            }
        }
        return prefix + (max + 1);
    }

    private String clean(String s) {
        return s == null ? "" : s.replace(",", " ").trim();
    }
}
