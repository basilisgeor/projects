/**
 * This is the Playlist class where we obtain all the necessary information of a Playlist object like id, name
 */
public class Playlist {
    private String id;
    private String name;

    /**
     * Constructor for the Playlist class.
     */
    public Playlist(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters and setters for all properties
    public String getId() {
        return id;
    }

    public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
        return name;
    }
}