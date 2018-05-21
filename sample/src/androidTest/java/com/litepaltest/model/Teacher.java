package com.litepaltest.model;

import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;
import java.util.List;

public class Teacher extends LitePalSupport {

	private int id;

	private String teacherName = "";

	private boolean sex = true;

	private int age = 22;

	private int teachYears;

	private IdCard idCard;

	// private Student student;

	private List<Student> students = new ArrayList<Student>();

	// /**
	// * @return the students
	// */
	// public List<Student> getStudents() {
	// return students;
	// }
	//
	// /**
	// * @param students the students to set
	// */
	// public void setStudents(List<Student> students) {
	// this.students = students;
	// }

	/**
	 * @return the teacherName
	 */
	public String getTeacherName() {
		return teacherName;
	}

	/**
	 * @return the idCard
	 */
	public IdCard getIdCard() {
		return idCard;
	}

	/**
	 * @param idCard
	 *            the idCard to set
	 */
	public void setIdCard(IdCard idCard) {
		this.idCard = idCard;
	}

	/**
	 * @param teacherName
	 *            the teacherName to set
	 */
	public void setTeacherName(String teacherName) {
		this.teacherName = teacherName;
	}

	/**
	 * @return the age
	 */
	public int getAge() {
		return age;
	}

	/**
	 * @param age
	 *            the age to set
	 */
	public void setAge(int age) {
		this.age = age;
	}

	/**
	 * @return the teachYears
	 */
	public int getTeachYears() {
		return teachYears;
	}

	/**
	 * @param teachYears
	 *            the teachYears to set
	 */
	public void setTeachYears(int teachYears) {
		this.teachYears = teachYears;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the sex
	 */
	public boolean isSex() {
		return sex;
	}

	/**
	 * @param sex
	 *            the sex to set
	 */
	public void setSex(boolean sex) {
		this.sex = sex;
	}

	public List<Student> getStudents() {
		return students;
	}

	public void setStudents(List<Student> students) {
		this.students = students;
	}

	// private Student student;

	// private List<Student> students = new ArrayList<Student>();
	//
	// private IdCard idCard;

	// public String getName() {
	// return name;
	// }
	//
	// public void setName(String name) {
	// this.name = name;
	// }
	//
	// public boolean isSex() {
	// return sex;
	// }
	//
	// public void setSex(boolean sex) {
	// this.sex = sex;
	// }

}
