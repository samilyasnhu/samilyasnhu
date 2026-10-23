package com.cs360.cs360projecttwo_weighttracker.security;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class PasswordHasherTest {

    @Test
    public void hashesAreSaltedAndVerifyCorrectly() {
        String first = PasswordHasher.hashPassword("ExamplePassword123");
        String second = PasswordHasher.hashPassword("ExamplePassword123");

        assertNotEquals(first, second);
        assertTrue(PasswordHasher.isHashed(first));
        assertTrue(PasswordHasher.verifyPassword("ExamplePassword123", first));
        assertFalse(PasswordHasher.verifyPassword("WrongPassword", first));
    }

    @Test
    public void legacyPlainTextCanStillBeVerifiedForMigration() {
        assertTrue(PasswordHasher.verifyPassword("legacyPassword", "legacyPassword"));
        assertFalse(PasswordHasher.isHashed("legacyPassword"));
    }
}
