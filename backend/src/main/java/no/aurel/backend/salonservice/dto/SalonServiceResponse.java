package no.aurel.backend.salonservice.dto;

import no.aurel.backend.salonservice.SalonService;

public record SalonServiceResponse(
        Long id,
        String name,
        String description,
        int durationMinutes,
        int priceNok,
        String category,
        String subcategory,
        boolean active
) {

    public static SalonServiceResponse from(SalonService entity) {
        return new SalonServiceResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getDurationMinutes(),
                entity.getPriceNok(),
                entity.getCategory(),
                entity.getSubcategory(),
                entity.isActive()
        );
    }
}
