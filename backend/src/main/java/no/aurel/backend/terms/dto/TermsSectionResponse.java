package no.aurel.backend.terms.dto;

import no.aurel.backend.terms.TermsSection;

public record TermsSectionResponse(String heading, String body) {

    public static TermsSectionResponse from(TermsSection entity) {
        return new TermsSectionResponse(entity.getHeading(), entity.getBody());
    }
}
