package com.example.demo.autowired.primary;

import org.springframework.stereotype.Component;

@Component
public class MovieRecommender {

	private MovieCatalog movieCatalog;

	public MovieRecommender(MovieCatalog movieCatalog) {
		this.movieCatalog = movieCatalog;
	}

	public MovieCatalog getMovieCatalog() {
		return movieCatalog;
	}

	public void setMovieCatalog(MovieCatalog movieCatalog) {
		this.movieCatalog = movieCatalog;
	}

	@Override
	public String toString() {
		return "MovieRecommender{" + "movieCatalog=" + movieCatalog + '}';
	}
}
