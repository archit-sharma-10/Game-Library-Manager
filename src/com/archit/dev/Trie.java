package com.archit.dev;

import java.util.ArrayList;
import java.util.List;

public class Trie {

    private final TrieNode root;

    public Trie() {
        root = new TrieNode();
    }

    
    public void insert(String word) {
        TrieNode current = root;
        for (char c : word.toCharArray()) {
            current = current.getChildren().computeIfAbsent(c, node -> new TrieNode());
        }
        current.setEndOfWord(true);
    }

    public List<String> findByPrefix(String prefix) {
        List<String> results = new ArrayList<>();
        TrieNode current = root;

        for (char c : prefix.toCharArray()) {
            TrieNode node = current.getChildren().get(c);
            if (node == null) {
                return results;
            }
            current = node;
        }

        findAllWordsFromNode(current, prefix, results);
        return results;
    }

    private void findAllWordsFromNode(TrieNode node, String currentPrefix, List<String> results) {
        if (node.isEndOfWord()) {
            results.add(currentPrefix);
        }

        for (char c : node.getChildren().keySet()) {
            findAllWordsFromNode(node.getChildren().get(c), currentPrefix + c, results);
        }
    }
}
