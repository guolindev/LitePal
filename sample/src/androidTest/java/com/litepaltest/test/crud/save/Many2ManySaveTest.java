package com.litepaltest.test.crud.save;

import androidx.test.filters.SmallTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.litepal.crud.LitePalSupport;

import com.litepaltest.model.Student;
import com.litepaltest.model.Teacher;
import com.litepaltest.test.LitePalTestCase;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

@SmallTest
public class Many2ManySaveTest extends LitePalTestCase {

	private Student danny;

	private Student mick;

	private Teacher cam;

	private Teacher jack;

	private void init() {
		danny = new Student();
		danny.setName("Danny");
		danny.setAge(14);
		mick = new Student();
		mick.setName("Mick");
		mick.setAge(13);
		cam = new Teacher();
		cam.setTeacherName("Cam");
		cam.setAge(33);
		cam.setSex(true);
		cam.setTeachYears(5);
		jack = new Teacher();
		jack.setTeacherName("Jack");
		jack.setAge(36);
		jack.setSex(false);
		jack.setTeachYears(11);
	}

	private void buildBidirectionalAssociation() {
		danny.getTeachers().add(jack);
		danny.getTeachers().add(cam);
		mick.getTeachers().add(jack);
		mick.getTeachers().add(cam);
		cam.getStudents().add(danny);
		cam.getStudents().add(mick);
		jack.getStudents().add(danny);
		jack.getStudents().add(mick);
	}

	private void buildUnidirectionalAssociation() {
		if (Math.random() >= 0.5) {
			danny.getTeachers().add(jack);
			danny.getTeachers().add(cam);
			mick.getTeachers().add(jack);
			mick.getTeachers().add(cam);
		} else {
			cam.getStudents().add(danny);
			cam.getStudents().add(mick);
			jack.getStudents().add(danny);
			jack.getStudents().add(mick);
		}
	}

	private List<LitePalSupport> getModelList() {
		List<LitePalSupport> list = new ArrayList<>();
		list.add(jack);
		list.add(danny);
		list.add(cam);
		list.add(mick);
		return list;
	}

	private void saveAllByRandom() {
		List<LitePalSupport> modelList = getModelList();
		while (!modelList.isEmpty()) {
			Random rand = new Random();
			int index = rand.nextInt(modelList.size());
            LitePalSupport model = modelList.remove(index);
			model.save();
		}
	}

	@Test
	public void testCase1() {
		init();
		buildBidirectionalAssociation();
		saveAllByRandom();
		assertTrue(isDataExists(getTableName(danny), danny.getId()));
		assertTrue(isDataExists(getTableName(mick), mick.getId()));
		assertTrue(isDataExists(getTableName(cam), cam.getId()));
		assertTrue(isDataExists(getTableName(jack), jack.getId()));
		assertM2M(getTableName(danny), getTableName(cam), danny.getId(), cam.getId());
		assertM2M(getTableName(danny), getTableName(jack), danny.getId(), jack.getId());
		assertM2M(getTableName(mick), getTableName(cam), mick.getId(), cam.getId());
		assertM2M(getTableName(mick), getTableName(jack), mick.getId(), jack.getId());
	}

    @Test
	public void testCase2() {
		init();
		buildBidirectionalAssociation();
		danny.save();
		jack.save();
		cam.save();
		assertTrue(isDataExists(getTableName(danny), danny.getId()));
		assertFalse(isDataExists(getTableName(mick), mick.getId()));
		assertTrue(isDataExists(getTableName(cam), cam.getId()));
		assertTrue(isDataExists(getTableName(jack), jack.getId()));
		assertM2M(getTableName(danny), getTableName(cam), danny.getId(), cam.getId());
		assertM2M(getTableName(danny), getTableName(jack), danny.getId(), jack.getId());
		assertM2MFalse(getTableName(mick), getTableName(cam), mick.getId(), cam.getId());
		assertM2MFalse(getTableName(mick), getTableName(jack), mick.getId(), jack.getId());
	}

    @Test
	public void testCase3() {
		init();
		buildBidirectionalAssociation();
		jack.save();
		cam.save();
		assertFalse(isDataExists(getTableName(danny), danny.getId()));
		assertFalse(isDataExists(getTableName(mick), mick.getId()));
		assertTrue(isDataExists(getTableName(cam), cam.getId()));
		assertTrue(isDataExists(getTableName(jack), jack.getId()));
		assertM2MFalse(getTableName(danny), getTableName(cam), danny.getId(), cam.getId());
		assertM2MFalse(getTableName(danny), getTableName(jack), danny.getId(), jack.getId());
		assertM2MFalse(getTableName(mick), getTableName(cam), mick.getId(), cam.getId());
		assertM2MFalse(getTableName(mick), getTableName(jack), mick.getId(), jack.getId());
	}

    @Test
	public void testCase4() {
		init();
		buildUnidirectionalAssociation();
		saveAllByRandom();
		assertTrue(isDataExists(getTableName(danny), danny.getId()));
		assertTrue(isDataExists(getTableName(mick), mick.getId()));
		assertTrue(isDataExists(getTableName(cam), cam.getId()));
		assertTrue(isDataExists(getTableName(jack), jack.getId()));
		assertM2M(getTableName(danny), getTableName(cam), danny.getId(), cam.getId());
		assertM2M(getTableName(danny), getTableName(jack), danny.getId(), jack.getId());
		assertM2M(getTableName(mick), getTableName(cam), mick.getId(), cam.getId());
		assertM2M(getTableName(mick), getTableName(jack), mick.getId(), jack.getId());
	}

}
