package com.litepaltest.model;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

public class Cellphone extends DataSupport {

	private Long id;

	public String brand;

	private Character inStock;

	protected Double price;

    @Column(defaultValue = "1.0")
    private double discount;

    @Column(unique = true, nullable = false)
    String serial;

    @Column(nullable = true, defaultValue = "0.0.0.0")
    private String mac;

    @Column(ignore = true)
    private String uuid;

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

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}