package com.litepaltest.test.annotation;

import com.litepaltest.model.Cellphone;
import com.litepaltest.test.LitePalTestCase;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.util.UUID;

/**
 * Created by tony on 15-8-24.
 */
public class ColumnTest extends LitePalTestCase {

    @Override
    protected void setUp() throws Exception {
        Connector.getDatabase();
    }

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

    public void testNotNull() {
        Cellphone cellphone = new Cellphone();
        cellphone.setBrand("三星");
        cellphone.setInStock('Y');
        cellphone.setPrice(1949.99);
        assertFalse(cellphone.save());
        cellphone.setSerial(UUID.randomUUID().toString());
        assertTrue(cellphone.save());
    }

    public void testDefaultValue() {
        Cellphone cellphone = new Cellphone();
        cellphone.setBrand("三星");
        cellphone.setInStock('Y');
        cellphone.setPrice(1949.99);
        cellphone.setSerial(UUID.randomUUID().toString());
        assertTrue(cellphone.save());
        assertEquals("0.0.0.0", DataSupport.find(Cellphone.class, cellphone.getId()).getMac());
        cellphone.setMac("192.168.0.1");
        assertTrue(cellphone.save());
        assertEquals("192.168.0.1", DataSupport.find(Cellphone.class, cellphone.getId()).getMac());
    }

}
