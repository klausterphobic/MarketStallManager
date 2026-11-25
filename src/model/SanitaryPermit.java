package model;

import java.time.LocalDate;

public class SanitaryPermit extends Permit {
    public SanitaryPermit(String id, String vendorId, String permitNumber,
                          LocalDate issueDate, LocalDate expiryDate) {
        super(id, vendorId, permitNumber, issueDate, expiryDate);
    }

    @Override
    public String getPermitType() { return "SANITARY"; }
}