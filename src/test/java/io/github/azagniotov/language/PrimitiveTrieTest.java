package io.github.azagniotov.language;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import org.junit.Test;

public class PrimitiveTrieTest {

  @Test
  public final void testBuildFromSetFlatTrie() {

    final PrimitiveTrie trie = PrimitiveTrie.buildFromSet(Set.of("a", "b", "c"));
    final PrimitiveTrie.TrieNode root = trie.root();

    assertFalse(root.endOfWord());
    assertEquals(3, root.kids().size());

    assertEquals(0, root.kids().get('a').kids().size());
    assertTrue(root.kids().get('a').endOfWord());

    assertEquals(0, root.kids().get('b').kids().size());
    assertTrue(root.kids().get('b').endOfWord());

    assertEquals(0, root.kids().get('c').kids().size());
    assertTrue(root.kids().get('c').endOfWord());
  }

  @Test
  public final void testBuildFromSet() {

    final PrimitiveTrie trie = PrimitiveTrie.buildFromSet(Set.of("abc"));
    final PrimitiveTrie.TrieNode root = trie.root();

    assertFalse(root.endOfWord());
    assertEquals(1, root.kids().size());

    assertEquals(1, root.kids().get('a').kids().size());
    assertFalse(root.kids().get('a').endOfWord());

    assertEquals(1, root.kids().get('a').kids().size());
    assertFalse(root.kids().get('a').kids().get('b').endOfWord());

    assertEquals(1, root.kids().get('a').kids().get('b').kids().size());
    assertTrue(root.kids().get('a').kids().get('b').kids().get('c').endOfWord());
  }
}
