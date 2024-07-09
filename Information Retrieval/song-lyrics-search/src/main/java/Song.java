import java.io.Serializable;
import org.json.JSONObject;

/**
 * This is the Song class where we obtain all the necessary information of a Song object like Title, id, aritst, album, Release Date and Lyrics
 */

@SuppressWarnings("serial")
public class Song implements Serializable {
	private int id; // Unique id for the song
	private String title; // Title of the song
    private String artist; // Artist of the song
    private String album; // Album of the song
    private String releaseDate; // Release date of the song
    private String lyrics; // Lyrics of the song
    
    
    /**
     * Constructor for the Song class.
     */
    public Song(int id,String title, String artist, String album, String releaseDate, String lyrics) {
    	this.id = id;
    	this.title = title;
        this.artist = artist;
        this.album = album;
        this.releaseDate = releaseDate;
        this.lyrics = lyrics;
    }
    
    // Getters and setters for all properties
    public int getId() {
		return id;
    }
    public void setId(int id) {
    	this.id = id;
    }
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}


	public String getAlbum() {
		return album;
	}
	public void setAlbum(String album) {
		this.album = album;
	}


	public String getReleaseDate() {
		return releaseDate;
	}
	public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
	}


	public String getLyrics() {
		return lyrics;
	}
	public void setLyrics(String lyrics) {
		this.lyrics = lyrics;
	}

	

	/**
     * This method converts the Song object to a JSON object, making it easy to handle.
     * Each property of the Song object will become a key-value pair in the resulting JSON object.
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("title", title);
        json.put("releaseDate", releaseDate);
        json.put("album", album);
        json.put("artist", artist);
        json.put("lyrics", lyrics);
        return json;
    }

}