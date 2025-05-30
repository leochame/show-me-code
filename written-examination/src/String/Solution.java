package String;

/**
 * 最长回文子串
 */
class longestPalindrome {
    public String longestPalindrome(String s) {
        char[] chars = s.toCharArray();
        // 标记
        StringBuilder sb = new StringBuilder("^#");
        for(int i = 0; i < chars.length; i++){
            sb.append(chars[i]).append('#');
        }
        sb.append('$');
        chars = sb.toString().toCharArray();
        int[] b = new int[chars.length];
        int maxCenter = 0;
        int curCenter = 0;
        int curBoundry = 0;

        for(int i = 1; i < chars.length - 1;i++ ){
            int mirror = 2*curCenter - i;
            if(i < curBoundry) b[i] = Math.min(curBoundry - i,b[mirror]);
            
            while(chars[i + b[i] + 1] == chars[i - b[i] - 1]) b[i]++;

            if(b[i]+i > curBoundry){
                curCenter = i;
                curBoundry = b[i] + i;
            }
            if(b[i] > b[maxCenter]){
                maxCenter = i;
            }
        } 

        int start = (maxCenter - b[maxCenter])/2;
        return s.substring(start,start+b[maxCenter]);

    }
}