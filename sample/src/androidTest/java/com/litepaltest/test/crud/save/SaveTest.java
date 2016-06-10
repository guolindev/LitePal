package com.litepaltest.test.crud.save;

import com.litepaltest.model.Cellphone;
import com.litepaltest.model.Classroom;
import com.litepaltest.model.Computer;
import com.litepaltest.model.IdCard;
import com.litepaltest.model.Product;
import com.litepaltest.model.Student;
import com.litepaltest.model.Teacher;
import com.litepaltest.model.WeChatMessage;
import com.litepaltest.model.WeiboMessage;
import com.litepaltest.test.LitePalTestCase;

import junit.framework.Assert;

import org.litepal.crud.DataSupport;

import java.util.List;
import java.util.UUID;

public class SaveTest extends LitePalTestCase {

	public void testSave() {
		Cellphone cell = new Cellphone();
		cell.setBrand("iPhone");
		cell.setPrice(4998.01);
		cell.setInStock('Y');
        cell.setSerial(UUID.randomUUID().toString());
		Assert.assertTrue(cell.save());
		Assert.assertTrue(isDataExists(getTableName(cell), cell.getId()));
	}

    public void testSaveFast() {
        Cellphone cell = new Cellphone();
        cell.setBrand("iPhone");
        cell.setPrice(4998.01);
        cell.setInStock('Y');
        cell.setSerial(UUID.randomUUID().toString());
        Assert.assertTrue(cell.saveFast());
        Assert.assertTrue(isDataExists(getTableName(cell), cell.getId()));
    }
	
	public void testSaveWithConstructors() {
		Computer computer = new Computer("asus", 699.00);
		assertTrue(computer.save());
		Assert.assertTrue(isDataExists(getTableName(computer), computer.getId()));
		Computer c = getComputer(computer.getId());
		assertEquals("asus", c.getBrand());
		assertEquals(699.00, c.getPrice());
		Computer cc = DataSupport.find(Computer.class, computer.getId());
		assertEquals("asus", cc.getBrand());
		assertEquals(699.00, cc.getPrice());
		Product p = new Product(null);
		p.setBrand("apple");
		p.setPrice(1222.33);
		p.save();
		Product.find(Product.class, p.getId());
	}

    public void testSaveFastWithConstructors() {
        Computer computer = new Computer("asus", 699.00);
        assertTrue(computer.saveFast());
        Assert.assertTrue(isDataExists(getTableName(computer), computer.getId()));
        Computer c = getComputer(computer.getId());
        assertEquals("asus", c.getBrand());
        assertEquals(699.00, c.getPrice());
        Computer cc = DataSupport.find(Computer.class, computer.getId());
        assertEquals("asus", cc.getBrand());
        assertEquals(699.00, cc.getPrice());
        Product p = new Product(null);
        p.setBrand("apple");
        p.setPrice(1222.33);
        p.saveFast();
        Product.find(Product.class, p.getId());
    }
	
