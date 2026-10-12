package datastructures;

import java.util.HashMap;

public class TrieNode {

    HashMap<Character, TrieNode> children;
    boolean isEndOfWord;
    int movieId;

    public TrieNode() {
        children = new HashMap<Character, TrieNode>();
        isEndOfWord = false;
        movieId = -1;
    }
}