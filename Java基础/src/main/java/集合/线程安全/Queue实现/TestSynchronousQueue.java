package 集合.线程安全.Queue实现;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

public class TestSynchronousQueue {
    private static int THREAD_NUM;
    private static int N = 100000;
    private static ExecutorService executor;
    public static void main(String[] args) throws Exception {                   
        System.out.println("Producer\tConsumer\tcapacity \t LinkedBlockingQueue \t ArrayBlockingQueue \t SynchronousQueue");
                            
        for(int j = 0; j<4; j++){
            THREAD_NUM = (int) Math.pow(2, j);
            executor = Executors.newFixedThreadPool(THREAD_NUM * 2);
                                
            for (int i = 10; i <= N; i=i*10) {
                int length = i;
                System.out.print(THREAD_NUM + "\t\t");
                System.out.print(THREAD_NUM + "\t\t");
                System.out.print(length + "\t\t");
                System.out.print(doTest2(new LinkedBlockingQueue<Integer>(length), N) + "/s\t\t\t");
                System.out.print(doTest2(new ArrayBlockingQueue<Integer>(length), N) + "/s\t\t\t");
                System.out.print(doTest2(new SynchronousQueue<Integer>(), N) + "/s");
                System.out.println();
            }
                                
            executor.shutdown();
        }
    }
                        
    private static class Producer implements Runnable{
        int n;
        BlockingQueue<Integer> q;
                            
        public Producer(int initN, BlockingQueue<Integer> initQ){
            n = initN;
            q = initQ;
        }
                            
        public void run() {
            for (int i = 0; i < n; i++)
                try {
                    q.put(i);
                } catch (InterruptedException ex) {
                }
        }
    }
                        
    private static class Consumer implements Callable<Long>{
        int n;
        BlockingQueue<Integer> q;
                            
        public Consumer(int initN, BlockingQueue<Integer> initQ){
            n = initN;
            q = initQ;
        }
                            
        public Long call() {
            long sum = 0;
            for (int i = 0; i < n; i++)
                try {
                    sum += q.take();
                } catch (InterruptedException ex) {
                }
            return sum;
        }
    }
                        
    private static long doTest2(final BlockingQueue<Integer> q, final int n)
            throws Exception {
        CompletionService<Long> completionServ = new ExecutorCompletionService<Long>(executor);
                            
        long t = System.nanoTime();
        for(int i=0; i<THREAD_NUM; i++){
            executor.submit(new Producer(n/THREAD_NUM, q));
        }   
        for(int i=0; i<THREAD_NUM; i++){
            completionServ.submit(new Consumer(n/THREAD_NUM, q));
        }
                            
        for(int i=0; i<THREAD_NUM; i++){
            completionServ.take().get();
        }   
                            
        t = System.nanoTime() - t;
        return (long) (1000000000.0 * N / t); // Throughput, items/sec
    }
}
