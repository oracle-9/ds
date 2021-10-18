import extern.Increment;

import java.util.ArrayList;

import static java.lang.System.out;

public final class Exercise1 {
    public static void main(final String... args) {
        final var N_THREADS = 10;
        final var payload = new Increment();

        final var pool = new ArrayList<Thread>();
        pool.ensureCapacity(N_THREADS);

        for (var i = 0; i < N_THREADS; ++i) {
            final var thread = new Thread(payload);
            thread.start();
            pool.add(thread);
        }

        for (final var thread : pool) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        out.println("end");
    }
}
