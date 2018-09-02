/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.mucommander.cache;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * LRU cache implementation which uses <code>LinkedHashMap</code> which provides fast retrieval and insertion
 * operations.
 * 
 * <p>The only area this implementation is slow at, is checking for and removing expired elements which
 * requires traversing all values and <code>LinkedHashMap</code> is slow at that. 
 * To minimize the impact this could have on performance, this operation is not systematically performed
 * for each call to <code>get()</code> and <code>set()</code> methods, unless the cache is full. 
 * That means this implementation is not as aggressive as it could be in terms of releasing expired items' memory
 * but favors performance instead, which is what caches are for.
 *
 * @author Maxence Bernard
 */
public class FastLRUCache<K, V> extends LRUCache<K,V> {

    /** Cache key->value/expirationDate map */
    private LinkedHashMap<K, Value<V>> cacheMap;

    /** Timestamp of last expired items purge */
    private long lastExpiredPurge;

    /** Number of millisecond to wait between 2 expired items purges, if cache is not full */
    private final static int PURGE_EXPIRED_DELAY = 1000;

    private static final class Value<V> {
        private V val;
        private Long expiration;

        public Value(V value, Long expirationDate) {
            this.val = value;
            this.expiration = expirationDate;
        }
    }
		

    public FastLRUCache(int capacity) {
        super(capacity);
        this.cacheMap = new LinkedHashMap<K, Value<V>>(16, 0.75f, true) {
                // Override this method to automatically remove eldest entry before insertion when cache is full
                @Override
                protected final boolean removeEldestEntry(Map.Entry<K, Value<V>> eldest) {
                    return cacheMap.size() > FastLRUCache.this.capacity;
                }
            };
    }


    /**
     * Returns a String representation of this cache.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString()).
                append(" size=").append(cacheMap.size()).
                append(" capacity=").append(capacity).
                append(" eldestExpirationDate=").append(eldestExpirationDate).append('\n');

        int i = 0;
        for (Map.Entry<K, Value<V>> mapEntry : cacheMap.entrySet()) {
            Object key = mapEntry.getKey();
            Value<V> value = mapEntry.getValue();
            sb.append(i++).append("-key=").append(key).append(" value=").append(value.val).
                    append(" expirationDate=").append(value.expiration).append('\n');
        }
		
        if (UPDATE_CACHE_COUNTERS) {
            sb.append("nbCacheHits=").append(nbHits).append(" nbCacheMisses=").append(nbMisses).append('\n');
        }
		
        return sb.toString();
    }


    /**
     * Looks for cached items that have a passed expiration date and purge them.
     */
    private void purgeExpiredItems() {
        long now = System.currentTimeMillis();
        // No need to go any further if eldestExpirationDate is in the future.
        // Also, since iterating on the values is an expensive operation (especially for LinkedHashMap),
        // wait PURGE_EXPIRED_DELAY between two purges, unless cache is full
        if (this.eldestExpirationDate > now || (cacheMap.size()<capacity && now-lastExpiredPurge<PURGE_EXPIRED_DELAY)) {
            return;
        }

        // Look for expired items and remove them and recalculate eldestExpirationDate for next time
        this.eldestExpirationDate = Long.MAX_VALUE;
        Long expirationDateL;
        long expirationDate;
        Iterator<Value<V>> iterator = cacheMap.values().iterator();
        // Iterate on all cached values
        while (iterator.hasNext()) {
            expirationDateL = iterator.next().expiration;
			
            // No expiration date for this value
            if (expirationDateL == null) {
                continue;
            }

            expirationDate = expirationDateL;
            // Test if the item has an expiration date and check if has passed
            if (expirationDate < now) {
                // Remove expired item
                iterator.remove();
            } else if(expirationDate < this.eldestExpirationDate) {
                // update eldestExpirationDate
                this.eldestExpirationDate = expirationDate;
            }
        }
		
        // Set last purge timestamp to now
        lastExpiredPurge = now;
    }


    /////////////////////////////////////
    // LRUCache methods implementation //
    /////////////////////////////////////	

    @Override
    public synchronized V get(K key) {
        // Look for expired items and purge them (if any)
        purgeExpiredItems();	

        // Look for a value correponding to the specified key in the cache map
        Value<V> value = cacheMap.get(key);

        if (value == null) {
            // No value matching key, better luck next time!
            if (UPDATE_CACHE_COUNTERS) {
                nbMisses++;	// Increase cache miss counter
            }
            return null;
        }

        // Since expired items purge is not performed on every call to this method for
        // performance reason, we can end with an expired cached value so we need
        // to check this
        if (value.expiration != null && System.currentTimeMillis() > value.expiration) {
            // Value has expired, let's remove it
            if (UPDATE_CACHE_COUNTERS) {
                nbMisses++;    // Increase cache miss counter
            }
            cacheMap.remove(key);
            return null;
        }
			

        if (UPDATE_CACHE_COUNTERS) {
            nbHits++;    // Increase cache hit counter
        }

        return value.val;
    }

	
    @Override
    public synchronized void add(K key, V value, long timeToLive) {
        // Look for expired items and purge them (if any)
        purgeExpiredItems();	

        Long expirationDateL;
        if (timeToLive == -1) {
            expirationDateL = null;
        } else {
            long expirationDate = System.currentTimeMillis()+timeToLive;
            // Update eledestExpirationDate if new element's expiration date is older
            if (expirationDate<this.eldestExpirationDate) {
                // update eldestExpirationDate
                this.eldestExpirationDate = expirationDate;
            }
            expirationDateL = expirationDate;
        }

        cacheMap.put(key, new Value<>(value, expirationDateL));
    }


    @Override
    public synchronized int size() {
        return cacheMap.size();
    }

	
    @Override
    public synchronized void clearAll() {
        cacheMap.clear();
        eldestExpirationDate = Long.MAX_VALUE;
    }
	
	
    //////////////////
    // Test methods //
    //////////////////

    /**
     * Tests this LRUCache for corruption and throws a RuntimeException if something is wrong.
     */
    @Override
    protected void testCorruption() throws RuntimeException {
        for (K key : cacheMap.keySet()) {
            Value<V> value = cacheMap.get(key);
            if (value == null) {
                throw new RuntimeException("cache corrupted: value could not be found for key="+key);
            }

            if (value.expiration == null) {
                continue;
            }
			
            Long expirationDate = value.expiration;
            if (expirationDate < eldestExpirationDate) {
                throw new RuntimeException("cache corrupted: expiration date for key="+key+" older than eldestExpirationDate");
            }
        }
    }
	
}
