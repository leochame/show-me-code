package BigInteger.BigInteger_Mutiply;

class Solution {
    public String multiply(String num1, String num2) {
        if(num1.equals("0") || num2.equals("0")) return "0";
        char[] chars1 = num1.toCharArray();
        char[] chars2 = num2.toCharArray();
        int[] arg = new int[chars1.length + chars2.length + 1];
        for(int i = chars1.length -1 ; i >=0; i--){
            int x1 = chars1[i] - '0';
            for(int j = chars2.length - 1; j >= 0; j--){
                int x2 = chars2[j] - '0';
                arg[chars1.length + chars2.length - 2 - i - j] += x1 * x2;
            }
        }
        int add = 0;
        for(int i = 0; i < chars1.length + chars2.length + 1; i++){
            int temp = add;
            add = (arg[i]+ add) / 10;
            arg[i] = (arg[i] + temp)  % 10;
        }
        StringBuilder sb = new StringBuilder();
        int index = arg[chars1.length + chars2.length - 1] == 0 ? chars1.length + chars2.length - 2 : chars1.length + chars2.length - 1;
        while(index >= 0){
            sb.append(arg[index]);
            index--;
        }
        return sb.toString();
    }
}