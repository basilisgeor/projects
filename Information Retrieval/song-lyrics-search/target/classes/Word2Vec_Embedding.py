from gensim.models import Word2Vec
from gensim.parsing.preprocessing import preprocess_string
import sqlite3
import numpy as np
import json

def connect_db(db_path):
    conn = sqlite3.connect(db_path)
    return conn

def get_all_songs(conn):
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM songs")
    songs = cursor.fetchall()
    return songs

def preprocess_lyrics(lyrics):
    # This will handle lowercasing, removing punctuation, and tokenizing
    return preprocess_string(lyrics)

def lyrics_to_vec(model, lyrics):
    preprocessed_lyrics = preprocess_lyrics(lyrics)
    vectors = []
    for word in preprocessed_lyrics:
        try:
            vectors.append(model.wv[word])
        except KeyError:
            pass
    if vectors:
        return np.mean(vectors, axis=0)
    else:
        # Return a vector of zeros with the same size as the other vectors if there are no words in the vocabulary
        return np.zeros(model.vector_size)
    return np.mean(vectors, axis=0)

db_path = "C:\\Users\\akisg\\OneDrive\\Documents\\Work\\school\\anaktisi Project\\song-lyrics-search\\song_data.db"
conn = connect_db(db_path)
songs = get_all_songs(conn)

# Here we're using a size of 100 for the word vectors, and considering only words that appear at least once in the corpus
model = Word2Vec([preprocess_lyrics(song[2]) for song in songs], vector_size =100, min_count=1)

# Calculate a vector for each song by averaging the vectors of its lyrics
global song_vectors
song_vectors = {song[0]: lyrics_to_vec(model, song[2]) for song in songs}

# Convert numpy arrays to list before saving
song_vectors_list = {k: v.tolist() for k, v in song_vectors.items()}

with open('song_vectors.json', 'w') as f:
    json.dump(song_vectors_list, f)
