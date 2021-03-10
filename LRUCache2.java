package com.lcj.leetcode.LRU;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author wangguowei
 * @date 2020/11/20 11:29
 */
public class LRUCache2<K,V> {

    private int capacity;

    private Map<K,V> map;

    private LinkedList<K> list;   // 记录key值

    LRUCache2(int capacity) {
        this.capacity = capacity;
        this.map = new HashMap<>((int) (capacity / 0.75 + 1));
        this.list = new LinkedList<>();
    }


    public V get(K key) {
        if(list.contains(key)) {
            V value = map.get(key);
            boolean judge =list.remove(key);
            if(judge) {
                list.addFirst(key);
                return value;
            }
        }
        return null;
    }

    public void put(K key, V value) {
        if(list.contains(key)) {
            boolean judge =list.remove(key);
            if(judge) {
                list.addFirst(key);
                map.put(key,value);
            }
        } else {
            if(list.size() >= capacity) {  // 如果List没有key的存在并且容量达到上限则移除链表尾部的元素
                list.removeLast();
            }
            list.addFirst(key);
            map.put(key,value);
        }
    }

    public static void main(String[] args) {
        LRUCache2<Integer,Integer> cache = new LRUCache2<>(4);
        cache.put(1,2);
        cache.put(2,3);
        System.out.println(cache.get(1));
        cache.put(3,4);
        cache.put(4,5);
        cache.put(5,6);
        System.out.println(cache.get(2));
    }
}
