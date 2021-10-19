import extern.Bank;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.out;

public final class Exercise3 {
    public static void main(final String... args) {
        final var N_THREADS = 10;
        final var N_DEPOSITS = 1000;
        final var DEPOSIT_VALUE = 100;
        final var EXPECTED_SAVINGS = N_THREADS * N_DEPOSITS * DEPOSIT_VALUE;

        final var savings = Bank.Account.withBalance(0);
        final var savingsLock = new ReentrantLock(); // alternatively, could be
                                                     // a field of Bank.

        final var pool = new ArrayList<Thread>();
        pool.ensureCapacity(N_THREADS);

        for (var i = 0; i < 10; ++i) {
            final var thread = new Thread(() -> {
                savingsLock.lock();
                try {
                    for (var j = 0; j < N_DEPOSITS; ++j) {
                        savings.deposit(DEPOSIT_VALUE);
                    }
                } finally {
                    savingsLock.unlock();
                }
            });
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

        out.print("""
            Expected savings: %d
            Actual savings:   %d
            """.formatted(EXPECTED_SAVINGS, savings.balance())
        );
    }
}
