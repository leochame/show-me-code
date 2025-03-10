package com.adam.二分.寻找正序数组的中位数;

class Solution {
    public double findMedianSortedArrays(int[] nums1, int[] nums2) {
        int n = nums1.length;
        int m = nums2.length;
        if(n > m){ return findMedianSortedArrays(nums2,nums1);}

        int l = -1;
        int r = n;
        while(l + 1 < r){
            int i = (l + r) / 2;
            int j = (m + n + 1) / 2 -2 -i;
            if(nums1[i] <= nums2[j + 1]){
                l = i;
            }else{
                r = i;
            }
        }

        int i = l;
        int j = (m + n + 1)/2 -2 -i;
        int mini = i < 0 ? Integer.MIN_VALUE : nums1[i];
        int minj = j < 0 ? Integer.MIN_VALUE : nums2[j];
        int maxi = i + 1 >= n ? Integer.MAX_VALUE : nums1[i + 1];
        int maxj = j + 1 >= m ? Integer.MAX_VALUE : nums2[j + 1];
        int min = Math.max(mini,minj);
        int max = Math.min(maxi,maxj);
        return (m + n) % 2 == 1 ? min : (min+max)/2.0;
    }
}

class Solution1 {
    public double findMedianSortedArrays(int[] nums1, int[] nums2) {
        int totalLength = nums1.length + nums2.length;
        if (totalLength % 2 == 1) {
            return findKth(nums1, 0, nums2, 0, (totalLength + 1) / 2);
        } else {
            int left = findKth(nums1, 0, nums2, 0, totalLength / 2);
            int right = findKth(nums1, 0, nums2, 0, totalLength / 2 + 1);
            return (left + right) / 2.0;
        }
    }

    private int findKth(int[] nums1, int i, int[] nums2, int j, int k) {
        if (i >= nums1.length) {
            return nums2[j + k - 1];
        }
        if (j >= nums2.length) {
            return nums1[i + k - 1];
        }
        if (k == 1) {
            return Math.min(nums1[i], nums2[j]);
        }

        int mid1 = i + k / 2 - 1;
        int mid2 = j + k / 2 - 1;

        int val1 = (mid1 < nums1.length) ? nums1[mid1] : Integer.MAX_VALUE;
        int val2 = (mid2 < nums2.length) ? nums2[mid2] : Integer.MAX_VALUE;

        if (val1 <= val2) {
            return findKth(nums1, mid1 + 1, nums2, j, k - k / 2);
        } else {
            return findKth(nums1, i, nums2, mid2 + 1, k - k / 2);
        }
    }
}