/*
 * =============================================================================
 *
 *   Copyright (c) 2019, The OSSCOLIB team (http://www.osscolib.org)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 * =============================================================================
 */
package org.osscolib.atomichash;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

final class HashEntry<K,V> implements AtomicHashStore.Entry<K,V>, Serializable, Comparable<HashEntry<K,V>> {

    private static final long serialVersionUID = -4165737057742605795L;

    final int hash;
    final K key;
    final V value;




    static int hash(final Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }




    HashEntry(final K key, final V value) {
        super();
        this.hash = hash(key);
        this.key = key;
        this.value = value;
    }


    @Override
    public K getKey() {
        return this.key;
    }

    @Override
    public V getValue() {
        return this.value;
    }

    @Override
    public Object setValue(final Object value) {
        // (Not) implemented, allowed at the java.util.Map.Entry specification
        throw new UnsupportedOperationException("Setting values is forbidden in this implementation");
    }

    @Override
    public boolean equals(final Object o) {
        // Implemented according to the java.util.Map.Entry specification
        if (o == this) {
            return true;
        }
        if (o instanceof HashEntry) {
            final HashEntry<?,?> e = (HashEntry<?,?>)o;
            if (Objects.equals(this.key, e.key) &&
                    Objects.equals(this.value, e.value)) {
                return true;
            }
        }
        if (o instanceof Map.Entry) {
            final Map.Entry<?,?> e = (Map.Entry<?,?>)o;
            if (Objects.equals(this.key, e.getKey()) &&
                    Objects.equals(this.value, e.getValue())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        // Implemented according to the java.util.Map.Entry specification
        return Objects.hashCode(this.key) ^ Objects.hashCode(this.value);
    }

    @Override
    public int compareTo(final HashEntry<K, V> o) {

        // We will need to order in the same way that entries would be returned by an iterator (tree inorder)
        // NOTE this class therefore has a natural ordering that is inconsistent with equals()

        final int h1 = this.hash;
        final int h2 = o.hash;

        if (h1 == h2) {
            // Hash collisions are solved by comparing the key's identity hash code.
            // NOTE it's important that we don't involve values here so that we can perform replaceAll
            // operations without needing to reorder the entries after value changes.
            return Integer.compare(
                        System.identityHashCode(this.key),
                        System.identityHashCode(o.key));
        }

        int comp;
        Level level = Level.LEVEL0;
        do {
            comp = Integer.compare(level.pos(h1), level.pos(h2));
            if (comp != 0) {
                return comp;
            }
            level = level.next;
        } while (true);

    }

}