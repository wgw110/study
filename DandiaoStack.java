package com.lcj.leetcode.summary;

import java.util.LinkedList;
import java.util.Stack;

/**
 * 单调栈
 * @author wangguowei
 * @date 2020/12/29 19:16
 */
public class DandiaoStack {

    // 单调递增栈  从栈底到栈顶是单调递增的
    public static void dandiaoIncrease(int[] nums) {
        Stack<Integer> stack = new Stack<>();
        for (int i=0;i<nums.length;i++) {
            while (!stack.isEmpty() && nums[i] <= stack.peek()) {  // 如果当前要入栈的元素比栈顶元素小，那么原来的栈顶元素出栈
                Integer a =stack.pop();
                System.out.println("需要出栈："+a);
            }
            stack.push(nums[i]);
        }
        while (!stack.isEmpty()) {
            System.out.print(stack.pop()+",");
        }
        System.out.println();
    }

    // 单调递减栈  从栈底到栈顶是单调递减的
    public static void dandiaoDecrease(int[] nums) {
        Stack<Integer> stack = new Stack<>();
        for (int i=0;i<nums.length;i++) {
            while (!stack.isEmpty() && nums[i] >= stack.peek()) { // 如果当前要入栈的元素比栈顶元素大，那么原来的栈顶元素出栈
                Integer a =stack.pop();
                System.out.println("需要出栈："+a);
            }
            stack.push(nums[i]);
        }
        while (!stack.isEmpty()) {
            System.out.print(stack.pop()+",");
        }
        System.out.println();
    }

    // [1,1,3,4,3,4]
    public static int[] nextGreatNum(int[] nums) {  // 下一个最大的元素
        int[] z = new int[nums.length];
        Stack<Integer> stack = new Stack<>();
        for (int i=nums.length-1;i>=0;i--) {
            while (!stack.isEmpty() && nums[i] >= stack.peek()) {
                stack.pop();
            }
            z[i] = stack.isEmpty()?-1:stack.peek();
            stack.push(nums[i]);
        }
        return z;
    }

    public static int[] nextGreatNumIndex(int[] nums) {  // 下一个最大元素的下标
        int[] z = new int[nums.length];
        Stack<Integer> stack = new Stack<>();
        for (int i=nums.length-1;i>=0;i--) {
            while (!stack.isEmpty() && nums[i] >= nums[stack.peek()]) {
                stack.pop();
            }
            z[i] = stack.isEmpty()?-1:stack.peek();
            stack.push(i);
        }
        return z;
    }

    public static int[] nextGreatNumDistance(int[] nums) {  // 下一个最大元素的下标距离当前元素的下标的距离
        int[] z = new int[nums.length];
        Stack<Integer> stack = new Stack<>();
        for (int i=nums.length-1;i>=0;i--) {
            while (!stack.isEmpty() && nums[i] >= nums[stack.peek()]) {
                stack.pop();
            }
            z[i] = stack.isEmpty()?-1:stack.peek()-i;
            stack.push(i);
        }
        return z;
    }

    // 接雨水
    public static int maxArea(int[] nums) {
        int ans = 0;
        for(int i=1;i<nums.length-1;i++) {
            int maxLeft = 0;
            for(int j=maxLeft-1;j>=0;j--) {
                maxLeft = Math.max(maxLeft, nums[j]);
            }
            int maxRight = 0;
            for(int k=maxLeft+1;k<nums.length;k++) {
                maxRight = Math.max(maxRight, nums[k]);
            }
            int minHeigh = Math.min(maxLeft, maxRight);
            if(minHeigh > nums[i]) {
                ans = minHeigh - nums[i];
            }
        }
        return ans;
    }

    // 接雨水2
    public static int maxArea2(int[] nums) {
        int ans = 0;
        int left = 0;
        int right = nums.length -1;
        int maxLeft = 0;   // 记录当前下标左侧最大值
        int maxRight = 0;  // 记录当前下标右侧最大值
        while (left < right) {
            if(nums[left] < nums[right]) {
                if (nums[left] >= maxLeft) {
                    maxLeft = nums[left];
                } else {
                    ans = (ans + maxLeft - nums[left]);
                }
                left++;
            } else {   // 左侧元素比右侧元素大
                if(nums[right] >= maxRight) {  // 如果当前元素的高度比右侧最大值大，那么以当前元素作为最大的maxRight
                    maxRight = nums[right];
                } else {
                    ans = ans + maxRight - nums[right];
                }
                right--;
            }
        }
        return ans;
    }

    // 接雨水
    public static int maxArea3(int[] nums) {
        int ans = 0;
        Stack<Integer> stack = new Stack<>();
        for(int i=0;i<nums.length;i++) {
            while (!stack.isEmpty() && nums[i] > nums[stack.peek()]) {
                int top = stack.peek();
                stack.pop();
                if(stack.isEmpty()) {
                    break;
                }
                int distance = i - stack.peek() - 1;
                int high = Math.min(nums[stack.peek()], nums[i]) - nums[top];
                ans += distance*high;
                System.out.println("ans="+ans);
            }
            stack.push(i);
        }
        return ans;
    }

    public static int[] maxItemInArray(int[] nums, int k) {
        int len = nums.length;
        int[] result=  new int[len-k+1];
        LinkedList<Integer> queue = new LinkedList<>();
        for(int i=0;i<len;i++) {
            while (!queue.isEmpty() && nums[i] >= nums[queue.peekLast()]) {
                queue.pollLast();
            }

            queue.addLast(i);

            if(queue.peek() +k <= i) {  // 判断队首元素是否需要删除
                queue.poll();
            }

            if(i+1>=k) {
                result[i-k+1] = nums[queue.peek()];
            }
        }
        return result;
    }

    public static void main(String[] args) {
        int[] nums = {0,1,0,2,1,0,1,3,2,1,2,1};
        int[] result = maxItemInArray(nums, 3);
        System.out.println(result);

     }
}
