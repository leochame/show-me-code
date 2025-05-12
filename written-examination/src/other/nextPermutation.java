package other;

public class nextPermutation {
    static class Solution {
        public void nextPermutation(int[] nums) {
            int i = 0;
            for(i = nums.length - 2; i >= 0;i--){
                if(nums[i] < nums[i+1]) break;
            }
            /**
             * 如果并非 321 这种最大的字典序，那么：
             * 找到倒数的第一个大于 nums[i] 的的索引位置
             */
            if(i >= 0){
                int j = nums.length - 1;
                for(; j >= i; j--){
                    if(nums[j] > nums[i]) break;
                }
                swap(nums,i,j);
            }
            /**
             * 其实在上一步骤中，就已经保证了 [i+1] ～ [nums.length - 1] 的单调递增的特性。
             * 一开始是用 “if(nums[i] < nums[i+1]) break;” 这段代码保证。这里好理解
             * 之后，又是通过“ if(nums[j] > nums[i]) break;”保证了，替换的nums[i] 一定大于 j 之后的数字，仍然保持单调
             */
            reverse(nums,i+1,nums.length - 1);
        }
        public void reverse(int[] nums, int l , int r){
            while(l < r){
                swap(nums,l,r);
                l++;
                r--;
            }
        }
        public void swap(int[] nums, int i , int j){
            int temp = nums[i];
            nums[i] = nums[j];
            nums[j] = temp;
        }
    }
}
class nextPermutation2 {
    static class Solution {
        public void nextPermutation(int[] nums) {
            int n = nums.length;
            if( n == 1){
                return;
            }
            int i = n - 2;
            while(i >= 0 && nums[i] >= nums[i+1]){
                i--;
            }
            if(i >= 0){
                int j = n-1;
                while(i <= j && nums[i] >= nums[j]){
                    j--;
                }
                swap(nums,j,i);
            }
            reverse(nums,i+1,n-1);
        }
        public void reverse(int[] nums, int l, int r){
            while(l < r){
                swap(nums,l,r);
                l++;
                r--;
            }
        }
        public void swap(int[] nums, int i, int j){
            int temp = nums[i];
            nums[i] = nums[j];
            nums[j] = temp;
        }
    }
}
