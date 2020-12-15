package com.example.demo._3._5_2;

import java.math.BigDecimal;

import org.springframework.format.annotation.NumberFormat;

public class MyModel {

	@NumberFormat(style = NumberFormat.Style.CURRENCY, pattern = "\u00A5###,###.###")
	private final BigDecimal bigDecimal;

	public MyModel(BigDecimal bigDecimal) {
		this.bigDecimal = bigDecimal;
	}

	public BigDecimal getBigDecimal() {
		return bigDecimal;
	}
}
