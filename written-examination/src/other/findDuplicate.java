package other;

public class findDuplicate {
    static class Solution {
        public int findDuplicate(int[] nums) {
            int slow = 0;
            int fast = 0;
            slow = nums[slow];
            fast = nums[nums[fast]];
            while(slow != fast){
                slow = nums[slow];
                fast = nums[nums[fast]];
            }
            int pre1 = slow;
            int pre2 = 0;
            while(nums[pre1] != nums[pre2]){
                pre1 = nums[pre1];
                pre2 = nums[pre2];
            }
            return nums[pre1];
        }
    }
}
