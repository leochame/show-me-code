package other;

public class sortColors {
    class Solution_v1 {
        public void sortColors(int[] nums) {
            int l = 0;
            int r = nums.length - 1;
            /**
             * i 到 l 是一定没有0存在的。l 在的位置要不然和 i 重叠，要不然就是非 0 。
             * 0 0 0
             * 1 0 1
             * 0 1 0 1
             */
            for(int i = 0; i <= r; i++){
                if(nums[i] == 0){
                    swap(nums,i,l);
                    l++;
                }else if(nums[i] == 2){
                    swap(nums,i,r);
                    r--;
                    i--;
                }
            }
        }
        public void swap(int[] nums, int i, int j){
            int temp = nums[i];
            nums[i] = nums[j];
            nums[j] = temp;
        }
    }
    class Solution2 {
        public void sortColors(int[] nums) {
            int l = 0;
            int r = nums.length - 1;
            int i = 0;
            while(i <= r){
                if(nums[i] == 0){
                    swap(nums,i,l);
                    l++;
                    i++;
                }else if(nums[i] == 2){
                    swap(nums,i,r);
                    r--;
                }else{
                    i++;
                }
            }
        }
        public void swap(int[] nums, int i ,int j){
            int temp = nums[i];
            nums[i] = nums[j];
            nums[j] = temp;
        }
    }
}
