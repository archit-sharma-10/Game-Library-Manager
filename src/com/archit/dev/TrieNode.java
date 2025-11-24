package com.archit.dev;

import java.util.HashMap;
import java.util.Map;


public class TrieNode {
    private final Map<Character, TrieNode> children = new HashMap<>();
    
    private boolean isEndOfWord;
    
    Map<Character, TrieNode> getChildren() {
        return children;
    }

    boolean isEndOfWord() {
        return isEndOfWord;
    }

    void setEndOfWord(boolean endOfWord) {
        isEndOfWord = endOfWord;
    }
}