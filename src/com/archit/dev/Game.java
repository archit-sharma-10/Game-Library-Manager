package com.archit.dev;

public class Game {
    private String title;
    private String genre;
    private int rating;
    private int hours;
    private String status;

    // --- NEW: A "no-argument" constructor for libraries like Gson ---
    public Game() {
    }
    
    // Constructor to easily create a new Game object from our form
    public Game(String title, String genre, int rating, int hours, String status) {
        this.title = title;
        this.genre = genre;
        this.rating = rating;
        this.hours = hours;
        this.status = status;
    }

    // --- Getters ---
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public int getRating() { return rating; }
    public int getHours() { return hours; }
    public String getStatus() { return status; }

    // --- Setters ---
    public void setTitle(String title) { this.title = title; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setRating(int rating) { this.rating = rating; }
    public void setHours(int hours) { this.hours = hours; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "Game [title=" + title + ", status=" + status + "]";
    }
}