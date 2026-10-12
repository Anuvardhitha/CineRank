package datastructures;

import java.util.ArrayList;
import java.util.List;

public class MovieTrie {

    private TrieNode root;

    public MovieTrie() {
        root = new TrieNode();
    }

    public void insert(String title, int movieId) {

        TrieNode current = root;

        title = title.toLowerCase();

        for (int i = 0; i < title.length(); i++) {

            char ch = title.charAt(i);

            if (!current.children.containsKey(ch)) {
                current.children.put(ch, new TrieNode());
            }

            current = current.children.get(ch);
        }

        current.isEndOfWord = true;
        current.movieId = movieId;
    }

    public List<Integer> searchPrefix(String prefix) {

        List<Integer> results = new ArrayList<Integer>();

        TrieNode current = root;

        prefix = prefix.toLowerCase();

        // Find the node corresponding to the prefix
        for (int i = 0; i < prefix.length(); i++) {

            char ch = prefix.charAt(i);

            if (!current.children.containsKey(ch)) {
                return results;
            }

            current = current.children.get(ch);
        }

        // Find all movie IDs below this node
        collectMovieIds(current, results);

        return results;
    }

    private void collectMovieIds(
            TrieNode node,
            List<Integer> results) {

        if (node.isEndOfWord) {
            results.add(node.movieId);
        }

        for (Character ch : node.children.keySet()) {

            collectMovieIds(
                node.children.get(ch),
                results
            );
        }
    }
}