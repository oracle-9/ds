package extern;

import extern.Bank.Version;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

import static java.lang.System.out;

public final class Test {
    private static void test(final Bank.Version version) {
        final var N_THREADS = 2;
        final var N_ACCOUNTS = 10;
        final var N_TRANSFERS = 100000;
        final var DEPOSIT_VALUE = 1000;
        final var TRANSFER_VALUE = 1;

        final var bank = new Bank.Builder()
            .withVersion(version)
            .setUpAccounts(N_ACCOUNTS)
            .build();

        for (var i = 0; i < N_ACCOUNTS; ++i) {
            bank.deposit(i, DEPOSIT_VALUE);
        }

        out.println(
            "total balance before transfers: %d".formatted(bank.totalBalance())
        );

        final var pool = new ArrayList<Thread>();
        pool.ensureCapacity(N_THREADS);

        for (var i = 0; i < N_THREADS; ++i) {
            final var thread = new Thread(() -> {
                final var transactionEnds = bank.getRandomAccounts(2).toArray();
                for (var m = 0; m < N_TRANSFERS; ++m) {
                    bank.transfer(
                        transactionEnds[0],
                        transactionEnds[1],
                        TRANSFER_VALUE
                    );
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

        out.println(
            "total balance after transfers: %d".formatted(bank.totalBalance())
        );
    }

    public static void main(final String... args) {
        out.println("Lockless implementation results:");
        final var t1Start = Instant.now();
        test(Version.ORIGINAL);
        final var t1End = Instant.now();

        out.println("\nBank-wide lock implementation results:");
        final var t2Start = Instant.now();
        test(Version.EXERCISE_2);
        final var t2End = Instant.now();

        out.println("\nAccount-wide lock implementation results");
        final var t3Start = Instant.now();
        test(Version.EXERCISE_3);
        final var t3End = Instant.now();

        final var t1Elapsed = Duration.between(t1Start, t1End).toMillis();
        final var t2Elapsed = Duration.between(t2Start, t2End).toMillis();
        final var t3Elapsed = Duration.between(t3Start, t3End).toMillis();

        out.println("""

            Lockless implementation elapsed time:          %dms
            Bank-wide lock implementation elapsed time:    %dms
            Account-wide lock implementation elapsed time: %dms"""
            .formatted(t1Elapsed, t2Elapsed, t3Elapsed)
        );

    }
}
