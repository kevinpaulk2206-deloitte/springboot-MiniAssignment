package com.assignment.miniAssignment.services;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;


import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.assignment.miniAssignment.models.Movie;
import com.assignment.miniAssignment.repositories.MovieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service

public class MovieService {
    private static Logger logger = LoggerFactory.getLogger(MovieService.class);
    @Autowired
    private Movie movie;
    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private AmazonDynamoDB amazonDynamoDB;

    @Scheduled(fixedDelay = 24 * 60 * 60 * 1000)

    public void syncData() {

        List<Movie> newMovies = readCsv();
        List<Movie> existingMovies = (List<Movie>) movieRepository.findAll();

        for (Movie newMovie : newMovies) {
            boolean isMovieExists = existingMovies.stream()
                    .anyMatch(movie -> movie.getTitle().equals(newMovie.getTitle()));

            if (!isMovieExists) {
                movieRepository.save(newMovie);
            }
        }
    }
    public List<Movie> findAll() {

        logger.info("findAll books " + this.getClass().getName());
        return movieRepository.findAll();
    }

    public List<Movie> readCsv() {
        List<Movie> newMovies = new ArrayList<>();

        String[] record = new String[20];

        try {
// create a reader
            Reader reader = Files.newBufferedReader(Paths.get("C:\\Users\\kpaulk\\Documents\\movies.csv"));

// create csv reader
            CSVReader csvReader = new CSVReader(reader);

// read one record at a time
            String[] rec;
            while ((rec = csvReader.readNext()) != null) {
                System.out.println("ID: " + rec[0]);
                movie.setImdb_title_id(rec[0]);
                movie.setTitle(rec[1]);
                movie.setOriginal_title(rec[2]);
                movie.setYear(rec[3]);
                movie.setDate_published(rec[4]);
                movie.setGenre(rec[5]);
                movie.setDuration(rec[6]);
                movie.setCountry(rec[7]);
                movie.setLanguage(rec[8]);
                movie.setDirector(rec[9]);
                movie.setWriter(rec[10]);
                movie.setProduction_company(rec[11]);
                movie.setActors(rec[12]);
                movie.setDescription(rec[13]);
                movie.setAvg_vote(rec[14]);
                movie.setVotes(rec[15]);
                movie.setBudget(rec[16]);
                movie.setUsa_gross_income(rec[17]);
                movie.setWorlwide_gross_income(rec[18]);
                movie.setMetasocre(rec[19]);
                movie.setReviews_from_users(rec[20]);
                movie.setReviews_from_critics(rec[21]);
                movieRepository.save(movie);
                newMovies.add(movie);
            }

// close readers
            csvReader.close();
            reader.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }

        return newMovies;

    }

    public Movie save(Movie movie) {
        logger.info("save book " + this.getClass().getName());
        return movieRepository.save(movie);
    }


    public List<String> getdirector(String director, int startYear, int endYear) {
        String accessKey = "12345";
        String secretKey = "12345";
        String region = "mumbai";
        List<String> Titles = new ArrayList<>();

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

        DynamoDbClient ddb = DynamoDbClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(Region.of(region))
                .endpointOverride(URI.create("http://localhost:8083")) // Local DynamoDB endpoint
                .build();

        try {
            ScanRequest scanRequest = ScanRequest.builder()
                    .tableName("movies")
                    .build();
            ScanResponse response = ddb.scan(scanRequest);
            for (Map<String, AttributeValue> item : response.items()) {
                String title = null;
                String itemDirector = item.get("director").s();
                if (director.equals(itemDirector)) {
                    int year = Integer.parseInt(item.get("year").s());
                    if (startYear < year && endYear > year) {
                        title = item.get("title").s();
                        System.out.println(item);
                        Titles.add(title);
                    }
                }
            }

        } catch (
                DynamoDbException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return Titles;
    }

    public List<String> getEnglishTitlesWithUserReviewsGreaterThan(int userReviewFilter) {
        String accessKey = "12345";
        String secretKey = "12345";
        String region = "mumbai";
        List<String> titles = new ArrayList<>();

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

        DynamoDbClient ddb = DynamoDbClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(Region.of(region))
                .endpointOverride(URI.create("http://localhost:8083")) // Local DynamoDB endpoint
                .build();

        try {
            ScanRequest scanRequest = ScanRequest.builder()
                    .tableName("movies")
                    .build();
            ScanResponse response = ddb.scan(scanRequest);


            List<Map<String, AttributeValue>> items = response.items();

// Filter English titles with user reviews greater than the given filter
            List<Map<String, AttributeValue>> filteredItems = items.stream()
                    .filter(item -> {
                        AttributeValue languageValue = item.get("language");
                        AttributeValue reviewsValue = item.get("reviews_from_users");

                        // Check if the attribute values are not null
                        if (languageValue != null && reviewsValue != null) {
                            String language = languageValue.s();
                            String reviews = reviewsValue.s();
//                            System.out.println(item);
                            if ("English".equals(language) && Integer.parseInt(reviews) > userReviewFilter)
                                System.out.println(item);
                            return "English".equals(language) && Integer.parseInt(reviews) > userReviewFilter;
                        }

                        // Skip items with missing or null attribute values
                        return false;
                    })
                    .collect(Collectors.toList());
//            Sort the filtered items by user reviews in descending order
            filteredItems.sort((item1, item2) -> Integer.compare(
                    Integer.parseInt(item2.get("reviews_from_users").s()),
                    Integer.parseInt(item1.get("reviews_from_users").s())));

            for (Map<String, AttributeValue> item : filteredItems) {
                String title = item.get("title").s();
                System.out.println(item);
                titles.add(title);
            }
        } catch (DynamoDbException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return titles;
    }

    public List<String> getHighestBudgetTitlesForYearAndCountry(String year, String country) {
        String accessKey = "12345";
        String secretKey = "12345";
        String region = "mumbai";
        List<String> titles = new ArrayList<>();

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

        DynamoDbClient ddb = DynamoDbClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(Region.of(region))
                .endpointOverride(URI.create("http://localhost:8083")) // Local DynamoDB endpoint
                .build();
        try {
            String tableName = "movies";

            ScanRequest scanRequest = ScanRequest.builder()
                    .tableName(tableName)
                    .build();

            ScanResponse response = ddb.scan(scanRequest);

            List<Map<String, AttributeValue>> items = response.items();

// Filter the items by year and country
            items = items.stream()
                    .filter(item -> item.get("year") != null && item.get("year").s() != null)
                    .filter(item -> item.get("country") != null && item.get("country").s() != null)
                    .filter(item -> item.get("budget") != null && item.get("budget").s() != null)
                    .filter(item -> item.get("year").s().equals(year))
                    .filter(item -> item.get("country").s().equalsIgnoreCase(country))
                    .collect(Collectors.toList());


// Sort the items by budget in descending order
            Map<String, AttributeValue> max = new HashMap<String, AttributeValue>();
            int maximum = -1;
            for (Map<String, AttributeValue> item : items) {
                String budget1 = item.get("budget").s().replaceAll("[^0-9]", "");
                System.out.println(budget1);
                if (Integer.parseInt(budget1) > maximum) {
                    max = item;
                    maximum = Integer.parseInt(budget1);
                }
                if (Integer.parseInt(budget1) > maximum) {
                    max = item;
                    maximum = Integer.parseInt(budget1);
                }
            }

            titles.add(max.get("budget") != null && max.get("budget").s() != null ? max.get("title").s() : "0");
        } catch(DynamoDbException e){
            e.printStackTrace();
            System.exit(1);
        }

        return titles;

    }

}