	public void testSaveAfterDelete() {
		Cellphone cell = new Cellphone();
		cell.setBrand("iPhone");
		cell.setPrice(4998.01);
		cell.setInStock('Y');
        cell.setSerial(UUID.randomUUID().toString());
		Assert.assertTrue(cell.save());
		Assert.assertTrue(isDataExists(getTableName(cell), cell.getId()));
		assertTrue(cell.delete() > 0);
		assertTrue(cell.save());
		Assert.assertTrue(isDataExists(getTableName(cell), cell.getId()));
		Student stu = new Student();
		stu.setName("Jimmy");
		IdCard idcard = new IdCard();
		idcard.setAddress("Washington");
		idcard.setNumber("123456");
		idcard.setStudent(stu);
		stu.setIdcard(idcard);
		stu.save();
		idcard.save();
		Assert.assertTrue(isDataExists(getTableName(stu), stu.getId()));
		Assert.assertTrue(isDataExists(getTableName(idcard), idcard.getId()));
		stu.delete();
		Assert.assertFalse(isDataExists(getTableName(stu), stu.getId()));
		Assert.assertFalse(isDataExists(getTableName(idcard), idcard.getId()));
		stu.save();
		idcard.save();
		Assert.assertTrue(isDataExists(getTableName(stu), stu.getId()));
		Assert.assertTrue(isDataExists(getTableName(idcard), idcard.getId()));
		Student danny = new Student();
		danny.setName("Danny");
		danny.setAge(14);
		Teacher cam = new Teacher();
		cam.setTeacherName("Cam");
		cam.setAge(33);
		cam.setSex(true);
		cam.setTeachYears(5);
		Teacher jack = new Teacher();
		jack.setTeacherName("Jack");
		jack.setAge(36);
		jack.setSex(false);
		jack.setTeachYears(11);
		danny.getTeachers().add(jack);
		danny.getTeachers().add(cam);
		cam.getStudents().add(danny);
		jack.getStudents().add(danny);
		danny.save();
		cam.save();
		jack.save();
		Assert.assertTrue(isDataExists(getTableName(danny), danny.getId()));
		Assert.assertTrue(isDataExists(getTableName(cam), cam.getId()));
		Assert.assertTrue(isDataExists(getTableName(jack), jack.getId()));
		danny.delete();
		Assert.assertFalse(isDataExists(getTableName(danny), danny.getId()));
		Assert.assertTrue(isDataExists(getTableName(cam), cam.getId()));
		Assert.assertTrue(isDataExists(getTableName(jack), jack.getId()));
		danny.save();
		Assert.assertTrue(isDataExists(getTableName(danny), danny.getId()));
		assertEquals(danny.getTeachers().size(), 2);
		Classroom c = new Classroom();
		c.setName("test classroom");
		Student s = new Student();
		s.setName("Tom");
		s.setClassroom(c);
		Student s2 = new Student();
		s2.setName("Tom");
		s2.setClassroom(c);
		assertTrue(c.save());
		assertTrue(s.save());
		assertTrue(s2.save());
		Assert.assertTrue(isDataExists(getTableName(c), c.get_id()));
		Assert.assertTrue(isDataExists(getTableName(s), s.getId()));
		Assert.assertTrue(isDataExists(getTableName(s), s2.getId()));
		c.delete();
		Assert.assertFalse(isDataExists(getTableName(c), c.get_id()));
		Assert.assertFalse(isDataExists(getTableName(s), s.getId()));
		Assert.assertFalse(isDataExists(getTableName(s), s2.getId()));
		c.save();
		s.save();
		s2.save();
		Assert.assertTrue(isDataExists(getTableName(c), c.get_id()));
		Assert.assertTrue(isDataExists(getTableName(s), s.getId()));
		Assert.assertTrue(isDataExists(getTableName(s), s2.getId()));
	}

    public void testSaveFastAfterDelete() {
        Cellphone cell = new Cellphone();
        cell.setBrand("iPhone");
        cell.setPrice(4998.01);
        cell.setInStock('Y');
        cell.setSerial(UUID.randomUUID().toString());
        Assert.assertTrue(cell.saveFast());
        Assert.assertTrue(isDataExists(getTableName(cell), cell.getId()));
        assertTrue(cell.delete() > 0);
        assertTrue(cell.saveFast());
        Assert.assertTrue(isDataExists(getTableName(cell), cell.getId()));
    }

