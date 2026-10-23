package com.cs360.cs360projecttwo_weighttracker.data;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DatabaseDateCodecTest {

    @Test
    public void convertsDisplayDateToSortableStorageDate() {
        assertEquals("2026-10-04", DatabaseDateCodec.toStorageDate("10/04/2026"));
    }

    @Test
    public void convertsStorageDateBackToDisplayDate() {
        assertEquals("10/04/2026", DatabaseDateCodec.toDisplayDate("2026-10-04"));
    }

    @Test
    public void recognizesStorageDateFormat() {
        assertTrue(DatabaseDateCodec.isStorageDate("2026-01-31"));
        assertFalse(DatabaseDateCodec.isStorageDate("01/31/2026"));
        assertFalse(DatabaseDateCodec.isStorageDate("2026-02-31"));
    }
}
