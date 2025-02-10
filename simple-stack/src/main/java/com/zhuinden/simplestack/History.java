/*
 * Copyright 2018 Gabor Varadi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhuinden.simplestack;

import android.annotation.TargetApi;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An immutable wrapper over backstack history with some additional helper methods.
 */
public class History<T> extends AbstractList<T> implements List<T> {
    private final List<T> elements;
    
    History() {
        this(Collections.<T>emptyList());
    }
    
    History(List<T> elements) {
        this.elements = Collections.unmodifiableList(new ArrayList<>(elements));
    }

    // operations

    /**
     * Returns the last element in the list, or null if the history is empty.
     *
     * @param <K> the type of the key
     * @return the top key
     */
    @Nullable
    public <K> K top() {
        if(this.isEmpty()) {
            return null;
        }
        // noinspection unchecked
        return (K) this.get(this.size() - 1);
    }

    /**
     * Returns the element indexed from the top.
     *
     * Offset value `0` behaves the same as {@link History#top()}, while `1` returns the one before it.
     * Negative indices are wrapped around, for example `-1` is the first element of the stack, `-2` the second, and so on.
     *
     * Accepted values are in range of [-size, size).
     *
     * @throws IllegalStateException if the history doesn't contain any elements yet.
     * @throws IllegalArgumentException if the provided offset is outside the range of [-size, size).
     *
     * @param offset the offset from the top
     * @param <K> the type of the key
     * @return the key from the top with offset
     */
    @Nonnull
    public <K> K fromTop(int offset) {
        int size = this.size();
        if(size <= 0) {
            throw new IllegalStateException("Cannot obtain elements from an uninitialized history.");
        }
        if(offset < -size || offset >= size) {
            throw new IllegalArgumentException("The provided offset value [" + offset + "] was out of range: [" + -size + "; " + size + ")");
        }
        while(offset < 0) {
            offset += size;
        }
        offset %= size;
        int target = (size - 1 - offset) % size;
        // noinspection unchecked
        return (K) this.get(target);
    }

    /**
     * Returns the root (bottom / first) element of this history, or null if it's empty.
     *
     * @param <K> the type of the key
     * @return the root (bottom) key
     */
    @Nullable
    public <K> K root() {
        if(isEmpty()) {
            return null;
        }
        // noinspection unchecked
        return (K) get(0);
    }

    // factories

    /**
     * Creates a {@link Builder} from this {@link History} to create a modified version of it.
     *
     * @return the history builder
     */
    @Nonnull
    public Builder buildUpon() {
        return History.builderFrom(this);
    }
    
    /**
     * Creates a new history from the provided keys.
     *
     * @param keys the provided keys
     * @param <T> possible base type of the keys
     * @return the history
     */
    @SuppressWarnings("unchecked") // @SafeVarargs is API 19+
    @Nonnull
    public static <T> History<T> of(T... keys) {
        if(keys == null) {
            throw new IllegalArgumentException("Cannot provide `null` as a key!");
        }
        for(Object key : keys) {
            if(key == null) {
                throw new IllegalArgumentException("Cannot provide `null` as a key!");
            }
        }
        return builderFrom(Arrays.asList(keys)).build();
    }

    /**
     * Creates a new history from the provided keys.
     *
     * @param keys the provided keys
     * @param <T> possible base type of the keys
     * @return the history
     */
    @Nonnull
    public static <T> History<T> from(@Nonnull List<? extends T> keys) {
        return builderFrom(keys).build();
    }

    /**
     * Creates a new history builder based on the {@link Backstack}'s history.
     *
     * @param backstack the {@link Backstack}.
     * @return the newly created {@link Builder}.
     */
    @Nonnull
    public static Builder builderFrom(@Nonnull Backstack backstack) {
        if(backstack == null) {
            throw new IllegalArgumentException("Backstack cannot be null!");
        }
        return newBuilder().addAll(backstack.getHistory());
    }

    /**
     * Creates a new history builder based on the {@link BackstackDelegate}'s managed backstack history.
     *
     * @param backstackDelegate the {@link BackstackDelegate}.
     * @return the newly created {@link Builder}.
     */
    @Nonnull
    public static Builder builderFrom(@Nonnull BackstackDelegate backstackDelegate) {
        if(backstackDelegate == null) {
            throw new IllegalArgumentException("BackstackDelegate cannot be null!");
        }
        return builderFrom(backstackDelegate.getBackstack());
    }

    /**
     * Creates a new history builder from the provided ordered elements.
     *
     * @param keys
     * @return the newly created {@link Builder}.
     */
    @SuppressWarnings("unchecked") // @SafeVarargs is API 19+
    @Nonnull
    public static Builder builderOf(Object... keys) {
        return builderFrom(Arrays.asList(keys));
    }

