package com.litepaltest.model;

import org.litepal.crud.LitePalSupport;

public class Product extends LitePalSupport{
	
	private int id;
	
	private String brand;

	private double price;

    private byte[] pic;
	
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

    public byte[] getPic() {
        return pic;
    }

    public void setPic(byte[] pic) {
        this.pic = pic;
    }
}
