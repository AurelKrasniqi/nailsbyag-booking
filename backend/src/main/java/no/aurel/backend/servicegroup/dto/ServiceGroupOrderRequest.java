package no.aurel.backend.servicegroup.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;

// Every box id, in the order the boxes should appear on the website
public record ServiceGroupOrderRequest(@NotNull List<Long> ids) {
}
