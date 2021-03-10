package com.lcj.leetcode.summary;

import com.lcj.leetcode.链表.ListNode;

import java.util.Arrays;
import java.util.PriorityQueue;

/**
 * @author wangguowei
 * @date 2020/12/30 16:31
 */
public class SortTest {

    public static  int[][] merge(int[][] nums) {
        Arrays.sort(nums, (o1, o2)->{
            if(o1[0] > o2[0]) {
                return 1;    // 正序，从小到大
            } else if(o1[0] < o2[0]) {
                return -1;
            } else {
                return 0;
            }
        });
        int index = -1;
        for (int i=0;i<nums.length;i++) {
            int[] current = nums[i];
            if(index == -1 || current[0] > nums[index][1]) {
                nums[++index] = current;
            } else {
                nums[index] = new int[] {nums[index][0], Math.max(nums[index][1], current[1])};
            }
        }
        return Arrays.copyOf(nums, index+1);
    }

    public ListNode mergeKLists(ListNode[] lists) {
        if(lists==null || lists.length == 0) {
            return null;
        }
        PriorityQueue<ListNode> queue = new PriorityQueue<ListNode>(lists.length, ((o1, o2) -> {
            if(o1 != null && o2 != null) {
                return o1.value > o2.value?1:-1;
            }
            return 0;
        }));
        for(int i=0;i<lists.length;i++) {
            if(lists[i] != null) {
                queue.offer(lists[i]);
            }
        }
        ListNode newNode = new ListNode(0);
        ListNode now = newNode;
        while (!queue.isEmpty()) {
            ListNode node = queue.poll();
            ListNode next = node.next;
            if(next != null) {
                queue.offer(next);
            }
            now.next = node;
            now = node;
        }
        return newNode.next;
    }

    public static void quickSort(int[] nums, int left, int right) {
        if(left < right) {
            int pivot = getPrivot(nums, left , right);
            quickSort(nums, left, pivot-1);
            quickSort(nums, pivot+1, right);
        }
    }

    private static int getPrivot(int[] nums, int left, int right) {
        int pivot = nums[left];
        while (left < right) {
            while (left < right && nums[right] >= pivot) { // 从右至左找到第一个比pivot小的元素替换原来pivot对应位置的元素，此时该位置元素移到原来pivot位置，那么现在该位置元素可以看作空
                right--;
            }
            nums[left] = nums[right];
            while (left < right && nums[left] <= pivot) { // 从左至右找到第一个比pivot大的元素替换pivot右侧"空位置"的那个元素
                left++;
            }
            nums[right] = nums[left];
        }
        nums[left] = pivot;
        return left;
    }

    public static void mergeSort(int[] nums, int left, int right) {
        if(left < right) {
            int middle = left + (right - left)/2;
            mergeSort(nums, left, middle);
            mergeSort(nums, middle+1, right);
            merge(nums, left, middle, right);
        }
    }

    private static void merge(int[] nums, int left, int middle, int right) {
        int[] temp = new int[right-left+1];
        int i = left;
        int j = middle + 1;
        int k=0;
        while (i <= middle && j <= right) {
            if(nums[i] < nums[j]) {
                temp[k++] = nums[i++];
            } else {
                temp[k++] = nums[j++];
            }
        }
        while (i <= middle) {
            temp[k++] = nums[i++];
        }
        while (j <= right) {
            temp[k++] = nums[j++];
        }
        for(int z=0;z<temp.length;z++) {
            nums[z+left] = temp[z];
        }
    }

    // word1到word2的转化所需要的步数
    public static int minDistance(String word1, String word2) {
        int[][] dp = new int[word1.length()+1][word2.length()+1];
        for(int i=0;i<=word1.length();i++) {
            dp[i][0] = i;
        }
        for(int j=0;j<=word2.length();j++) {
            dp[0][j] = j;
        }
        for(int i=1;i<=word1.length();i++) {
            for (int j=1;j<=word2.length();j++) {
                if(word1.charAt(i-1) == word2.charAt(j-1)) {
                    dp[i][j] = dp[i-1][j-1];
                } else {
                    dp[i][j] = Math.min(Math.min(dp[i-1][j-1], dp[i][j-1]), dp[i-1][j])+1;
                }
            }
        }
        return dp[word1.length()][word2.length()];
    }

    public static void sortColors(int[] nums) {
        int len = nums.length;
        if(len < 3) {
            return;
        }
        int one = 0;
        int two = len;
        int i = 0;
        while (i < two) {
            if(nums[i] == 0) {
                swap(nums, one, i);
                i++;
                one++;
            } else if(nums[i] == 1){
                i++;
            } else {
                two--;
                swap(nums, two, i);
            }
        }
    }

    private static void swap(int[] nums, int index1 , int index2) {
        int temp = nums[index1];
        nums[index1] = nums[index2];
        nums[index2] = temp;
    }

    public static void main(String[] args) {
       int[] nums = {0,1,2,1,0,1,2,0,1,1,2};
       sortColors(nums);
    }
}
