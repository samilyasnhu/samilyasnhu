package com.cs360.cs360projecttwo_weighttracker.domain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InputValidatorTest {

    @Test
    public void validWeightReturnsParsedValue() {
        Result<Double> result = InputValidator.validateWeight("185.5", "Weight");
        assertTrue(result.isSuccessful());
        assertEquals(185.5, result.getValue(), 0.001);
    }

    @Test
    public void outOfRangeWeightFailsValidation() {
        Result<Double> result = InputValidator.validateWeight("-4", "Weight");
        assertFalse(result.isSuccessful());
    }

    @Test
    public void invalidCalendarDateFailsValidation() {
        Result<String> result = InputValidator.validateDate("02/31/2026");
        assertFalse(result.isSuccessful());
    }

    @Test
    public void validDatePassesValidation() {
        Result<String> result = InputValidator.validateDate("09/20/2026");
        assertTrue(result.isSuccessful());
    }

    @Test
    public void shortNewPasswordFailsValidation() {
        Result<String> result = InputValidator.validatePassword("123", true);
        assertFalse(result.isSuccessful());
    }
}