    public void testSaveWithBlob() {
        byte[] b = new byte[10];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte)i;
        }
        Product product = new Product();
        product.setBrand("Android");
        product.setPrice(2899.69);
        product.setPic(b);
        assertTrue(product.saveFast());
        Product p = DataSupport.find(Product.class, product.getId());
        byte[] pic = p.getPic();
        assertEquals(b.length, pic.length);
        for (int i = 0; i < b.length; i++) {
            assertEquals(i, pic[i]);
        }
    }

    public void testSaveIfExists() {
        String serial = UUID.randomUUID().toString();
        Cellphone cell = new Cellphone();
        cell.setBrand("iPhone");
        cell.setPrice(4998.01);
        cell.setInStock('Y');
        cell.setSerial(serial);
        assertTrue(cell.saveIfNotExist("serial = ?", serial));
        Cellphone cell2 = new Cellphone();
        cell2.setBrand("Android");
        cell2.setPrice(1998.01);
        cell2.setInStock('Y');
        cell2.setSerial(serial);
        assertFalse(cell.saveIfNotExist("serial = ?", serial));
        List<Cellphone> cellphoneList = DataSupport.where("serial = ?", serial).find(Cellphone.class);
        assertEquals(1, cellphoneList.size());
    }

    public void testSaveInheritModels() {
        WeChatMessage weChatMessage = new WeChatMessage();
        weChatMessage.setFriend("Tom");
        weChatMessage.setContent("Hello nice to meet you");
        weChatMessage.setTitle("Greeting message");
        weChatMessage.setType(1);
        assertTrue(weChatMessage.save());
        assertTrue(weChatMessage.getId() > 0);
        WeChatMessage message1 = DataSupport.find(WeChatMessage.class, weChatMessage.getId());
        assertEquals("Tom", message1.getFriend());
        assertEquals("Hello nice to meet you", message1.getContent());
        assertNull(message1.getTitle());
        assertEquals(1, message1.getType());

        WeiboMessage weiboMessage = new WeiboMessage();
        weiboMessage.setType(2);
        weiboMessage.setTitle("Following message");
        weiboMessage.setContent("Something big happens");
        weiboMessage.setFollower("Jimmy");
        weiboMessage.setNumber(123456);
        assertTrue(weiboMessage.saveFast());
        assertTrue(weiboMessage.getId() > 0);
    }

    public void testSaveInheritModelsWithAssociations() {
        Cellphone cellphone = new Cellphone();
        cellphone.setBrand("iPhone 7");
        cellphone.setInStock('N');
        cellphone.setPrice(6999.99);
        cellphone.setSerial(UUID.randomUUID().toString());
        cellphone.setMac("ff:3d:4a:99:76");
        cellphone.save();

        WeChatMessage weChatMessage = new WeChatMessage();
        weChatMessage.setFriend("Tom");
        weChatMessage.setContent("Hello nice to meet you");
        weChatMessage.setTitle("Greeting message");
        weChatMessage.setType(1);
        assertTrue(weChatMessage.save());
        assertTrue(weChatMessage.getId() > 0);
        WeChatMessage message1 = DataSupport.find(WeChatMessage.class, weChatMessage.getId());
        assertEquals("Tom", message1.getFriend());
        assertEquals("Hello nice to meet you", message1.getContent());
        assertNull(message1.getTitle());
        assertEquals(1, message1.getType());

        WeiboMessage weiboMessage = new WeiboMessage();
        weiboMessage.setType(2);
        weiboMessage.setTitle("Following message");
        weiboMessage.setContent("Something big happens");
        weiboMessage.setFollower("Jimmy");
        weiboMessage.setNumber(123456);
        weiboMessage.setCellphone(cellphone);
        assertTrue(weiboMessage.save());
        assertTrue(weiboMessage.getId() > 0);
        WeiboMessage message2 = DataSupport.find(WeiboMessage.class, weiboMessage.getId(), true);
        Cellphone result = message2.getCellphone();
        assertEquals(cellphone.getId(), result.getId());
        assertEquals(cellphone.getBrand(), result.getBrand());
        assertEquals(cellphone.getInStock(), result.getInStock());
        assertEquals(cellphone.getPrice(), result.getPrice());
        assertEquals(cellphone.getSerial(), result.getSerial());
        assertEquals(cellphone.getMac(), result.getMac());
    }

}
