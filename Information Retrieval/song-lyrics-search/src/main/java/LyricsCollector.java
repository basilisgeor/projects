import java.io.IOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import okhttp3.*;
import com.google.gson.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.net.URLEncoder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
import java.util.concurrent.atomic.AtomicReference;



public class LyricsCollector {
	int id = 0;
	String spotifyClientId = "672c5d6749a54c0eae43e7ccf8cac222";
	String spotifyClientSecret = "a98f6fd483a243a682072d242a3ab392";
	AtomicReference<String> spotifyAccessToken = new AtomicReference<>();
	List<String> playlistIds = Arrays.asList("37zyylvxmzBVLPUBBqCOq0", "3wOaoZVQo33ty1Zq6NRTeW", "3Q1DIJ51dJpUO6RhnIHdVx");  // add your playlist IDs here

	
	public LyricsCollector(String spotifyClientId, String spotifyClientSecret, String spotifyAccessToken) {
        this.spotifyClientId = spotifyClientId;
        this.spotifyClientSecret = spotifyClientSecret;
        this.spotifyAccessToken = new AtomicReference<>(spotifyAccessToken);
    }
	
	/**
	 * This method retrieves a list of tracks from a Spotify playlist.
	 * 
	 * @param playlistId  the Spotify playlist ID to fetch tracks from
	 * @param offset      the starting index in the playlist to fetch from
	 * @return            the list of Song objects collected from the playlist
	 */
	public List<Song> getPlaylistTracks(String playlistId, int offset) {
		// Create HTTP client
	    OkHttpClient client = new OkHttpClient();
	    
	    // Initialize an empty list to store the songs
	    List<Song> allSongs = new ArrayList<>();

	    // Print the current access token for debugging
	    System.out.println("Current Spotify access token: " + spotifyAccessToken.get());

	    // Build the URL for the Spotify API request
	    HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.spotify.com/v1/playlists/" + playlistId + "/tracks").newBuilder();
	    urlBuilder.addQueryParameter("market", "US");
	    urlBuilder.addQueryParameter("limit", "100"); // 100 is the maximum allowed limit by the Spotify API
	    urlBuilder.addQueryParameter("offset", String.valueOf(offset)); // Add offset to the query parameters
	    String url = urlBuilder.build().toString();

	    
	    // Print debug info
	    System.out.println("Fetching songs from playlist: " + playlistId + " with offset: " + offset); // Print the playlistId and offset

	    
	    // Create the Spotify API request
	    Request request = new Request.Builder()
	            .url(url)
	            .addHeader("Authorization", "Bearer " + spotifyAccessToken.get())
	            .build();

	    
	    // Execute the request and process the response
	    try (Response response = client.newCall(request).execute()) {
	        if (response.isSuccessful()) {
	            String jsonResponse = response.body().string();
	            System.out.println("Spotify JSON Response: " + jsonResponse); // print the JSON response
	            JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
	            JsonArray tracks = json.getAsJsonArray("items");

	            // Parse each track into a Song object
	            for (JsonElement trackElement : tracks) {
	                JsonObject trackJson = trackElement.getAsJsonObject().getAsJsonObject("track");
	                JsonObject album = trackJson.getAsJsonObject("album");
	                String title = trackJson.has("name") && !trackJson.get("name").isJsonNull() ? trackJson.get("name").getAsString() : "";
	                String artist = trackJson.getAsJsonArray("artists").get(0).getAsJsonObject().has("name") && !trackJson.getAsJsonArray("artists").get(0).getAsJsonObject().get("name").isJsonNull() ? trackJson.getAsJsonArray("artists").get(0).getAsJsonObject().get("name").getAsString() : "";
	                String albumName = album.has("name") && !album.get("name").isJsonNull() ? album.get("name").getAsString() : "";
	                String releaseDate = album.has("release_date") && !album.get("release_date").isJsonNull() ? album.get("release_date").getAsString() : "";

	                // Add the new Song object to the list
	                allSongs.add(new Song(id, title, artist, albumName, releaseDate, ""));
	                id++;
	            }
	        }
	    } catch (IOException e) {
	        System.err.println("Error while making the request: " + e.getMessage());
	        e.printStackTrace();
	    }
	    
	    // Return the list of songs
	    return allSongs;
	}
	
