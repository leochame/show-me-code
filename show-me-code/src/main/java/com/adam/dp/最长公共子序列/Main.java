package com.adam.dp.最长公共子序列;

/**
 * <a href="https://writings.sh/post/algorithm-longest-common-substring-and-longest-common-subsequence">解题思路</a>
 *  a[i] == b[j]， 则填写 table[i][j] = table[i-1][j-1] + 1
 *  a[i] != b[j]， 填写 table[i][j] = max(table[i-1][j], table[i][j-1])
 */
public class Main {
    public static void main(String[] args) {

    }
}
