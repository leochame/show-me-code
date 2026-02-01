# 线程池核心记忆点

## 核心组件

### 1. 线程分类
- **核心线程（CoreThread）**：常驻线程，使用 `take()` 阻塞等待任务
- **支持线程（SupportThread）**：临时线程，使用 `poll(timeout)` 超时获取任务

### 2. 任务提交流程
```
提交任务
  ↓
核心线程未满？ → 是 → 创建核心线程 → 任务入队
  ↓ 否
队列未满？ → 是 → 任务入队
  ↓ 否
总线程数未满？ → 是 → 创建支持线程 → 任务入队
  ↓ 否
执行拒绝策略
```

## 实现要点

### 核心线程实现
```java
private class CoreThread extends Thread {
    public void run() {
        while (true) {
            Runnable task = blockingQueue.take();  // 阻塞等待
            task.run();  // 直接执行，不是 start()
        }
    }
}
```

### 支持线程实现
```java
private class SupportThread extends Thread {
    public void run() {
        while (true) {
            Runnable task = blockingQueue.poll(liveTime, timeUnit);
            if (task == null) break;  // 超时退出
            task.run();
        }
    }
}
```

### 任务提交逻辑
```java
public void execute(Runnable command) {
    // 1. 核心线程未满 → 创建核心线程
    if (coreThreadList.size() < coreSize) {
        createCoreThread();
        queue.offer(command);
        return;
    }
    
    // 2. 队列未满 → 入队
    if (queue.offer(command)) return;
    
    // 3. 总线程数未满 → 创建支持线程
    if (totalThreads < maxSize) {
        createSupportThread();
        queue.offer(command);
        return;
    }
    
    // 4. 拒绝策略
    reject(command);
}
```

## 关键记忆点

1. **核心线程 vs 支持线程**
   - 核心线程：`take()` 永久阻塞，永不退出
   - 支持线程：`poll(timeout)` 超时返回 null，自动退出

2. **任务执行**
   - 调用 `task.run()` 而不是 `task.start()`
   - 任务已在工作线程中，直接执行即可

3. **线程创建时机**
   - 核心线程：核心线程数未满时创建
   - 支持线程：队列满且总线程数未满时创建

4. **线程安全**
   - 使用 `BlockingQueue` 保证线程安全
   - 线程列表操作需要同步（建议使用 `CopyOnWriteArrayList` 或加锁）

5. **拒绝策略**
   - 队列满 + 线程数满时触发
   - 常见策略：抛异常、调用者执行、丢弃最老任务

## 完整实现结构

```java
public class MyThreadPool {
    // 参数
    private int coreSize;           // 核心线程数
    private int maxSize;            // 最大线程数
    private BlockingQueue<Runnable> queue;  // 任务队列
    private int liveTime;           // 支持线程存活时间
    private TimeUnit timeUnit;      // 时间单位
    
    // 线程管理
    private List<Thread> coreThreadList;      // 核心线程列表
    private List<Thread> supportThreadList;   // 支持线程列表
    
    // 内部类
    private class CoreThread extends Thread { ... }
    private class SupportThread extends Thread { ... }
    
    // 核心方法
    public void execute(Runnable command) { ... }
}
```

## 常见问题

1. **为什么核心线程用 take()，支持线程用 poll(timeout)？**
   - 核心线程需要常驻，永久等待任务
   - 支持线程需要超时退出，避免资源浪费

2. **为什么调用 run() 而不是 start()？**
   - 任务已经在工作线程中运行，直接执行即可
   - 如果调用 start() 会创建新线程，违背线程池设计

3. **如何优雅关闭线程池？**
   - 中断所有线程：`thread.interrupt()`
   - 等待任务完成：`thread.join()`
   - 清空队列：`queue.clear()`