	/**
	 * Fetches lyrics for a given song from Genius.
	 *
	 * This method uses the Genius API to fetch lyrics, and then scrapes the resulting web page using Jsoup. 
	 * 
	 * @param primaryArtist       the name of the artist
	 * @param title               the title of the song
	 * @param geniusAccessToken   the Genius API access token
	 * @return                    the lyrics of the song, or an empty string if they couldn't be fetched
	 */
    public String getLyrics(String primaryArtist, String title, String geniusAccessToken) {
        
        // Create a new HTTP client
    	OkHttpClient client = new OkHttpClient();
        
        // Build the search URL
    	String encodedQuery = URLEncoder.encode(primaryArtist + " " + title, StandardCharsets.UTF_8);
        String searchUrl = "https://api.genius.com/search?q=" + encodedQuery;
        
        // Create the request
        Request searchRequest = new Request.Builder()
                .url(searchUrl)
                .addHeader("Authorization", "Bearer " + geniusAccessToken)
                .build();

        try (Response searchResponse = client.newCall(searchRequest).execute()) {
            // If the request was successful
        	if (searchResponse.isSuccessful()) {
                
                // Parse the response
        		String jsonResponse = searchResponse.body().string();
                JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
                JsonArray hits = json.getAsJsonObject("response").getAsJsonArray("hits");

                // Iterate over the hits to find the best match
                JsonObject bestResult = null;
                for (JsonElement hit : hits) {
                    JsonObject hitInfo = hit.getAsJsonObject().getAsJsonObject("result");
                    String hitTitle = hitInfo.get("title").getAsString();
                    String hitArtist = hitInfo.getAsJsonObject("primary_artist").get("name").getAsString();

                    // If the hit matches the song and artist
                    if (hitTitle.equalsIgnoreCase(title) && hitArtist.equalsIgnoreCase(primaryArtist)) {
                        bestResult = hitInfo;
                        break;
                    }
                }

                // If we found a match
                if (bestResult != null) {
                	
                    // Extract the lyrics URL
                    String lyricsUrl = bestResult.get("url").getAsString();

                    // Update the user agent string to a more recent browser
                    String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:106.0) Gecko/20100101 Firefox/106.0";
                   
                    // Fetch the lyrics page
                    Document lyricsPage = Jsoup.connect(lyricsUrl).userAgent(userAgent).timeout(10 * 1000).get();
                    
                    // Select the lyrics elements
                    Elements lyricsElements = lyricsPage.select("div[class^='Lyrics__Container']");

                    // If the lyrics were found
                    if (lyricsElements != null) {
                        StringBuilder lyrics = new StringBuilder();

                        // Iterate over the lyrics elements
                        for (Element lyricsElement : lyricsElements) {
                            
                        	// Preserve newline characters
                            String lyricsHtml = lyricsElement.html().replaceAll("<br>", "\n");
                            
                            // Parse the HTML string into a Document
                            Document parsedDocument = Parser.parse(lyricsHtml, "");
                            
                            // Clean the parsed document
                            Cleaner cleaner = new Cleaner(Safelist.none());
                            Document cleanDocument = cleaner.clean(parsedDocument);
                            String currentLyrics = cleanDocument.body().text();
                            
                            // Insert a newline before each '[' symbol
                            currentLyrics = currentLyrics.replaceAll("\\[", "\n[");

                            // Append current lyrics to the StringBuilder
                            lyrics.append(currentLyrics).append("\n");
                        }

                        // Return the cleaned and formatted lyrics
                        return lyrics.toString().trim();
                    }
                }
            } else {
                // Log the response body if the request was unsuccessful
                System.out.println("Genius search response body: " + searchResponse.body().string()); // Add this line
            }
        } catch (IOException e) {
            // If there was an error making the request, print the error message and stack trace
            System.err.println("Error while making the request: " + e.getMessage());
            e.printStackTrace();
        }
        // If we got this far, we couldn't fetch the lyrics, so return an empty string        
        return "";
    }
    
    public String getLyrics(Song song, String geniusAccessToken) {
        return getLyrics(song.getArtist(), song.getTitle(), geniusAccessToken);
    }
    
    
    
    
    
    /**
     * This method collects a specified number of songs from a list of playlists.
     * 
     * @param playlistIds         the list of Spotify playlist IDs to collect from
     * @param spotifyAccessToken  the Spotify API access token
     * @param geniusAccessToken   the Genius API access token
     * @param numberOfSongs       the number of songs to collect
     * @param dbManager           the DatabaseManager instance to interact with the database
     * @return                    the list of collected songs
     */
	public List<Song> collectSongs(List<String> playlistIds, String spotifyAccessToken, String geniusAccessToken, int numberOfSongs, DatabaseManager dbManager) {
        
	    // Initialize an empty list to hold the collected songs
		List<Song> songs = Collections.synchronizedList(new ArrayList<>());
        
	    // Define the delay between requests and the number of threads
		int delayMillis = 6000; // Set the desired delay between requests in milliseconds (e.g., 5000ms = 5 seconds)
        int threads = 10; // Set the desired number of threads

        // Initialize the executor service
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        // Create a map to store the offset for each playlist
        Map<String, Integer> playlistOffsets = new HashMap<>();
        for (String playlistId : playlistIds) {
            playlistOffsets.put(playlistId, 0);
        }
        
        // Keep fetching songs until we have enough
        while (songs.size() < numberOfSongs) {

            // Iterate over each playlist
            for (String playlistId : playlistIds) {
            	
                // If we already have enough songs, stop fetching
                if (songs.size() >= numberOfSongs) {
                    break;
                }
                
                // Get the current offset for this playlist
                int offset = playlistOffsets.get(playlistId);

                List<Song> playlistSongs = getPlaylistTracks(playlistId, offset);
                for (Song playlistSong : playlistSongs) {
                    System.out.println("Fetched song: " + playlistSong.getTitle() + " by " + playlistSong.getArtist()); // Added print statement to display the fetched song
                    System.out.println("Fetching lyrics for song: " + playlistSong.getTitle()); // Add progress update

                    // Fetch the lyrics for this song	
                    String lyrics = getLyrics(playlistSong, geniusAccessToken);
                    if (!lyrics.isEmpty()) {
                        // Set the lyrics and ID of the song
                        playlistSong.setLyrics(lyrics);
                        playlistSong.setId(id++);
                        
                        songs.add(playlistSong);
                        
                        System.out.println("Added song: " + playlistSong.getTitle() + " by " + playlistSong.getArtist()); // Add this line to print the song title and artist when added
                        System.out.println("Lyrics: " + lyrics); // Print the fetched lyrics

                    } else {
                        System.out.println("No lyrics found for song: " + playlistSong.getTitle() + " by " + playlistSong.getArtist()); // Add this line to print the song title and artist when no lyrics are found
                    }
                    //add a 6-second delay(delayMillis) between each request to the Genius API:
                    try {
                        Thread.sleep(delayMillis); // Pause the execution for the specified delay
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // Update the offset for this playlist
                playlistOffsets.put(playlistId, offset + 100);
            }
        }
        // Shut down the executor service
        executor.shutdown();
        
        // Return the collected songs
        return songs;
    }
	
	
	/**
	 * This method retrieves a Spotify access token using the client ID and client secret.
	 *
	 * @param clientId     the Spotify client ID
	 * @param clientSecret the Spotify client secret
	 * @return             the access token as a string
	 */
	public String getSpotifyAccessToken(String clientId, String clientSecret) {
        
	    // Combine the client ID and client secret into one string, then encode it
		String credentials = clientId + ":" + clientSecret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        // Create an HTTP client
        OkHttpClient client = new OkHttpClient();
        
        // Build the request body
        RequestBody requestBody = new FormBody.Builder()
                .add("grant_type", "client_credentials")
                .build();
        
        // Construct the request to the Spotify API
        Request request = new Request.Builder()
                .url("https://accounts.spotify.com/api/token")
                .addHeader("Authorization", "Basic " + encodedCredentials)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(requestBody)
                .build();

        // Execute the request and handle the response
        try (Response response = client.newCall(request).execute()) {
        	// Print the entire response from the Spotify API
            System.out.println("Spotify API response: " + response.toString());
            if (response.isSuccessful()) {
                String jsonResponse = response.body().string();
                JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
                
                // Extract the access token from the JSON response
                String newToken = json.get("access_token").getAsString();
                System.out.println("Obtained new Spotify access token: " + newToken);
                
                // Update the global access token variable and return the new token
                spotifyAccessToken.set(newToken);
                return newToken;
            } else {
                // If the response is not successful, print the error message and throw an exception
                System.out.println("Error response body: " + response.body().string());
                String errorMessage = "Error obtaining access token: " + response.code() + " " + response.body().string();
                System.err.println(errorMessage);
                throw new RuntimeException(errorMessage);
            }
        } catch (IOException e) {
            // If there's an exception while executing the request, print the error message and throw an exception
            String errorMessage = "Error while making the request: " + e.getMessage();
            System.err.println(errorMessage);
            e.printStackTrace();
            throw new RuntimeException(errorMessage);
        }
    }
	
	
	/**
	 * Begins the process of regularly refreshing the Spotify access token.
	 *
	 * This method creates a new single-threaded executor service that runs a token 
	 * refresh task at a fixed rate. This task attempts to refresh the Spotify access 
	 * token and updates the 'spotifyAccessToken' accordingly. If the token refresh fails, 
	 * it logs an error message but does not halt the executor service.
	 * 
	 * The refresh task is scheduled to start after an initial delay of 10 seconds and 
	 * then run every 3500 seconds (just under an hour). This is in line with the 
	 * typical expiry time of a Spotify access token, which is 3600 seconds (one hour).
	 */
	public void startTokenRefresher() {
	    // Create a single-threaded executor service to handle token refresh tasks
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(new Runnable() {
        	@Override
            public void run() {
        		try {
                    System.out.println("Refreshing Spotify access token...");
                    
                    // Attempt to get a new Spotify access token
                    String newToken = getSpotifyAccessToken(spotifyClientId, spotifyClientSecret);
                    
                    // If a new token was successfully retrieved, update 'spotifyAccessToken'
                    if (newToken != null) {
                        spotifyAccessToken.set(newToken);
                    } else {
                        // If the token refresh failed, log an error message
                        System.err.println("Failed to refresh Spotify access token");
                    }
                } catch (Exception e) {
                    // If an exception occurred during the token refresh, log the exception message and stack trace
                    System.err.println("Exception occurred during Spotify access token refresh: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            /* Schedule the token refresh task to run at a fixed rate of 3500 seconds. 
        	 * Also add a delay for 10 seconds at the beginning */
        }, 10, 3500, TimeUnit.SECONDS);
    }
}
