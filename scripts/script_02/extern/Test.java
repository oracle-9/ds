package extern;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.function.BiConsumer;

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
                for (var j = 0; j < N_TRANSFERS; ++j) {
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
        final var runTest = (BiConsumer<Bank.Version, String>)
        (version, message) -> {
            out.println(message);
            final var ti = Instant.now();
            test(version);
            final var tf = Instant.now();
            out.println(
                "elapsed: %dms%n".formatted(Duration.between(ti, tf).toMillis())
            );
        };

        runTest.accept(
            Bank.Version.ORIGINAL, "Lockless implementation results:"
        );
        runTest.accept(
            Bank.Version.EXERCISE_2, "Bank-wide lock implementation results:"
        );
        runTest.accept(
            Bank.Version.EXERCISE_3, "Account-wide lock implementation results:"
        );
    }
}
