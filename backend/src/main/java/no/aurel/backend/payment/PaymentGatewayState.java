package no.aurel.backend.payment;

public enum PaymentGatewayState {
    // Created, the customer has not approved it yet
    PENDING,
    // The customer approved; the money is reserved but not yet taken
    AUTHORIZED,
    // The money has been taken
    CAPTURED,
    // Rejected, expired or cancelled: no money will move
    FAILED
}
