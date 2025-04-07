package io.github.azagniotov.language;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class PrimitiveTrie {

  private final TrieNode root;

  PrimitiveTrie() {
    this.root = new TrieNode();
  }

  TrieNode root() {
    return root;
  }

  boolean isEmpty() {
    return root.kids.isEmpty();
  }

  /**
   * Static helper method to build a Trie from a Set of allowlist strings.
   *
   * @param languageProfileNGrams Set of allowed n-gram strings.
   * @return A populated Trie ready for lookups.
   */
  static PrimitiveTrie buildFromSet(final Set<String> languageProfileNGrams) {
    final PrimitiveTrie trie = new PrimitiveTrie();
    if (languageProfileNGrams == null || languageProfileNGrams.isEmpty()) {
      return trie;
    }

    for (final String nGram : languageProfileNGrams) {
      trie.insert(nGram);
    }
    return trie;
  }

  private void insert(final String nGram) {
    TrieNode current = root;
    for (int idx = 0; idx < nGram.length(); idx++) {
      final char currentChar = nGram.charAt(idx);
      current = current.kids.computeIfAbsent(currentChar, charAsKey -> new TrieNode());
    }
    current.isEndOfWord = true;
  }

  static final class TrieNode {
    private final Map<Character, TrieNode> kids;
    private boolean isEndOfWord;

    TrieNode() {
      this.kids = new HashMap<>();
      this.isEndOfWord = false;
    }

    Map<Character, TrieNode> kids() {
      return this.kids;
    }

    boolean endOfWord() {
      return isEndOfWord;
    }
  }
}