    /**
     * Creates a new history builder from the provided ordered collection.
     *
     * @param keys
     * @return the newly created {@link Builder}.
     */
    @Nonnull
    public static Builder builderFrom(@Nonnull List<?> keys) {
        for(Object key : keys) {
            if(key == null) {
                throw new IllegalArgumentException("Cannot provide `null` as a key!");
            }
        }
        return History.newBuilder().addAll(keys);
    }

    /**
     * Creates a new empty history builder.
     *
     * @return the newly created {@link Builder}.
     */
    @Nonnull
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Creates a new history that contains only the provided key.
     *
     * @param key
     * @return a history that contains the key.
     */
    @Nonnull
    public static <T> History<T> single(@Nonnull T key) {
        return History.newBuilder()
                .add(key)
                .build();
    }

    // delegations
    @Override
    public boolean add(T t) {
        return elements.add(t);
    }

    @Override
    public T set(int index, T element) {
        return elements.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        elements.add(index, element);
    }

    @Override
    public T remove(int index) {
        return elements.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return elements.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return elements.lastIndexOf(o);
    }

    @Override
    public void clear() {
        elements.clear();
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return elements.addAll(index, c);
    }

    @Nonnull
    @Override
    public Iterator<T> iterator() {
        return elements.iterator();
    }

    @Nonnull
    @Override
    public ListIterator<T> listIterator() {
        return elements.listIterator();
    }

    @Nonnull
    @Override
    public ListIterator<T> listIterator(int index) {
        return elements.listIterator(index);
    }

