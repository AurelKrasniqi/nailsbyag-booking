package no.aurel.backend.addon;

// A chosen extra that has been checked against the database: the real add-on plus how many were chosen
public record ResolvedAddOn(AddOn addOn, int quantity) {

    public int totalPriceNok() {
        return addOn.getPriceNok() * quantity;
    }

    public int totalDurationMinutes() {
        return addOn.getDurationMinutes() * quantity;
    }
}
