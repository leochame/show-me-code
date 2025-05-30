package sort;

import java.util.Random;

public class QuickSort_random {
    static void quickSort(int[] nums, int left, int right) {
        // 子数组长度为 1 时终止递归
        if (left >= right)
            return;
        // 哨兵划分
        int pivot = partition(nums, left, right);
        // 递归左子数组、右子数组
        quickSort(nums, left, pivot - 1);
        quickSort(nums, pivot + 1, right);
    }
    /* 元素交换 */
    static void swap(int[] nums, int i, int j) {
        int tmp = nums[i];
        nums[i] = nums[j];
        nums[j] = tmp;
    }

    /* 哨兵划分 */
    static int partition(int[] nums, int left, int right) {
        int pivotIndex = new Random().nextInt(right - left + 1) + left;
        /// 🌟 这里值得注意的是 ： 一定要把基准数交换到最左边
        swap(nums, left, pivotIndex);
        int pivot = nums[left];
        int i = left, j = right;

        while (i < j) {
            while (i < j && nums[j] >= pivot)
                j--;
            while (i < j && nums[i] <= pivot)
                i++;
            swap(nums, i, j);
        }
        swap(nums, left, i); // 把基准数放到最终位置
        return i;
    }

}
