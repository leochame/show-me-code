package com.adam.sort;

import java.util.Arrays;
import java.util.Random;

/**
 * 快速排序实现
 * 
 * <h3>核心思想：分治法</h3>
 * <ol>
 *   <li>选择一个基准元素（pivot）</li>
 *   <li>将数组分为两部分：小于基准的放在左边，大于等于基准的放在右边</li>
 *   <li>递归处理左右两部分</li>
 * </ol>
 * 
 * <h3>算法流程：</h3>
 * <pre>
 * 1. 选择基准：通常选择最左边或最右边的元素
 * 2. 分区操作：使用双指针将数组分为两部分
 * 3. 递归排序：对左右两部分分别递归调用快速排序
 * </pre>
 * 
 * <h3>时间复杂度：</h3>
 * <ul>
 *   <li>平均情况：O(n log n) - 每次分区都能将数组大致分为两半</li>
 *   <li>最坏情况：O(n²) - 当每次选择的基准都是最大或最小值时（如已排序数组）</li>
 *   <li>最好情况：O(n log n) - 每次分区都能将数组精确分为两半</li>
 * </ul>
 * 
 * <h3>空间复杂度：</h3>
 * <ul>
 *   <li>O(log n) - 递归调用栈的深度，平均情况下为 log n</li>
 *   <li>最坏情况下为 O(n)，当数组完全有序时</li>
 * </ul>
 * 
 * <h3>稳定性：</h3>
 * <p>快速排序是不稳定的排序算法。相同元素的相对位置可能会改变。</p>
 * 
 * <h3>适用场景：</h3>
 * <ul>
 *   <li>适合处理大规模数据，平均性能优秀</li>
 *   <li>不适合处理已排序或接近排序的数据（除非使用随机化版本）</li>
 *   <li>不适合对稳定性有要求的场景</li>
 * </ul>
 * 
 * <h3>优化策略：</h3>
 * <ul>
 *   <li>随机化选择基准：避免最坏情况</li>
 *   <li>三数取中：选择左、中、右三个元素的中位数作为基准</li>
 *   <li>小数组使用插入排序：当子数组长度小于某个阈值时，使用插入排序</li>
 *   <li>三路快排：处理大量重复元素的情况</li>
 * </ul>
 * 
 * @author Adam
 * @version 1.0
 */
public class QuickSort {

    /**
     * 快速排序入口方法（基础版本）
     * 
     * <p>对数组进行原地排序，不返回新数组。</p>
     * 
     * <p><b>使用示例：</b></p>
     * <pre>
     * int[] arr = {64, 34, 25, 12, 22, 11, 90, 5};
     * QuickSort.quickSort(arr);
     * // arr 现在是 [5, 11, 12, 22, 25, 34, 64, 90]
     * </pre>
     * 
     * @param arr 待排序数组，排序后会被修改
     * @throws NullPointerException 如果 arr 为 null
     */
    public static void quickSort(int[] arr) {
        if (arr == null || arr.length <= 1) {
            return;
        }
        quickSort(arr, 0, arr.length - 1);
    }

    /**
     * 快速排序递归方法
     * 
     * <p>对数组的指定区间进行快速排序。</p>
     * 
     * <p><b>算法步骤：</b></p>
     * <ol>
     *   <li>如果 left >= right，直接返回（子数组长度为 0 或 1）</li>
     *   <li>执行分区操作，将数组分为两部分，返回基准的最终位置</li>
     *   <li>递归排序左子数组 [left, pivotIndex - 1]</li>
     *   <li>递归排序右子数组 [pivotIndex + 1, right]</li>
     * </ol>
     * 
     * @param arr 待排序数组
     * @param left 左边界（包含）
     * @param right 右边界（包含）
     */
    private static void quickSort(int[] arr, int left, int right) {
        // 递归终止条件：子数组长度为 0 或 1
        if (left >= right) {
            return;
        }

        // 分区操作，返回基准元素的最终位置
        int pivotIndex = partition(arr, left, right);

        // 递归排序左子数组 [left, pivotIndex - 1]
        quickSort(arr, left, pivotIndex - 1);
        
        // 递归排序右子数组 [pivotIndex + 1, right]
        quickSort(arr, pivotIndex + 1, right);
    }

