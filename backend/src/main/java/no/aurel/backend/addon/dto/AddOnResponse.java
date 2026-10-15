package no.aurel.backend.addon.dto;

import java.util.List;

import no.aurel.backend.addon.AddOn;

// groupIds: the boxes whose treatments this extra can be added to
public record AddOnResponse(
        Long id,
        String name,
        String unitLabel,
        List<Long> groupIds,
        int priceNok,
        int durationMinutes,
        int maxQuantity,
        boolean active
) {

    public static AddOnResponse from(AddOn entity) {
        return new AddOnResponse(
                entity.getId(),
                entity.getName(),
                entity.getUnitLabel(),
                entity.getGroupIds().stream().sorted().toList(),
                entity.getPriceNok(),
                entity.getDurationMinutes(),
                entity.getMaxQuantity(),
                entity.isActive());
    }
}
