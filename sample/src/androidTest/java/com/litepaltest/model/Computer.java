package com.litepaltest.model;

import org.litepal.crud.LitePalSupport;

public class Computer extends LitePalSupport {
	
	private long id;

	private String brand;

	private double price;

	public Computer(String brand, double price) {
		this.brand = brand;
		this.price = price;
	}
	
	public long getId() {
		return id;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

}
