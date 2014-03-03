package ece1779.jsp;

 
public class Movie {
    public int id;
    public String name, rating, starring, director, synopsis, poster, url;
    public Movie (int id, String name, String rating, String starring, 
            String director, String synopsis, String poster, String url) {
        this.id = id;
        this.name = name;
        this.rating = rating;
        this.starring = starring;
        this.director = director;
        this.synopsis = synopsis;
        this.poster = poster;
        this.url = url;
    }
}