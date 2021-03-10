package com.lcj.leetcode.summary;

/**
 * [1,3,5,4,1]
 * @author wangguowei
 * @date 2021/2/2 15:27
 */
public class ArraysTest {

    public static void main(String[] args) {
        int[] nums = {1,3,5,4,1};
        new ArraysTest().adjust(nums);
        System.out.println(nums);
    }

    public void adjust(int[] nums) {
        int len = nums.length;
        int indexA = len-2;
        while (indexA > 0) {
            if(nums[indexA] < nums[indexA+1]) {
                break;
            }
            indexA--;
        }
        if(indexA == -1) {
            reverse(nums, 0 ,len-1);
        } else {
            int indexB = indexA+1;
            while (indexB < len) {
                if(nums[indexB] > nums[indexA]) {
                    indexB++;
                } else {
                    indexB--;
                    break;
                }
            }
            swap(nums, indexA, indexB);
            reverse(nums, indexA+1, len-1);
        }
    }

    private void reverse(int[] nums, int a, int b) {
        while (a<b) {
            swap(nums, a, b);
            a++;
            b--;
        }
    }

    private void swap(int[] nums, int a, int b) {
        int temp = nums[a];
        nums[a] = nums[b];
        nums[b] = temp;
    }
}
