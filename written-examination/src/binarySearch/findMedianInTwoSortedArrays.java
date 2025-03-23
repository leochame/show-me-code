package binarySearch;

public class findMedianInTwoSortedArrays {
}

/**
 * 这个问题解答的关键：
 *  1. 注意二分时候进行移动的判断判断条件
 *  因为【 a[i] <= b[j + 1] && a[i+1]>b[j] 】是唯一的，只有一个位置符合。
 *  我们用二分，并且条件为【a[i] <= b[j + 1]】去寻找那个【最大】位置的，
 *  自然就找到了符合【a[i] <= b[j + 1] && a[i+1]>b[j]】的
 */
class Solution {
    public double findMedianSortedArrays(int[] nums1, int[] nums2) {
        if(nums1.length > nums2.length){
            return findMedianSortedArrays(nums2,nums1);
        }
        int m = nums1.length;
        int n = nums2.length;
        int left = -1;
        int right = m;

        while(left + 1 < right){
            int i = (left + right)/2;
            int j = (m + n +1)/2 - 2 - i;
            // nums1 中的位置 小于 nums2 中的位置
            if(nums1[i] <= nums2[j+1]){
                left = i;
            }else{
                right = i;
            }
        }


        int i = left;
        int j = (m + n +1)/2 - 2 - i;

        int small1 = i > -1 ? nums1[i] : Integer.MIN_VALUE;
        int small2 = j > -1 ? nums2[j] : Integer.MIN_VALUE;
        int big1 = i < m - 1 ? nums1[i+1] : Integer.MAX_VALUE;
        int big2 = j < n - 1 ? nums2[j+1] : Integer.MAX_VALUE;

        int small = Math.max(small1,small2);
        int big = Math.min(big1,big2);

        return (m+n)%2 == 1 ? small : (small + big)/2.0;
    }
}