package no.aurel.backend.addon.dto;

import no.aurel.backend.addon.AddOn;

public record AddOnResponse(
        Long id,
        String name,
        String unitLabel,
        String category,
        String subcategory,
        int priceNok,
        int durationMinutes,
        int maxQuantity
) {

    public static AddOnResponse from(AddOn entity) {
        return new AddOnResponse(
                entity.getId(),
                entity.getName(),
                entity.getUnitLabel(),
                entity.getCategory(),
                entity.getSubcategory(),
                entity.getPriceNok(),
                entity.getDurationMinutes(),
                entity.getMaxQuantity());
    }
}
