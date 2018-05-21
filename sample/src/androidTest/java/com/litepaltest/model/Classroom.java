package com.litepaltest.model;

import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Classroom extends LitePalSupport {
	private int _id;

	private String name;

    private List<String> news = new ArrayList<>();

    private List<Integer> numbers = new ArrayList<>();

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

    public List<String> getNews() {
        return news;
    }

    public void setNews(List<String> news) {
        this.news = news;
    }

    public List<Integer> getNumbers() {
        return numbers;
    }

    public void setNumbers(List<Integer> numbers) {
        this.numbers = numbers;
    }
}
