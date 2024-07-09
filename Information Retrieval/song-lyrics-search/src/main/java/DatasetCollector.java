import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;
import java.io.PrintWriter;
import java.sql.SQLException;

/**
 * A class that collects and saves song information to different formats.
 * 
 * This class saves the data into a text file, a serialized file, and a SQLite database.
 * 
 * The collected song data includes title, artist, album, release date, and lyrics.
 */

/*public class DatasetCollector {
	
    public static void main(String[] args) {
        // Initialize required parameters and objects
    	String geniusAccessToken = "rxIBGWqIbN2hhDwXuVhHEMzv0s9lzSFv02J2ZbTmtQ-_bajQ7Lk2mWYOsA7GPdMl";
    	int numberOfSongs = 500;
        String spotifyClientID = "672c5d6749a54c0eae43e7ccf8cac222";
        String spotifyClientSecret = "a98f6fd483a243a682072d242a3ab392";
    	List<String> playlistIds = Arrays.asList("37zyylvxmzBVLPUBBqCOq0", "3wOaoZVQo33ty1Zq6NRTeW", "3Q1DIJ51dJpUO6RhnIHdVx"); // the 3 spotify playlist ID we chose
    	LyricsCollector collector = new LyricsCollector(spotifyClientID, spotifyClientSecret, "");
        String initialAccessToken = collector.getSpotifyAccessToken(spotifyClientID, spotifyClientSecret);
        collector = new LyricsCollector(spotifyClientID, spotifyClientSecret, initialAccessToken);

        // Start the token refresher
    	collector.startTokenRefresher();
    	
    	// create a DatabaseManager instance
        DatabaseManager dbManager = new DatabaseManager();
        
        // Try to connect to the SQLite database
        try {
            dbManager.connect("song_data.db");
        } catch (SQLException e) {
            System.err.println("Error while connecting to the SQLite database: " + e.getMessage());
            e.printStackTrace();
            return;  // Exit the program if the connection fails
        }
        
        // Collect songs from the playlists
        System.out.println("Collecting song data...");
        List<Song> songs = collector.collectSongs(playlistIds, collector.getSpotifyAccessToken(spotifyClientID, spotifyClientSecret), geniusAccessToken, numberOfSongs, dbManager);
        System.out.println("Song data collected successfully!");
        
        saveToTextFile(songs);
        saveToSerFile(songs);
        saveToDatabase(songs, dbManager);

        System.out.println("Song data saved in all formats. Program completed successfully!!");
    }
    
    
    
    //Save song data to a text file.
    private static void saveToTextFile(List<Song> songs) {
        System.out.println("Saving song data to text file...");

        // Save the song information to a text file
        try (PrintWriter writer = new PrintWriter("song_data.txt", "UTF-8")) {
            for (Song song : songs) {
                writer.println("Title: " + song.getTitle());
                writer.println("Artist: " + song.getArtist());
                writer.println("Album: " + song.getAlbum());
                writer.println("Release Date: " + song.getReleaseDate());
                writer.println("Lyrics:");
                writer.println(song.getLyrics());
                writer.println("---------------------------------------------------------");
            }

            System.out.println("Song data saved to text file successfully.\n");
        } catch (IOException e) {
            System.err.println("Failed to save song data to the text file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

    //Save song data to a ser file.
    private static void saveToSerFile(List<Song> songs) {
        System.out.println("Saving song data to ser file...");

        // Save the song information to a ser file
        try (FileOutputStream fos = new FileOutputStream("song_data.ser");
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(songs);
            System.out.println("Song data saved to ser file successfully.\n");
        } catch (IOException e) {
            System.err.println("Failed to save song data to the ser file: " + e.getMessage());
            e.printStackTrace();
        }
    }
        
        
        
    // Save song data to a SQLite database.
    private static void saveToDatabase(List<Song> songs, DatabaseManager dbManager) {
        System.out.println("Saving song data to the SQLite database...");

        try {
            // Batch insert all songs
            for (Song song : songs) {
                dbManager.insertSong(song);
            }
            
            System.out.println("Song data saved to the SQLite database successfully.\n");
            
            // Now remove duplicates
            dbManager.removeDuplicates();
            System.out.println("Removed duplicates from the SQLite database.");

            dbManager.close();
        } catch (SQLException e) {
            System.err.println("Failed to save song data to the SQLite database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}*/