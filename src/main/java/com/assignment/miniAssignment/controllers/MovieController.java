package com.assignment.miniAssignment.controllers;

import com.assignment.miniAssignment.models.Movie;
import com.assignment.miniAssignment.services.MovieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/movie")


public class MovieController {
    private static Logger logger = LoggerFactory.getLogger(MovieService.class);
    @Autowired
    private MovieService movieService;

    @GetMapping
    public List<Movie> findAll() {
        logger.info("findAll books " + this.getClass().getName());
        return movieService.findAll();
    }

    //Retrieving the movies.csv file
    @PostMapping("/importCsvFile")
    public List<Movie> importCsv() {

        return movieService.readCsv();
    }

    @PostMapping
    public Movie save(@RequestBody Movie movie) {
        logger.info("save book " + this.getClass().getName());
        return movieService.save(movie);
    }

    //Retrieving Titles directed by given director in the given year range
    @GetMapping("/director/{director}/year-range/{startYear}/{endYear}")
    public List<String> getMoviesByDirectorAndYearRange(@PathVariable String director,
                                                        @PathVariable int startYear,
                                                        @PathVariable int endYear) {
        return movieService.getdirector(director, startYear, endYear);
    }

    //Retrieving English titles which have user reviews more than given user review filter
    @GetMapping("/get-english-titles")
    public List<String> getEnglishTitlesWithUserReviewsGreaterThan(
            @RequestParam int userReviewFilter) {
        return movieService.getEnglishTitlesWithUserReviewsGreaterThan(userReviewFilter);
    }

    // Retrieving highest budget titles
    @GetMapping("/highest-budget-titles")
    public List<String> getHighestBudgetMoviesForYearAndCountry(
            @RequestParam String year, @RequestParam String country) {
        return movieService.getHighestBudgetTitlesForYearAndCountry(year, country);
    }
}