    /**
     * 分区操作（双指针法）
     * 
     * <p>将数组分为两部分：</p>
     * <ul>
     *   <li>左边：所有小于基准的元素</li>
     *   <li>右边：所有大于等于基准的元素</li>
     * </ul>
     * 
     * <h3>⚠️ 关键点：为什么必须先移动右指针？</h3>
     * 
     * <p><b>原因分析：</b></p>
     * <p>由于我们选择的是最左边的元素作为基准（pivot = arr[left]），
     * 最后需要将基准元素交换到 i 和 j 相遇的位置。
     * 为了保证交换后基准元素在正确的位置，相遇点必须满足：
     * <b>相遇点的元素必须小于等于基准</b>。</p>
     * 
     * <p><b>如果先移动左指针（错误做法）：</b></p>
     * <pre>
     * 示例：数组 [5, 1, 2, 3, 4]，基准 pivot = 5
     * 
     * 初始状态：i=0, j=4
     * [5, 1, 2, 3, 4]
     *  ↑           ↑
     *  i           j
     * 
     * 先移动左指针 i：
     * - i 向右移动，找到第一个 > 5 的元素（找不到，i 停在 j 的位置）
     * - 此时 i=4, j=4，相遇在位置 4
     * - arr[4] = 4 < 5，看起来没问题？
     * 
     * 但考虑另一个例子：[5, 6, 7, 8, 1]
     * - i 向右移动，找到第一个 > 5 的元素：arr[1] = 6
     * - i 停在位置 1，j 从右边移动，找到第一个 < 5 的元素：arr[4] = 1
     * - 交换后：[5, 1, 7, 8, 6]
     * - 继续：i 向右找到 7，j 向左找不到，相遇在位置 2
     * - 此时 arr[2] = 7 > 5
     * - 如果交换 arr[left] 和 arr[i]，会把 7 放到左边，破坏分区！
     * </pre>
     * 
     * <p><b>先移动右指针（正确做法）：</b></p>
     * <pre>
     * 示例：数组 [5, 1, 2, 3, 4]，基准 pivot = 5
     * 
     * 初始状态：i=0, j=4
     * [5, 1, 2, 3, 4]
     *  ↑           ↑
     *  i           j
     * 
     * 先移动右指针 j：
     * - j 向左移动，找到第一个 < 5 的元素：arr[3] = 3
     * - j 停在位置 3
     * - 然后移动左指针 i，找到第一个 > 5 的元素（找不到）
     * - i 向右移动，直到 i == j，相遇在位置 3
     * - 此时 arr[3] = 3 < 5，满足条件！
     * - 交换 arr[left] 和 arr[i]，得到 [3, 1, 2, 5, 4]
     * - 基准 5 被正确放置，左边都 < 5，右边都 >= 5
     * </pre>
     * 
     * <p><b>核心原理：</b></p>
     * <ol>
     *   <li>由于基准在左边，我们需要保证相遇点的元素 <= 基准</li>
     *   <li>先移动右指针，j 会停在 <= 基准的位置（或停在 left 位置）</li>
     *   <li>然后移动左指针，i 会停在 >= 基准的位置，但不会超过 j</li>
     *   <li>当 i == j 时，相遇点一定是 j 最后停下的位置，即 <= 基准的位置</li>
     *   <li>因此可以安全地将基准与 arr[i] 交换</li>
     * </ol>
     * 
     * <p><b>记忆技巧：</b></p>
     * <ul>
     *   <li>基准在左边 → 先移动右指针（从另一边开始）</li>
     *   <li>基准在右边 → 先移动左指针（从另一边开始）</li>
     *   <li>原则：从基准的"对面"开始移动，保证相遇点满足交换条件</li>
     * </ul>
     * 
     * @param arr 数组
     * @param left 左边界
     * @param right 右边界
     * @return 基准元素的最终位置
     */
    private static int partition(int[] arr, int left, int right) {
        // 选择最左边的元素作为基准
        int pivot = arr[left];
        
        // 双指针：i 从左边开始，j 从右边开始
        int i = left;
        int j = right;

        while (i < j) {
            // ⚠️ 关键：必须先移动右指针！
            // 
            // 原因：
            // 1. 基准在左边（arr[left]），最后需要交换到 i 的位置
            // 2. 为了保证交换后基准在正确位置，i 必须指向 <= 基准的元素
            // 3. 先移动右指针 j，j 会停在 <= 基准的位置
            // 4. 然后移动左指针 i，当 i == j 时，相遇点就是 j 停下的位置
            // 5. 这样就能保证 arr[i] <= pivot，可以安全交换
            
            // 从右向左找第一个小于基准的元素
            while (i < j && arr[j] >= pivot) {
                j--;
            }
            
            // 从左向右找第一个大于基准的元素
            while (i < j && arr[i] <= pivot) {
                i++;
            }
            
            // 交换这两个元素
            if (i < j) {
                swap(arr, i, j);
            }
        }
        
        // 将基准元素交换到最终位置（i 和 j 相遇的位置）
        // 此时 i 左边的元素都小于基准，右边的元素都大于等于基准
        // 由于先移动了右指针，可以保证 arr[i] <= pivot，交换是安全的
        swap(arr, left, i);
        
        return i;
    }

