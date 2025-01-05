package leetcode_208;

class Trie {
    char v; // 当前节点的字符
    List<Trie> list; // 子节点列表

    // 构造函数
    public Trie() {
        this.v = '\0'; // 默认值
        this.list = new ArrayList<>(); // 初始化子节点列表
    }

    // 插入一个单词
    public void insert(String word) {
        char[] chars = word.toCharArray();
        Trie trie = this; // 从根节点开始
        for (char c : chars) {
            Trie next = null;
            // 查找当前字符是否已经存在
            for (Trie child : trie.list) {
                if (child!=null && child.v == c) {
                    next = child;
                    break;
                }
            }
            // 如果当前字符不存在，创建新节点
            if (next == null) {
                next = new Trie();
                next.v = c;
                trie.list.add(next);
            }
            trie = next; // 进入下一层
        }
        // 用 null 表示单词结束
        if (!trie.list.contains(null)) {
            trie.list.add(null);
        }
    }

    // 搜索一个单词
    public boolean search(String word) {
        char[] chars = word.toCharArray();
        Trie trie = this; // 从根节点开始
        for (char c : chars) {
            Trie next = null;
            for (Trie child : trie.list) {
                if (child != null && child.v == c) {
                    next = child;
                    break;
                }
            }
            if (next == null) return false; // 找不到字符
            trie = next;
        }
        return trie.list.contains(null); // 检查单词是否结束
    }

    // 检查是否存在以 prefix 开头的单词
    public boolean startsWith(String prefix) {
        char[] chars = prefix.toCharArray();
        Trie trie = this; // 从根节点开始
        for (char c : chars) {
            Trie next = null;
            // 查找当前字符是否存在
            for (Trie child : trie.list) {
                if (child != null && child.v == c) {
                    next = child;
                    break;
                }
            }
            if (next == null) return false; // 找不到字符
            trie = next;
        }
        return true; // 成功匹配到前缀
    }
}
