package ece1779.jsp;

public class Showtime {
    public int id;
    public int theaterid, movieid;
    public int year, month, day;           // date
    public int hour, minute;               // time of day
    public float price;
    
    public Showtime (int id,
                     int theaterid, int movieid, 
                     int year, int month, int day,
                     int hour, int minute,
                     float price) {
      this.id = id;
      this.theaterid = theaterid;
      this.movieid = movieid;
      this.year = year;
      this.month = month;
      this.day = day;
      this.hour = hour;
      this.minute = minute;
      this.price = price;
    }
}