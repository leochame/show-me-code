package com.adam.leetcode_295;

import java.util.*;

class MedianFinder {
    int size;
    int f;
    List<Integer> big;   // 小顶堆，保存较大的一半
    List<Integer> small; // 大顶堆，保存较小的一半

    public MedianFinder() {
        this.size = 0;
        this.f = 0;
        big = new ArrayList<>();
        small = new ArrayList<>();
    }

    public void addNum(int num) {
        if (small.size() == 0 || num <= small.get(0)) {
            small.add(num);
            buildSmall(small.size() - 1);
        } else {
            big.add(num);
            buildBig(big.size() - 1);
        }

        // 保持平衡
        if (small.size() > big.size() + 1) {
            big.add(small.get(0));
            buildBig(big.size() - 1);
            small.set(0, small.get(small.size() - 1));
            small.remove(small.size() - 1);
            heapifySmall(0);
        } else if (big.size() > small.size()) {
            small.add(big.get(0));
            buildSmall(small.size() - 1);
            big.set(0, big.get(big.size() - 1));
            big.remove(big.size() - 1);
            heapifyBig(0);
        }
    }

    public double findMedian() {
        if (small.size() > big.size()) {
            return (double) small.get(0);
        } else {
            return (small.get(0) + big.get(0)) / 2.0;
        }
    }

    private void buildBig(int i) {
        while (i > 0 && big.get((i - 1) / 2) > big.get(i)) {
            swap(i, (i - 1) / 2, big);
            i = (i - 1) / 2;
        }
    }

    private void heapifyBig(int i) {
        int l = 2 * i + 1;
        int r = 2 * i + 2;
        int smallest = i;

        if (l < big.size() && big.get(l) < big.get(smallest)) {
            smallest = l;
        }

        if (r < big.size() && big.get(r) < big.get(smallest)) {
            smallest = r;
        }

        if (smallest != i) {
            swap(i, smallest, big);
            heapifyBig(smallest);
        }
    }

    private void buildSmall(int i) {
        while (i > 0 && small.get((i - 1) / 2) < small.get(i)) {
            swap(i, (i - 1) / 2, small);
            i = (i - 1) / 2;
        }
    }

    private void heapifySmall(int i) {
        int l = 2 * i + 1;
        int r = 2 * i + 2;
        int largest = i;

        if (l < small.size() && small.get(l) > small.get(largest)) {
            largest = l;
        }

        if (r < small.size() && small.get(r) > small.get(largest)) {
            largest = r;
        }

        if (largest != i) {
            swap(i, largest, small);
            heapifySmall(largest);
        }
    }

    private void swap(int i, int j, List<Integer> heap) {
        int temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }
}

