package other;

public class nextPermutation {
}
class Solution1 {
    public void nextPermutation(int[] nums) {
        int i = 0;
        if(nums.length == 1) return;
        for(i = nums.length - 2; i >= 0; i--){
            if(nums[i] < nums[i + 1]) break;
        }
        if(i >= 0){
            int j  = nums.length - 1;
            while(j > i && nums[j] <= nums[i]){
                j--;
            }
            swap(nums,i,j);
        }
        reverse(nums,i+1,nums.length-1);
    }
    public void reverse(int[] nums, int l, int r){
        while(l < r){
            swap(nums,l,r);
            l++;
            r--;
        }
    }
    public void swap(int[] nums,int i, int j){
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }
}
