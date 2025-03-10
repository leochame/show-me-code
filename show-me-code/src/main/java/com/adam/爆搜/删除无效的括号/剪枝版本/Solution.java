package com.adam.爆搜.删除无效的括号.剪枝版本;
import java.util.*;
class Solution {
    int n;
    Map<Integer,Set<String>> map= new HashMap<>();
    int max;
    char[] chars;
    public List<String> removeInvalidParentheses(String s) {
        n = s.length();
        max = 0;
        chars = s.toCharArray();
        dfs(new StringBuilder(),0,0);
        return new ArrayList<String>(map.get(max));
    }
    public void dfs(StringBuilder sb, int i, int sum){            
        if(i == n){
            if( sb.length() >= max &&  sum == 0 && judge(sb.toString())){
                max = sb.length();
                Set<String> set = map.getOrDefault(max,new HashSet<>());
                set.add(new String(sb.toString()));
                map.put(max,set);
            }
            return;
        }
        if(chars[i] == '(' || chars[i] == ')') dfs(sb,i+1,sum);
        sb.append(chars[i]);
        if(chars[i] == '('){
           dfs(sb,i+1,sum + 1);
        }else if(chars[i] == ')'){
            dfs(sb,i+1,sum - 1);
        }else{
            dfs(sb,i+1,sum);
        }
        
        sb.deleteCharAt(sb.length()-1);
    }
    public Boolean judge(String s){
        Deque<Integer> deque = new ArrayDeque<>();
        char[] chars = s.toCharArray();
        
        for(int i = 0; i < chars.length;i++ ){
            if(chars[i] == '('){
                deque.push(i);
            }else if(chars[i] == ')'){
                if(deque.isEmpty()) return false;
                deque.pop();
            }
        }
        if(deque.isEmpty()) return true;
        return false;
    }

}