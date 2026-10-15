package no.aurel.backend.servicegroup.dto;

import no.aurel.backend.servicegroup.ServiceGroup;

// imageUrl: the photo to show (uploaded, otherwise the one that ships with the website), or null for none.
// hasUploadedImage: whether the admin has uploaded her own, i.e. whether "remove photo" means anything.
// defaultImageUrl: the photo that ships with the website, which comes back when her own is removed.
public record ServiceGroupResponse(
        Long id,
        String title,
        String eyebrow,
        String imageUrl,
        boolean hasUploadedImage,
        String defaultImageUrl,
        int imageFocus,
        int sortOrder
) {

    public static ServiceGroupResponse from(ServiceGroup entity) {
        boolean uploaded = entity.getImageVersion() != null;
        String imageUrl = uploaded
                ? "/api/service-groups/" + entity.getId() + "/image?v=" + entity.getImageVersion()
                : entity.getDefaultImage();
        return new ServiceGroupResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getEyebrow(),
                imageUrl,
                uploaded,
                entity.getDefaultImage(),
                entity.getImageFocus(),
                entity.getSortOrder());
    }
}