    /**
     * 交换数组中两个元素的位置
     * 
     * @param arr 数组
     * @param i 第一个元素的索引
     * @param j 第二个元素的索引
     */
    private static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    // ==================== 随机化版本 ====================

    /**
     * 快速排序入口方法（随机化版本）
     * 
     * <p>通过随机选择基准元素，避免最坏情况 O(n²)。</p>
     * 
     * <p><b>优势：</b></p>
     * <ul>
     *   <li>对于已排序或接近排序的数组，性能显著优于基础版本</li>
     *   <li>平均时间复杂度仍为 O(n log n)，但最坏情况概率大大降低</li>
     *   <li>适合处理未知数据分布的情况</li>
     * </ul>
     * 
     * <p><b>使用场景：</b></p>
     * <ul>
     *   <li>不确定输入数据的排序状态</li>
     *   <li>需要避免最坏情况性能</li>
     *   <li>对性能稳定性有要求</li>
     * </ul>
     * 
     * @param arr 待排序数组，排序后会被修改
     */
    public static void quickSortRandom(int[] arr) {
        if (arr == null || arr.length <= 1) {
            return;
        }
        quickSortRandom(arr, 0, arr.length - 1);
    }

    /**
     * 随机化快速排序递归方法
     */
    private static void quickSortRandom(int[] arr, int left, int right) {
        if (left >= right) {
            return;
        }

        // 随机化分区
        int pivotIndex = partitionRandom(arr, left, right);

        quickSortRandom(arr, left, pivotIndex - 1);
        quickSortRandom(arr, pivotIndex + 1, right);
    }

    /**
     * 随机化分区操作
     * 
     * <p>随机选择基准元素，提高平均性能，避免最坏情况。</p>
     * 
     * <p><b>实现步骤：</b></p>
     * <ol>
     *   <li>在 [left, right] 范围内随机选择一个位置</li>
     *   <li>将随机选择的元素交换到最左边，作为基准</li>
     *   <li>执行与普通分区相同的双指针分区操作</li>
     * </ol>
     * 
     * <p><b>注意：</b>由于随机选择的元素被交换到最左边，因此同样需要先移动右指针。
     * 原因与普通分区方法相同：基准在左边，必须先从右边开始移动，保证相遇点满足交换条件。</p>
     * 
     * @param arr 数组
     * @param left 左边界
     * @param right 右边界
     * @return 基准元素的最终位置
     */
    private static int partitionRandom(int[] arr, int left, int right) {
        // 随机选择一个位置作为基准
        Random random = new Random();
        int randomIndex = random.nextInt(right - left + 1) + left;
        
        // 将随机选择的元素交换到最左边，作为基准
        swap(arr, left, randomIndex);
        
        // 后续逻辑与普通分区相同
        // ⚠️ 同样需要先移动右指针（原因见 partition 方法的详细说明）
        int pivot = arr[left];
        int i = left;
        int j = right;

        while (i < j) {
            // 先移动右指针（关键！）
            while (i < j && arr[j] >= pivot) {
                j--;
            }
            // 再移动左指针
            while (i < j && arr[i] <= pivot) {
                i++;
            }
            if (i < j) {
                swap(arr, i, j);
            }
        }
        
        // 将基准交换到最终位置
        swap(arr, left, i);
        return i;
    }

    // ==================== 测试方法 ====================

    public static void main(String[] args) {
        // 测试基础版本
        System.out.println("=== 基础快速排序测试 ===");
        int[] arr1 = {64, 34, 25, 12, 22, 11, 90, 5};
        System.out.println("排序前: " + Arrays.toString(arr1));
        quickSort(arr1);
        System.out.println("排序后: " + Arrays.toString(arr1));
        
        // 测试随机化版本
        System.out.println("\n=== 随机化快速排序测试 ===");
        int[] arr2 = {64, 34, 25, 12, 22, 11, 90, 5};
        System.out.println("排序前: " + Arrays.toString(arr2));
        quickSortRandom(arr2);
        System.out.println("排序后: " + Arrays.toString(arr2));
        
        // 测试边界情况
        System.out.println("\n=== 边界情况测试 ===");
        int[] arr3 = {1};
        quickSort(arr3);
        System.out.println("单元素: " + Arrays.toString(arr3));
        
        int[] arr4 = {};
        quickSort(arr4);
        System.out.println("空数组: " + Arrays.toString(arr4));
        
        int[] arr5 = {5, 5, 5, 5, 5};
        quickSort(arr5);
        System.out.println("相同元素: " + Arrays.toString(arr5));
        
        int[] arr6 = {9, 8, 7, 6, 5, 4, 3, 2, 1};
        quickSort(arr6);
        System.out.println("逆序数组: " + Arrays.toString(arr6));
    }
}

