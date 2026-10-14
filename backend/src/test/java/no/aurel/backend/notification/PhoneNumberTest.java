package no.aurel.backend.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class PhoneNumberTest {

    @Test
    void plainNorwegianNumberGetsCountryCode() {
        assertEquals("+4791234567", PhoneNumber.toE164("912 34 567"));
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
    void garbageGivesNull() {
        assertNull(PhoneNumber.toE164("abc"));
        assertNull(PhoneNumber.toE164("123"));
        assertNull(PhoneNumber.toE164(null));
    }
}
