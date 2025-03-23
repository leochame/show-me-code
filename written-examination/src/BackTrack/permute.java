package BackTrack;
import java.util.*;

public class permute {
}
class Solution {
    List<List<Integer>> ans = new ArrayList<>();
    public List<List<Integer>> permute(int[] nums) {
        boolean[] flags = new boolean[nums.length];
        dfs(nums,0,new ArrayList<>(),flags);
        return ans;
    }
    public void dfs(int[] nums,int i, List<Integer> list,boolean[] flags){

        if(i == nums.length) ans.add(new ArrayList<>(list));

        for(int j = 0; j < flags.length; j++){
            if(flags[j]) continue;
            flags[j] = true;
            list.add(nums[j]);
            dfs(nums,i+1,list,flags);
            flags[j] = false;
            list.remove(list.size()-1);
        }
    }
}