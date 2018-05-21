package com.litepaltest.model;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

public class Book extends LitePalSupport implements Serializable{

	private static final long serialVersionUID = 9040804172147110007L;

	private long id;

	private String bookName;

	private Integer pages;

	private double price;

	private char level;

	private short isbn;

	private boolean isPublished;

	private float area;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getBookName() {
		return bookName;
	}

	public void setBookName(String bookName) {
		this.bookName = bookName;
	}

	public Integer getPages() {
		return pages;
	}

	public void setPages(Integer pages) {
		this.pages = pages;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public char getLevel() {
		return level;
	}

	public void setLevel(char level) {
		this.level = level;
	}

	public short getIsbn() {
		return isbn;
	}

	public void setIsbn(short isbn) {
		this.isbn = isbn;
	}

	public boolean isPublished() {
		return isPublished;
	}

	public void setPublished(boolean isPublished) {
		this.isPublished = isPublished;
	}

	public float getArea() {
		return area;
	}

	public void setArea(float area) {
		this.area = area;
	}

}
