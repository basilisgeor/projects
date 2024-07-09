import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;

public class DatabaseManager {
    private Connection connection;

    public void connect(String databasePath) throws SQLException {
    	try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            System.out.println("Connected to database at " + databasePath);
            System.out.println("Database connection: " + connection);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @SuppressWarnings("unused")
	private void createTable() throws SQLException {
    	
    	// Drop the table if it exists
        String dropSql = "DROP TABLE IF EXISTS songs";
        try (Statement dropStmt = connection.createStatement()) {
            dropStmt.execute(dropSql);
        }

        // Create the table and use a UNIQUE constraint on title and artist
        String sql = "CREATE TABLE IF NOT EXISTS songs (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                     "title TEXT NOT NULL," +
                     "artist TEXT NOT NULL," +
                     "album TEXT," +
                     "release_date TEXT," +
                     "lyrics TEXT," +
                     "UNIQUE(title, artist))";


        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }


    public void insertSong(Song song) throws SQLException {
    		String sql = "INSERT OR IGNORE INTO songs (title, artist, album, release_date, lyrics) VALUES (?, ?, ?, ?, ?)";

	        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
	            pstmt.setString(1, song.getTitle());
	            pstmt.setString(2, song.getArtist());
	            pstmt.setString(3, song.getAlbum());
	            pstmt.setString(4, song.getReleaseDate());
	            pstmt.setString(5, song.getLyrics());
	            pstmt.executeUpdate();
	        }
    }
    
    public List<Song> getAllSongs() {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT * FROM songs";
        System.out.println("Executing SQL query: " + sql);


        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("ResultSet: " + rs);
            ResultSetMetaData rsmd = rs.getMetaData();
            System.out.println("Number of columns: " + rsmd.getColumnCount());

            while (rs.next()) {
            	
                String title = rs.getString("title");
                String artist = rs.getString("artist");
                String album = rs.getString("album");
                String releaseDate = rs.getString("release_date");
                String lyrics = rs.getString("lyrics");
                int id = rs.getInt("id");
                
                Song song = new Song(id, title, artist, album, releaseDate, lyrics);
                songs.add(song);
            }
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            e.printStackTrace();  // Print the full stack trace for debugging
        }
        System.out.println("Retrieved " + songs.size() + " songs from the database");

        return songs;
    }
    
    public boolean isConnected() {
        return connection != null;
    }
    
    public void removeDuplicates() throws SQLException {
        String createTempTableSql = "CREATE TABLE IF NOT EXISTS temp_songs AS " +
                                    "SELECT id, title, artist, album, release_date, lyrics " +
                                    "FROM ( " +
                                    "  SELECT *, " +
                                    "  ROW_NUMBER() OVER (PARTITION BY title, artist ORDER BY id) AS rn " +
                                    "  FROM songs " +
                                    ") " +
                                    "WHERE rn = 1";
        String dropOriginalTableSql = "DROP TABLE songs";
        String renameTempTableSql = "ALTER TABLE temp_songs RENAME TO songs";

        try (Statement stmt = connection.createStatement()) {
            // Create a new temporary table with unique songs
            stmt.execute(createTempTableSql);
            // Delete the original table
            stmt.execute(dropOriginalTableSql);
            // Rename the temporary table to the original name
            stmt.execute(renameTempTableSql);
        }
    }


    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
}