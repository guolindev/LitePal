package com.litepaltest.model;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Student extends LitePalSupport {
	private int id;
	private String name;
	private int age;
	private Date birthday;

	@Column(defaultValue = "1589203961859")
	private Date schoolDate;
	private Classroom classroom;
	private IdCard idcard;

	// private Teacher teacher;
	private List<Teacher> teachers = new ArrayList<Teacher>();

	// private IdCard idCard;

	// private double salary;
	// private String address;
	// private Teacher teacher;
	// private int classroom_id;
	// private List lists;
	// private Context context;
	// private IdCard idCard;
	// private List<Classroom> classrooms;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the classroom
	 */
	public Classroom getClassroom() {
		return classroom;
	}

	/**
	 * @param classroom
	 *            the classroom to set
	 */
	public void setClassroom(Classroom classroom) {
		this.classroom = classroom;
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
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
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
	 * @return the birthday
	 */
	public Date getBirthday() {
		return birthday;
	}

	/**
	 * @param birthday
	 *            the birthday to set
	 */
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	/**
	 * @return the idcard
	 */
	public IdCard getIdcard() {
		return idcard;
	}

	/**
	 * @param idcard
	 *            the idcard to set
	 */
	public void setIdcard(IdCard idcard) {
		this.idcard = idcard;
	}

	public List<Teacher> getTeachers() {
		return teachers;
	}

	public void setTeachers(List<Teacher> teachers) {
		this.teachers = teachers;
	}

	// /**
	// * @return the idCard
	// */
	// public IdCard getIdCard() {
	// return idCard;
	// }
	//
	// /**
	// * @param idCard the idCard to set
	// */
	// public void setIdCard(IdCard idCard) {
	// this.idCard = idCard;
	// }


	public Date getSchoolDate() {
		return schoolDate;
	}

	public void setSchoolDate(Date schoolDate) {
		this.schoolDate = schoolDate;
	}

}
