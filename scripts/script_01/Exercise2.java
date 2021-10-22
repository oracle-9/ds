import extern.Bank;

import java.util.ArrayList;

import static java.lang.System.out;

public final class Exercise2 {
    public static void main(final String... args) {
        final var N_THREADS = 10;
        final var N_DEPOSITS = 1000;
        final var DEPOSIT_VALUE = 100;
        final var EXPECTED_SAVINGS = N_THREADS * N_DEPOSITS * DEPOSIT_VALUE;

        final var savings = Bank.Account.withBalance(0);

        final var pool = new ArrayList<Thread>();
        pool.ensureCapacity(N_THREADS);

        for (var i = 0; i < N_THREADS; ++i) {
            final var thread = new Thread(() -> {
                for (var j = 0; j < N_DEPOSITS; ++j) {
                    savings.deposit(DEPOSIT_VALUE);
                }
            });
            thread.start();
            pool.add(thread);
        }

        for (final var thread : pool) {
            try {
                thread.join();
            } catch (final InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        out.println("""
            Expected savings: %d
            Actual savings:   %d"""
            .formatted(EXPECTED_SAVINGS, savings.balance())
        );
    }
}