    @Nonnull
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return elements.subList(fromIndex, toIndex);
    }

    @Override
    public boolean equals(Object o) {
        if(o == null) {
            return false;
        }
        if(!(o instanceof History)) {
            return false;
        }
        return elements.equals(((History)o).elements);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + elements.hashCode();
        return result;
    }

    @Override
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return elements.contains(o);
    }

    @Nonnull
    @Override
    public Object[] toArray() {
        return elements.toArray();
    }

    @Nonnull
    @Override
    public <T1> T1[] toArray(@Nonnull T1[] a) {
        return elements.toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        return elements.remove(o);
    }

    @Override
    public boolean containsAll(@Nonnull Collection<?> c) {
        return elements.containsAll(c);
    }

    @Override
    public boolean addAll(@Nonnull Collection<? extends T> c) {
        return elements.addAll(c);
    }

    @Override
    public boolean removeAll(@Nonnull Collection<?> c) {
        return elements.removeAll(c);
    }

    @Override
    public boolean retainAll(@Nonnull Collection<?> c) {
        return elements.retainAll(c);
    }

    @Override
    public String toString() {
        return Arrays.toString(elements.toArray());
    }

    @Override
    public T get(int index) {
        return elements.get(index);
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    @TargetApi(24)
    public boolean removeIf(Predicate<? super T> filter) {
        return elements.removeIf(filter);
    }

    @Override
    @TargetApi(24)
    public void replaceAll(UnaryOperator<T> operator) {
        elements.replaceAll(operator);
    }

    @Override
    @TargetApi(24)
    public void sort(Comparator<? super T> c) {
        elements.sort(c);
    }

    @Override
    @TargetApi(24)
    public Spliterator<T> spliterator() {
        return elements.spliterator();
    }

    @Override
    @TargetApi(24)
    public Stream<T> stream() {
        return elements.stream();
    }

    @Override
    @TargetApi(24)
    public Stream<T> parallelStream() {
        return elements.parallelStream();
    }

    @Override
    @TargetApi(24)
    public void forEach(Consumer<? super T> action) {
        elements.forEach(action);
    }

    /**
     * Builder for {@link History}.
     */
    public static class Builder
            implements Iterable<Object> {
        private ArrayList<Object> list = new ArrayList<>();

        Builder() { // use History.newBuilder()
        }

        /**
         * Adds the keys to the builder.
         *
         * @param keys
         * @return the current builder.
         */
        @Nonnull
        public Builder addAll(@Nonnull List<?> keys) {
            if(keys == null) {
                throw new IllegalArgumentException("Provided collection cannot be null");
            }
            this.list.addAll(keys);
            return this;
        }

        /**
         * Adds the keys to the builder at a given index.
         *
         * @param keys
         * @param index
         * @return the current builder.
         */
        @Nonnull
        public Builder addAllAt(@Nonnull List<?> keys, int index) {
            if(keys == null) {
                throw new IllegalArgumentException("Provided collection cannot be null");
            }
            this.list.addAll(index, keys);
            return this;
        }

        /**
         * Clears the history builder.
         *
         * @return the current builder.
         */
        @Nonnull
        public Builder clear() {
            list.clear();
            return this;
        }

        /**
         * Returns if the given key is contained within the builder.
         *
         * @param key
         * @return true if the builder contains the given key.
         */
        public boolean contains(@Nonnull Object key) {
            checkKey(key);
            return list.contains(key);
        }

        /**
         * Returns if the builder contains all provided keys.
         *
         * @param keys
         * @return true if the builder contains all keys.
         */
        public boolean containsAll(@Nonnull Collection<?> keys) {
            if(keys == null) {
                throw new IllegalArgumentException("Keys cannot be null!");
            }
            return list.containsAll(keys);
        }

        /**
         * Returns the size of the builder.
         *
         * @return the number of keys in the builder.
         */
        public int size() {
            return list.size();
        }

        /**
         * Removes the given key from the builder.
         *
         * @param key
         * @return the current builder.
         */
        @Nonnull
        public Builder remove(@Nonnull Object key) {
            checkKey(key);
            list.remove(key);
            return this;
        }

        /**
         * Remove the key at the given index.
         *
         * @param index
         * @return the current builder.
         */
        @Nonnull
        public Builder removeAt(int index) {
            list.remove(index);
            return this;
        }

        /**
         * Removes all keys from the builder not contained inside the provided keys.
         *
         * @param keys
         * @return the current builder.
         */
        @Nonnull
        public Builder retainAll(@Nonnull Collection<?> keys) {
            checkKeys(keys);
            list.retainAll(keys);
            return this;
        }

        /**
         * Returns if the builder is empty.
         *
         * @return true if the builder does not contain any keys
         */
        public boolean isEmpty() {
            return list.isEmpty();
        }

        /**
         * Removes the last entry in the builder.
         * If the builder is empty, an exception is thrown.
         *
         * @return the current builder.
         */
        @Nonnull
        public Builder removeLast() {
            if(list.isEmpty()) {
                throw new IllegalStateException("Cannot remove element from empty builder");
            }
            list.remove(list.size() - 1);
            return this;
        }

        /**
         * Removes all keys until the provided key is found (exclusive).
         * If the key is not found, an exception is thrown.
         *
         * @param key
         * @return the current builder.
         */
        @Nonnull
        public Builder removeUntil(@Nonnull Object key) {
            checkKey(key);
            while(!list.isEmpty() && !getLast().equals(key)) {
                removeLast();
            }
            if(list.isEmpty()) {
                throw new IllegalArgumentException("[" + key + "] was not found in history!");
            }
            return this;
        }

        /**
         * Returns the index of the provided key.
         *
         * @param key
         * @return the index, -1 if not found.
         */
        public int indexOf(@Nonnull Object key) {
            checkKey(key);
            return list.indexOf(key);
        }

        /**
         * Returns the key at the given index.
         *
         * @param index
         * @return the key at the given index
         */
        @Nonnull
        public <T> T get(int index) {
            // noinspection unchecked
            return (T) list.get(index);
        }

        /**
         * Returns the last element of the builder.
         * If the builder is empty, null is returned.
         *
         * @return the key at the last index
         */
        @Nullable
        public <T> T getLast() {
            // noinspection unchecked
            return (T) (list.isEmpty() ? null : list.get(list.size() - 1));
        }

        /**
         * Adds the provided key as the last element of the builder.
         *
         * @param key
         * @return the current builder.
         */
        @Nonnull
        public Builder add(@Nonnull Object key) {
            checkKey(key);
            list.add(key);
            return this;
        }

        /**
         * Adds the provided key at the provided index.
         *
         * @param key
         * @param index
         * @return the current builder.
         */
        @Nonnull
        public Builder add(@Nonnull Object key, int index) {
            checkKey(key);
            list.add(index, key);
            return this;
        }

        /**
         * Provides an iterator for the builder.
         *
         * @return the iterator
         */
        @Nonnull
        @Override
        public Iterator<Object> iterator() {
            return list.iterator();
        }

        /**
         * Creates the history, which is immutable.
         *
         * @return the built history.
         */
        @Nonnull
        public <T> History<T> build() {
            List<T> list = new LinkedList<>();
            for(Object obj : this.list) {
                // noinspection unchecked
                list.add((T) obj);
            }
            return new History<>(list);
        }

        // validations
        private void checkKey(Object key) {
            if(key == null) {
                throw new IllegalArgumentException("History key cannot be null!");
            }
        }

        private void checkKeys(Collection<?> keys) {
            if(keys == null) {
                throw new IllegalArgumentException("Keys cannot be null!");
            }
        }
    }
}
