package test;

import java.util.Arrays;

public class Test {
    public static void main(String[] args) {
        int[] nums = {9,7,6,4,3,3,3,2,2,1,5,66,7,8,9,9,3,3,4,5};
        quickSort(nums,0,nums.length - 1);
        for (int num : nums) {
            System.out.print(num + "  ");
        }

    }

    public static  void quickSort(int[] nums,int l ,int r){
        if(l >= r) return;
        int i = partition(nums,l,r);
        quickSort(nums,l,i-1);
        quickSort(nums,i+1,r);
    }

    public  static int partition(int[] nums,int l ,int r){
        int left = l;
        int right = r;
        while(left < right){
            while(left < right && nums[right] >= nums[l]) {
                right--;
            }
            while(left < right && nums[left] <= nums[l]){
                left++;
            }
            swap(nums,left,right);
        }
        swap(nums,l,left);
        return left;
    }

    public static void swap(int[] nums, int l ,int r){
        int temp = nums[l];
        nums[l] = nums[r];
        nums[r] = temp;
    }
    public class Node{

    }
}
