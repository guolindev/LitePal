package com.litepaltest.model;

import org.litepal.annotation.Table;
import org.litepal.crud.DataSupport;

public class Product extends DataSupport{
	
	private int id;
	
	private String brand;

	private double price;
	
	public Product() {
	}
	
	public Product(Product p) {
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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
