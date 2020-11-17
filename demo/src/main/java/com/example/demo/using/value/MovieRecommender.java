package com.example.demo.using.value;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MovieRecommender {

	private final String catalog;

	public MovieRecommender(@Value("${catalog.name}") String catalog) {
		this.catalog = catalog;
	}

	public String getCatalog() {
		return catalog;
	}

	@Override
	public String toString() {
		return "MovieRecommender{" + "catalog='" + catalog + '\'' + '}';
	}
}
