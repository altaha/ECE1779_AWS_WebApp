package ece1779.jsp;  


public class Movieplex {

    public Theater [] theaters;       
    public Movie [] movies;
    public Showtime [] showtimes;
    
    public Movieplex() {
        
        Theater [] t = {new Theater(1,"Canada Square","2190 Yonge Street, Toronto, ON, M4S2C6", "416-6460444"),
                        new Theater(2,"SilverCity","2300 Yonge Street, Toronto, ON, M4P1E4","416-5441236")
                       };
        this.theaters = t;
                
        Movie [] m = {  new Movie(1,"Casino Royale", "PG-13","Daniel Craig, Judi Dench, Mads Mikkelsen, Eva Green, Jeffrey Wright", "Martin Campbell", "James Bond's first 007 mission takes him to Madagascar, where he is to spy on a terrorist Mollaka. Not everything goes as planned and Bond decides to investigate, independently of the MI6 agency", "http://www.moviesonline.ca/movie-gallery/albums/userpics/poster_CasinoRoyalTeaserPoster.jpg", "http://www.sonypictures.com/movies/casinoroyale/site/"),
                        new Movie(2,"Babel", "R", "Brad Pitt, Cate Blanchett, Gael Garcia Bernal, Elle Fanning, Koji Yakusho", "Alejandro Gonzalez Inarritu", "In the remote sands of the Moroccan desert, a rifle shot rings out--detonating a chain of events that will link an American tourist couple's frantic struggle to survive, two Moroccan boys involved in an accidental crime, a nanny illegally crossing into Mexico with two American children and a Japanese teen rebel whose father is sought by the police in Tokyo.", "http://z.about.com/d/movies/1/0/D/I/N/babelposter.jpg", "http://www.paramountvantage.com/babel/" )
                     };
        
        this.movies = m;              
                     
        Showtime [] s = { new Showtime(1,1,1,2007,3,23,18,0,8),
                          new Showtime(2,1,1,2007,3,18,20,0,8),
                          new Showtime(3,1,1,2007,3,18,22,0,8),
                          new Showtime(4,1,2,2007,3,23,18,0,8),
                          new Showtime(5,1,2,2007,3,18,20,0,8),
                          new Showtime(6,1,2,2007,3,18,22,0,8),
                          new Showtime(7,2,1,2007,3,23,18,0,8),
                          new Showtime(8,2,1,2007,3,18,20,0,8),
                          new Showtime(9,2,1,2007,3,18,22,0,8),
                          new Showtime(10,2,2,2007,3,23,18,0,8),
                          new Showtime(11,2,2,2007,3,18,20,0,8),
                          new Showtime(12,2,2,2007,3,18,22,0,8)
        };               
                                 
        this.showtimes = s;
     }
}
