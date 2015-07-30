package com.litepaltest.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.litepal.annotation.Table;
import org.litepal.crud.DataSupport;

@Table(name = "class_room")
public class Classroom extends DataSupport {
	private int _id;

	private String name;

	private Set<Student> studentCollection = new HashSet<Student>();

	private List<Teacher> teachers = new ArrayList<Teacher>();

	/**
	 * @return the _id
	 */
	public int get_id() {
		return _id;
	}

	/**
	 * @param _id
	 *            the _id to set
	 */
	public void set_id(int _id) {
		this._id = _id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	public Set<Student> getStudentCollection() {
		return studentCollection;
	}

	public void setStudentCollection(Set<Student> studentCollection) {
		this.studentCollection = studentCollection;
	}

	public List<Teacher> getTeachers() {
		return teachers;
	}

	public void setTeachers(List<Teacher> teachers) {
		this.teachers = teachers;
	}

}
