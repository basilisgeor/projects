Το σύστημα μας είναι μια απλή εφαρμογή αναζήτησης τραγουδιών με τη χρήση λέξεων/φράσεων κλειδιά και έχει ένα βασικό GUI.

Για να τρέξετε το Project :  

Θα πρέπει να αλλάξετε τα path για τα αρχεία και τα directories στις κλάσεις: SongSearchApp (για το INDEX_DIR, SYNONYM_FILE, EMBEDDINGS_FILE), NormalSearcher (για το INDEX_DIR, SYNONYM_FILE) , Searcher (για το INDEX_DIR), Indexer ( για το INDEX_DIR, SYNONYM_FILE, EMBEDDINGS_FILE) 

Το DatasetCollector ενώ έχει δεν χρειάζεται να το τρέξετε, το τρέξαμε εμείς για τη συλλογή των πληροφοριών και την αποθήκευση των εγγράφων.  

Αφού κάνετε τις αλλαγές στα Path για να ταιριάζουν στον υπολογιστή σας, θα πρέπει να εκτελέσετε την main(SongSearchApp).