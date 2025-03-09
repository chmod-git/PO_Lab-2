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

    public static void main(String[] args) {
        int[] arraySizes = {10_000, 100_000, 1_000_000};

        for (int arraySize : arraySizes) {
            int[] array = new int[arraySize];
            double totalTimeNonParallel = 0.0;

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
        }
    }
}
