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
package org.osscolib.indexmap;

import java.util.Arrays;
import java.util.Objects;

final class NodeData<K,V> {

    final int index;
    final boolean multi;
    final Entry<K,V> entry;
    final Entry<K,V>[] entries;



    NodeData(final int index, final Entry<K,V> entry) {
        super();
        this.index = index;
        this.multi = false;
        this.entry = entry;
        this.entries = null;
    }


    NodeData(final int index, final Entry<K,V>[] entries) {
        super();
        this.index = index;
        this.multi = true;
        this.entry = null;
        this.entries = entries;
    }




    int size() {
        return (this.multi ? this.entries.length : 1);
    }




    NodeData<K,V> put(final int index, final Entry<K,V> newEntry) {
        return this.multi ? putMulti(index, newEntry) : putSingle(index, newEntry);
    }


    NodeData<K,V> putMulti(final int index, final Entry<K,V> newEntry) {

        // TODO We should improve this to avoid linear performance depending on the amount of collisions. This was also fixed in HashMap in Java 8 to avoid DoS

        int pos = -1;
        for (int i = 0; i < this.entries.length; i++) {
            if (Objects.equals(this.entries[i].key, newEntry.key)) {
                pos = i;
                break;
            }
        }
        if (pos >= 0) {
            if (this.entries[pos].key == newEntry.key && this.entries[pos].value == newEntry.value) {
                // No need to perform any modifications, we might avoid a rewrite of a tree path!
                // Note this will only happen if key and value are actually the same object, not by object equality
                return this;
            }
            final Entry<K,V>[] newEntries = Arrays.copyOf(this.entries, this.entries.length);
            newEntries[pos] = newEntry;
            return new NodeData<>(index, newEntries);
        }

        final Entry<K,V>[] newEntries = Arrays.copyOf(this.entries, this.entries.length + 1);
        newEntries[this.entries.length] = newEntry;
        return new NodeData<>(index, newEntries);

    }


    NodeData<K,V> putSingle(final int index, final Entry<K,V> newEntry) {

        if (this.entry.key == newEntry.key && this.entry.value == newEntry.value) {
            // No need to perform any modifications, we might avoid a rewrite of a tree path!
            return this;
        }
        if (Objects.equals(this.entry.key, newEntry.key)) {
            // We are replacing the previous value for a new one
            return new NodeData<>(index, newEntry);
        }
        // TODO We should improve this to avoid linear performance depending on the amount of collisions. This was also fixed in HashMap in Java 8 to avoid DoS
        // There is an index collision, but this is a different slot, so we need to go multi value
        final Entry<K,V>[] newEntries = new Entry[] { this.entry, newEntry};
        return new NodeData<>(index, newEntries);

    }




    NodeData<K,V> remove(final int index, final Object key) {
        return this.multi ? removeMulti(index, key) : removeSingle(index, key);
    }


    NodeData<K,V> removeMulti(final int index, final Object key) {

        int pos = -1;
        for (int i = 0; i < this.entries.length; i++) {
            if (Objects.equals(this.entries[i].key, key)) {
                pos = i;
                break;
            }
        }
        if (pos >= 0) {
            if (this.entries.length == 2) {
                // There are only two items in the multi value, and we are removing one, so now its single value
                final Entry<K,V> remainingEntry = this.entries[pos == 0? 1 : 0];
                return new NodeData<>(index, remainingEntry);
            }
            final Entry<K,V>[] newEntries = new Entry[this.entries.length - 1];
            System.arraycopy(this.entries, 0, newEntries, 0, pos);
            System.arraycopy(this.entries, pos + 1, newEntries, pos, (this.entries.length - (pos + 1)));
            return new NodeData<>(index, newEntries);
        }

        return this;

    }


    NodeData<K,V> removeSingle(final int index, final Object key) {
        return Objects.equals(this.entry.key,key) ? null : this;
    }


}