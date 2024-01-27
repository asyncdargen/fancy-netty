package ru.dargen.fancy.util.registry;

import java.util.Map;

public interface ReversedRegistry<K, V> {

    Map<K, V> asMap();

    Map<V, K> asReversedMap();

    void put(K key, V value);

    V getValue(K key);

    K getKey(V value);

    V removeKey(K key);

    K removeValue(V value);

}
