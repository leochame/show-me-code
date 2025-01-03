package leetcode_347;

class Solution {
    int[] heap;
    int k;
    Map<Integer,Integer> map = new HashMap<>();
    public int[] topKFrequent(int[] nums, int k) {
        
        this.heap = new int[k];
        this.k = k;
        map.put(Integer.MIN_VALUE,Integer.MIN_VALUE);
        for(int i = 0; i < heap.length; i++){
            heap[i] = Integer.MIN_VALUE;
        }
        for(int i = 0; i < nums.length; i++){
            map.put(nums[i], map.getOrDefault(nums[i], 0) + 1);
        }
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            int value = entry.getKey();
            int count = entry.getValue();
            if(count <= map.get(heap[0]))
                continue;
            heap[0] = value;
            keepHeap(0);
        }
        return heap;
    }

    private void keepHeap(int i){
        int l = 2 * i + 1;
        int r = 2 * i + 2;
        int min = i;
        if(l < k && map.get(heap[l]) < map.get(heap[min]))
            min = l;
        if(r < k && map.get(heap[r]) < map.get(heap[min]))
            min = r;

        if(min == i)
            return;
        swap(i,min);
        keepHeap(min);    
    }
    private void swap(int i, int j){
        int temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }
}