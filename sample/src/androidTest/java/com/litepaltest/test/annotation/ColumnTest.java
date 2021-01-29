package com.litepaltest.test.annotation;

import androidx.test.filters.SmallTest;

import com.litepaltest.model.Cellphone;
import com.litepaltest.test.LitePalTestCase;

import org.junit.Before;
import org.junit.Test;
import org.litepal.LitePal;

import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by tony on 15-8-24.
 */
@SmallTest
public class ColumnTest extends LitePalTestCase {

    @Before
    public void setUp() {
        LitePal.getDatabase();
    }

    @Test
    public void testUnique() {
        String serial = UUID.randomUUID().toString();
        for (int i = 0; i < 2; i++) {
            Cellphone cellphone = new Cellphone();
            cellphone.setBrand("三星");
            cellphone.setInStock('Y');
            cellphone.setPrice(1949.99);
            cellphone.setSerial(serial);
            if (i == 0) {
                assertTrue(cellphone.save());
            } else if (i == 1) {
                assertFalse(cellphone.save());
            }
        }
    }

    @Test
    public void testNotNull() {
        Cellphone cellphone = new Cellphone();
        cellphone.setBrand("三星");
        cellphone.setInStock('Y');
        cellphone.setPrice(1949.99);
        assertFalse(cellphone.save());
        cellphone.setSerial(UUID.randomUUID().toString());
        assertTrue(cellphone.save());
    }

    @Test
    public void testDefaultValue() {
        Cellphone cellphone = new Cellphone();
        cellphone.setBrand("三星");
        cellphone.setInStock('Y');
        cellphone.setPrice(1949.99);
        cellphone.setSerial(UUID.randomUUID().toString());
        assertTrue(cellphone.save());
        assertEquals("0.0.0.0", LitePal.find(Cellphone.class, cellphone.getId()).getMac());
        cellphone.setMac("192.168.0.1");
        assertTrue(cellphone.save());
        assertEquals("192.168.0.1", LitePal.find(Cellphone.class, cellphone.getId()).getMac());
    }

}
