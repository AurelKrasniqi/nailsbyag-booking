package no.aurel.backend.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PhoneNumberTest {

    @Test
    void plainNorwegianMobileGetsCountryCode() {
        assertEquals("+4791234567", PhoneNumber.toE164("912 34 567"));
        assertEquals("+4741234567", PhoneNumber.toE164("41234567"));
    }

    @Test
    void numberWithCountryCodeIsKept() {
        assertEquals("+4791234567", PhoneNumber.toE164("+47 912 34 567"));
        assertEquals("+4791234567", PhoneNumber.toE164("0047 91234567"));
        assertEquals("+4791234567", PhoneNumber.toE164("4791234567"));
    }

    @Test
    void foreignNumberIsKept() {
        assertEquals("+46701234567", PhoneNumber.toE164("+46 70-123 45 67"));
    }

    @Test
    void norwegianLandlineIsRejected() {
        assertNull(PhoneNumber.toE164("22 33 44 55"));
        assertNull(PhoneNumber.toE164("+47 32 12 34 56"));
    }

    @Test
    void garbageGivesNull() {
        assertNull(PhoneNumber.toE164("abc"));
        assertNull(PhoneNumber.toE164("123"));
        assertNull(PhoneNumber.toE164("9123456")); // one digit short
        assertNull(PhoneNumber.toE164("+47 912 34 5678")); // one digit too many
        assertNull(PhoneNumber.toE164(null));
    }

    @Test
    void isValidMatchesToE164() {
        assertTrue(PhoneNumber.isValid("912 34 567"));
        assertFalse(PhoneNumber.isValid("hei"));
    }
}
