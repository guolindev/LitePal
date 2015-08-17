package com.litepaltest.model;

import org.litepal.crud.DataSupport;

public class Cellphone extends DataSupport {

	private Long id;

	private String brand;

	private Character inStock;

	private Double price;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public Character getInStock() {
		return inStock;
	}

	public void setInStock(Character inStock) {
		this.inStock = inStock;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

}