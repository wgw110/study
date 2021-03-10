package com.lcj.leetcode.summary;

import com.atlassian.guava.collect.Maps;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wangguowei
 * @date 2020/12/28 20:43
 */
public class Window {

    // 最长不重复子串
    public static int lengthOfLongestSubstring(String s) {
        int maxLengh = 0;
        Map<Character,Integer> window = new HashMap<>();
        int left = 0;
        int right = 0;
        Integer count = null;
        while (right < s.length()) {
            char c = s.charAt(right);
            count = window.get(c);
            if(count == null) {
                window.put(c, 1);
            } else {
                window.put(c, count+1);
            }
            right++;
            while (window.get(c) > 1) {
                maxLengh = Math.max(maxLengh, right-left-1);
                char d = s.charAt(left);
                count = window.get(d);
                if(count != null) {
                    if(count == 1) {
                        window.remove(d);
                    } else {
                        window.put(d, count-1);
                    }
                }
                left++;
            }
        }
        return maxLengh;
    }

    // 最长不重复子串
    public static int lengthOfLongestSubstring2(String s) {
        int maxLengh = Integer.MIN_VALUE;
        Map<Character,Integer> map = new HashMap<>();
        int left = 0;
        for(int i=0;i<s.length();i++) {
            if(map.containsKey(s.charAt(i))) {
                left = Math.max(left, map.get(s.charAt(i))+1);
            }
            maxLengh = Math.max(maxLengh, i-left+1);
            map.put(s.charAt(i), i);
        }
        return maxLengh;
    }

    public void mergeSort(int[] m, int[] n) {
        int i = m.length-1;
        int j = n.length-1;
        int k = i+j+1;
        while (i>=0 && j>= 0) {
            if(m[i] > m[j]) {
                m[k--] = m[i--];
            } else {
                m[k--] = n[j--];
            }
        }
        while (j>=0) {
            m[k--] = n[j--];
        }
    }


    // s中包含t的最小字串  s = "ADOBECODEBANC", t = "ABC"
    public static String minWindow(String s, String t) {
        Map<Character,Integer> source = new HashMap<>();
        for(int i =0;i<t.length();i++) {
            Integer count = source.get(t.charAt(i));
            source.put(t.charAt(i), count == null?1:count+1);
        }
        Map<Character,Integer> window = new HashMap<>();
        int left = 0;
        int right = 0;
        int zleft = 0;
        Integer count = null;
        Integer minLength = Integer.MAX_VALUE;
        while (right < s.length()) {
            char c = s.charAt(right);
            if(t.indexOf(c)>=0) {
                count = window.get(c);
                window.put(c, (count == null)?1:(count+1));
            }
            right++;
            while (needShrink(window, source)) {
                if(right - left < minLength) {
                    zleft = left;
                    minLength = right - left;
                }
                char d = s.charAt(left);
                count = window.get(d);
                if(count != null) {
                    if (count == 1) {
                        window.remove(d);
                    } else {
                        window.put(d, count -1);
                    }
                }
                left++;
            }
        }
        if(minLength < Integer.MAX_VALUE) {
            return s.substring(zleft, zleft+minLength);
        } else {
            return "";
        }
    }

    public static String minWindow2(String s, String t) {
        Map<Character,Integer> source = new HashMap<>();
        Map<Character,Integer> window = new HashMap<>();
        for(int i =0;i<t.length();i++) {
            source.put(t.charAt(i), source.getOrDefault(t.charAt(i), 0)+1);
        }
        int left = 0;
        int right = 0;
        int start = 0;
        int valid = 0;
        int len = Integer.MAX_VALUE;
        while (right < s.length()) {
            char c = s.charAt(right);
            right++;
            if(source.containsKey(c)) {
                window.put(c, window.getOrDefault(c, 0)+1);
                if(window.get(c) == source.get(c)) {
                    valid++;
                }
            }
            while (valid == source.size()) {
                if(right - left < len) {
                    start = left;
                    len = right-left;
                }
                char d = s.charAt(left);
                left++;
                if(source.containsKey(d)) {
                    if(window.get(d) == source.get(d)) {
                        valid--;
                    }
                    window.put(d, window.get(d) - 1);
                }
            }
        }
        return len == Integer.MAX_VALUE?"":s.substring(start, start+len);
    }

    private static boolean needShrink(Map<Character, Integer> window, Map<Character,Integer> source) {
        for(Map.Entry<Character,Integer> entry : source.entrySet()) {
            Character key = entry.getKey();
            Integer value = entry.getValue();
            if(!window.containsKey(key) || window.get(key)<(value)) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        Map<Character,Integer> map = Maps.newHashMap();
        String s = "ADOBECODEBANC";
        System.out.println(minWindow2(s, "ABC"));
        char[] chars = s.toCharArray();
        for(int i=0;i<chars.length;i++) {
            map.put(chars[i], i);
        }
        System.out.println(s.length());
        System.out.println(s.toCharArray());
       // minWindow("ADOBECODEBANC", "ABC");
    }

}
