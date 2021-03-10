package com.lcj.leetcode.LFU;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author wangguowei
 * @date 2020/11/23 10:29
 */
class LFUCache {

    private Map<Integer,Integer> keyToValueMap;  // key到value的映射

    private Map<Integer,Integer> freMap;   // key到频次的映射

    private Map<Integer, LinkedList<Integer>>  freToKeyMap;  // 频次到key集合的映射

    private int capacity;

    public LFUCache(int capacity) {
        this.capacity = capacity;
        keyToValueMap = new HashMap<>((int)(this.capacity/0.75f+1));
        freMap = new HashMap<>((int)(this.capacity/0.75f+1));
        freToKeyMap = new HashMap<>((int)(this.capacity/0.75f+1));
    }

    public int get(int key) {
        Integer value = keyToValueMap.get(key);
        if(value == null) {
            return -1;
        }
        Integer freCount = freMap.get(key);
        LinkedList<Integer> OldValues = freToKeyMap.get(freCount);
        OldValues.remove(Integer.valueOf(key));
        if(OldValues != null && OldValues.size()>0) {
            freToKeyMap.put(freCount, OldValues);
        } else {
            freToKeyMap.remove(freCount);
        }
        freCount++;
        LinkedList<Integer> newValues = freToKeyMap.get(freCount);
        if(newValues == null) {
            newValues = new LinkedList<>();
        }
        newValues.addFirst(key);
        freToKeyMap.put(freCount, newValues);
        freMap.put(key, freCount);
        return value;
    }

    public void put(int key, int value) {
        Integer oldValue = keyToValueMap.get(key);
        if(oldValue != null) {
            keyToValueMap.put(key, value);
            Integer oldFre = freMap.get(key);
            LinkedList<Integer> OldValues = freToKeyMap.get(oldFre);
            OldValues.remove(Integer.valueOf(key));
            if(OldValues != null && OldValues.size()>0) {
                freToKeyMap.put(oldFre, OldValues);
            } else {
                freToKeyMap.remove(oldFre);
            }
            oldFre++;
            freMap.put(key, oldFre);
            LinkedList<Integer> newValues = freToKeyMap.get(oldFre);
            if(newValues == null) {
                newValues = new LinkedList<>();
            }
            newValues.addFirst(key);
            freToKeyMap.put(oldFre, newValues);
        } else {
            if(this.capacity > keyToValueMap.size()) {
                keyToValueMap.put(key, value);
                freMap.put(key, 1);
                LinkedList<Integer> OldValues = freToKeyMap.get(1);
                if(OldValues == null) {
                    OldValues = new LinkedList<>();
                }
                OldValues.addFirst(key);
                freToKeyMap.put(1, OldValues);
            } else {
                // 移除老的元素
                Integer minFre = freToKeyMap.keySet().stream().min(Integer::compareTo).get();
                LinkedList<Integer> values = freToKeyMap.get(minFre);
                Integer oldKey = values.getLast();
                values.removeLast();
                if(values != null && values.size()>0) {
                    freToKeyMap.put(minFre, values);
                } else {
                    freToKeyMap.remove(minFre);
                }
                freMap.remove(oldKey);
                keyToValueMap.remove(oldKey);

                // 加入新元素
                keyToValueMap.put(key, value);
                freMap.put(key, 1);
                LinkedList<Integer> OldValues = freToKeyMap.get(1);
                if(OldValues == null) {
                    OldValues = new LinkedList<>();
                }
                OldValues.addFirst(key);
                freToKeyMap.put(1, OldValues);
            }
        }
    }


    public static void main(String[] args) {
        LFUCache cache = new LFUCache(4);
        cache.put(1,1);
        cache.put(2,2);
        cache.put(3,3);
        cache.put(4,4);
        cache.put(1,5);
        cache.get(3);
        cache.put(6,6);
    }
}
