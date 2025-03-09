import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void fillArray(int[] array, int n) {
        for (int i = 0; i < n; i++) {
            array[i] = (int) (Math.random() * 10000);
        }
    }

    public static void nonParallel(int[] array, int[] min) {
        int minLocal = array[0];
        int count = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i] < minLocal) {
                minLocal = array[i];
                count = 1;
            } else if (array[i] == minLocal) {
                count++;
            }
        }

        min[0] = minLocal;
        min[1] = count;
    }

    public static void parallelPrimitiveSync(int[] array, int startRow, int endRow, int[] min) {
        int minLocal = min[0];
        int count = 0;
        for (int i = startRow; i < endRow; i++) {
            synchronized (Main.class) {
                if (array[i] < minLocal) {
                    minLocal = array[i];
                    count = 1;
                } else if (array[i] == minLocal) {
                    count++;
                }
            }
        }

        if (minLocal < min[0]) {
            min[0] = minLocal;
            min[1] = count;
        } else if (minLocal == min[0]) {
            min[1] += count;
        }
    }

    public static void parallelAtomic(int[] array, int startRow, int endRow, AtomicInteger min, AtomicInteger count) {
        int minLocal = min.get();
        int counter = 0;
        for (int i = startRow; i < endRow; i++) {
            if (array[i] < minLocal) {
                minLocal = array[i];
                counter = 1;
            } else if (array[i] == minLocal) {
                counter++;
            }
        }

        if (minLocal < min.get()) {
            setAtomicValue(min, minLocal);
            setAtomicValue(count, counter);
        } else if (minLocal == min.get()) {
            addAtomicValue(count, counter);
        }
    }

    public static void addAtomicValue(AtomicInteger valueOld, int increment) {
        int expectedCount = valueOld.get();
        int newCount = expectedCount + increment;
        while (!valueOld.compareAndSet(expectedCount, newCount)) {
            expectedCount = valueOld.get();
            newCount = expectedCount + increment;
        }
    }

    public static void setAtomicValue(AtomicInteger valueOld, int newValue) {
        int oldValue = valueOld.get();
        while (!valueOld.compareAndSet(oldValue, newValue)) {
            oldValue = valueOld.get();
        }
    }

    public static void main(String[] args) {
        int[] arraySizes = {10_000, 100_000, 1_000_000};
        double[] primitiveSyncExecutionTimes = new double[arraySizes.length];
        double[] atomicSyncExecutionTimes = new double[arraySizes.length];
        int sizeIndex = 0;

        for (int arraySize : arraySizes) {
            int numberOfRuns = 10;
            int numberOfThreads = 5;
            int[] array = new int[arraySize];
            int[] minResult = new int[]{Integer.MAX_VALUE, 0};
            double totalTimePrimitiveSync = 0.0;
            double totalTimeNonParallel = 0.0;
            double totalTimeAtomicSync = 0.0;

            fillArray(array, arraySize);

            final String ANSI_GREEN = "\u001B[32m";
            final String ANSI_RESET = "\u001B[0m";
            System.out.println(ANSI_GREEN + "\nArray size " + arraySize + ANSI_RESET);

            long startTime = System.nanoTime();
            int[] minNonParallel = new int[]{Integer.MAX_VALUE, 0};
            nonParallel(array, minNonParallel);
            long endTime = System.nanoTime();
            totalTimeNonParallel = (endTime - startTime) / 1e9;

            System.out.println("Minimum element: " + minNonParallel[0]);
            System.out.println("Minimum elements amount: " + minNonParallel[1]);
            System.out.printf("=== One thread without parallelization ===\nTotal Execution Time: %.5f seconds\n", totalTimeNonParallel);

            for (int i = 0; i < numberOfRuns; i++) {
                startTime = System.nanoTime();
                Thread[] threads = new Thread[numberOfThreads];
                int chunkSize = arraySize / numberOfThreads;

                for (int threadIndex = 0; threadIndex < numberOfThreads; threadIndex++) {
                    int startRow = threadIndex * chunkSize;
                    int endRow = (threadIndex == numberOfThreads - 1) ? arraySize : (threadIndex + 1) * chunkSize;
                    threads[threadIndex] = new Thread(() -> parallelPrimitiveSync(array, startRow, endRow, minResult));
                    threads[threadIndex].start();
                }

                for (Thread thread : threads) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                endTime = System.nanoTime();
                totalTimePrimitiveSync += (endTime - startTime) / 1e9;
                if (i != numberOfRuns - 1) minResult[1] = 0;
            }

            System.out.println("\nMinimum element: " + minResult[0]);
            System.out.println("Minimum elements amount: " + minResult[1]);
            System.out.printf("=== Primitives synchronization ===\nNumber of Threads: %d, Total Execution Time: %.5f seconds\n",
                    numberOfThreads, (totalTimePrimitiveSync / numberOfRuns));

            AtomicInteger atomicMinValue = new AtomicInteger(Integer.MAX_VALUE);
            AtomicInteger atomicMinCount = new AtomicInteger(0);

            for (int i = 0; i < numberOfRuns; i++) {
                startTime = System.nanoTime();
                Thread[] threads = new Thread[numberOfThreads];
                int chunkSize = arraySize / numberOfThreads;

                for (int threadIndex = 0; threadIndex < numberOfThreads; threadIndex++) {
                    int startRow = threadIndex * chunkSize;
                    int endRow = (threadIndex == numberOfThreads - 1) ? arraySize : (threadIndex + 1) * chunkSize;
                    threads[threadIndex] = new Thread(() -> parallelAtomic(array, startRow, endRow, atomicMinValue, atomicMinCount));
                    threads[threadIndex].start();
                }

                for (Thread thread : threads) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                endTime = System.nanoTime();
                totalTimeAtomicSync += (endTime - startTime) / 1e9;
                if (i != numberOfRuns - 1) atomicMinCount.set(0);
            }

            System.out.println("\nMinimum element: " + atomicMinValue.get());
            System.out.println("Minimum elements amount: " + atomicMinCount.get());
            System.out.printf("=== Atomic synchronization ===\nNumber of Threads: %d, Total Execution Time: %.5f seconds\n",
                    numberOfThreads, (totalTimeAtomicSync / numberOfRuns));

            primitiveSyncExecutionTimes[sizeIndex] = totalTimePrimitiveSync / numberOfRuns;
            atomicSyncExecutionTimes[sizeIndex] = totalTimeAtomicSync / numberOfRuns;
            sizeIndex++;
        }

        PlotBuilder.plotBarGraph("Array Size vs Execution Time", "Array Size", "Execution Time (seconds)",
                "Parallel Primitive Sync", arraySizes, primitiveSyncExecutionTimes, "Atomic Sync", arraySizes, atomicSyncExecutionTimes);
    }
}
