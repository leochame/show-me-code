package sort;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        int[] ints = {1,3,5,6,2,3,7,8,3,2,2,2,2,2,2,2,2,2,2,2,2,2,2};
        QuickSort.quickSort(ints,0,ints.length-1);
        System.out.println(Arrays.toString(ints));
    }
}
