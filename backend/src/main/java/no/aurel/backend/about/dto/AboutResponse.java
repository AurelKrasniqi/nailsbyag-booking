package no.aurel.backend.about.dto;

import no.aurel.backend.about.AboutContent;

// imageUrl is null when the default photo is used
public record AboutResponse(String heading, String body, String imageUrl) {

    public static AboutResponse from(AboutContent entity) {
        String imageUrl = entity.getImageVersion() == null ? null : "/api/about/image?v=" + entity.getImageVersion();
        return new AboutResponse(entity.getHeading(), entity.getBody(), imageUrl);
    }
}
