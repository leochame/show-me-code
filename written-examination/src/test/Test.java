package test;

import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        int[] nums = {9,7,6,4,3,3,3,2,2,1,5,6,7,8,9,9,3,3,4,5};
        int[] bucket = new int[10];
        int n = 871;
        for (int num : nums) {
            bucket[num]++;
        }
        int n1 = n;
        List<Integer> list = new ArrayList<>();
        while(n1 > 0){
            list.add( n1%10 );
            n1 = n1 / 10;
        }
        int m = list.size();
        if(m > nums.length){
            System.out.println(generate(bucket,nums.length,0));
            return;
        }
        // ----
        int[] nums1 = new int[list.size()];
        for(int i = 0; i < nums1.length;i++){
            nums1[i] = list.get(list.size() - 1 - i);
        }
        System.out.println(dfs(bucket,0,nums1,0));
    }

    private static int dfs(int[] bucket, int depth, int[] nums1,int ans) {
        if(depth == nums1.length - 1){
            int x = findLower(bucket,nums1[depth]);
            if(x == -1){
                return -1;
            }
            return ans*10 + x;
        }

        if(bucket[nums1[depth]] > 0){
            bucket[nums1[depth]]--;
            int x = dfs(bucket,depth+1,nums1,ans*10 + nums1[depth]);
            if(x != -1) return x;
            bucket[nums1[depth]]++;
        }

        int x = findLower(bucket,nums1[depth]);
        if(x == -1) {
            return -1;
        }
        bucket[x]--;
        return generate(bucket,nums1.length - depth - 1, ans * 10 + x);
    }

    private static int findLower(int[] bucket, int t) {
        for(int i = t - 1; i >= 0; i--){
            if(bucket[i] > 0){
                return i;
            }
        }
        return -1;
    }

    public static int generate(int[] buc, int m1, int re){
        for(int i = 9; i >= 0 && m1 > 0;){
            if(buc[i] == 0){
                i--;
            }else{
                buc[i]--;
                m1--;
                re = re*10 + i;
            }
        }
        return re;
    }
}
