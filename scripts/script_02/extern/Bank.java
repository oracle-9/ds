package extern;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public final class Bank {
    private static final int DEFAULT_N_ACCOUNTS = 10;
    private final Account[] accounts;
    private final ThreadLocalRandom rng;
    private final ReentrantLock lock;
    private final Version version;

    public enum Version {
        ORIGINAL,
        EXERCISE_1,
        EXERCISE_2,
        EXERCISE_3,
    }

    private Account getAccountOrThrow(final int accountId) {
        if (accountId >= this.accounts.length) {
            throw new RuntimeException("no such account");
        }
        return this.accounts[accountId];
    }

    private Bank(
        final Account[] accounts,
        final ThreadLocalRandom rng,
        final ReentrantLock lock,
        final Version version
    ) {
        this.accounts = accounts;
        this.rng = rng;
        this.lock = lock;
        this.version = version;
    }

    public static final class Builder {
        private Bank.Version version = Version.EXERCISE_3;
        private int nAccounts = Bank.DEFAULT_N_ACCOUNTS;

        public Bank.Builder withVersion(final Bank.Version version) {
            this.version = version;
            return this;
        }

        public Bank.Builder setUpAccounts(final int nAccounts) {
            this.nAccounts = nAccounts;
            return this;
        }

        public Bank build() {
            final var accounts = new Account[this.nAccounts];
            final var bankWideLock = this.version == Version.EXERCISE_1 ||
                                     this.version == Version.EXERCISE_2 ||
                                     this.version == Version.EXERCISE_3
                ? new ReentrantLock()
                : null;

            final var bank = new Bank(
                accounts,
                ThreadLocalRandom.current(),
                bankWideLock,
                this.version
            );

            switch (this.version) {
            case EXERCISE_3 ->
                Arrays.fill(accounts, bank.new Account(0, new ReentrantLock()));
            default ->
                Arrays.fill(accounts, bank.new Account(0, null));
            }
            return bank;
        }
    }

    public int balance(final int accountId) {
        return switch (this.version) {
        case ORIGINAL -> this.getAccountOrThrow(accountId).balance();
        case EXERCISE_1, EXERCISE_2 -> {
            this.lock.lock();
            try {
                yield this.getAccountOrThrow(accountId).balance();
            } finally {
                this.lock.unlock();
            }
        }
        case EXERCISE_3 -> {
            Account account;
            this.lock.lock();
            try {
                account = this.getAccountOrThrow(accountId);
            } catch (final RuntimeException e) {
                this.lock.unlock();
                throw e;
            }
            account.lock.lock();
            this.lock.unlock();
            try {
                yield account.balance();
            } finally {
                account.lock.unlock();
            }
        }
        };
    }

    public void deposit(final int accountId, final int value) {
        switch (this.version) {
        case ORIGINAL -> this.getAccountOrThrow(accountId).deposit(value);
        case EXERCISE_1, EXERCISE_2 -> {
            this.lock.lock();
            try {
                this.getAccountOrThrow(accountId).deposit(value);
            } finally {
                this.lock.unlock();
            }
        }
        case EXERCISE_3 -> {
            Account account;
            this.lock.lock();
            try {
                account = this.getAccountOrThrow(accountId);
            } catch (final RuntimeException e) {
                this.lock.unlock();
                throw e;
            }
            account.lock.lock();
            this.lock.unlock();
            try {
                account.deposit(value);
            } finally {
                account.lock.unlock();
            }
        }
        }
    }

    public void withdraw(final int accountId, final int value) {
        switch (this.version) {
        case ORIGINAL -> this.getAccountOrThrow(accountId).withdraw(value);
        case EXERCISE_1, EXERCISE_2 -> {
            this.lock.lock();
            try {
                this.getAccountOrThrow(accountId).withdraw(value);
            } finally {
                this.lock.unlock();
            }
        }
        case EXERCISE_3 -> {
            Account account;
            this.lock.lock();
            try {
                account = this.getAccountOrThrow(accountId);
            } catch (final RuntimeException e) {
                this.lock.unlock();
                throw e;
            }
            account.lock.lock();
            this.lock.unlock();
            try {
                account.withdraw(value);
            } finally {
                account.lock.unlock();
            }
        }
        }
    }

    public void transfer(
        final int srcAccountId,
        final int destAccountId,
        final int value
    ) {
        switch (this.version) {
        case ORIGINAL -> {
            final var srcAccount = this.getAccountOrThrow(srcAccountId);
            final var destAccount = this.getAccountOrThrow(destAccountId);
            srcAccount.withdraw(value);
            try {
                destAccount.deposit(value);
            } catch (final RuntimeException e) {
                srcAccount.deposit(value);
                throw e;
            }
        }
        case EXERCISE_1, EXERCISE_2 -> {
            Account srcAccount;
            Account destAccount;
            this.lock.lock();

            try {
                srcAccount = this.getAccountOrThrow(destAccountId);
                destAccount = this.getAccountOrThrow(destAccountId);
                srcAccount.withdraw(value);
            } catch (final Exception e) {
                this.lock.unlock();
                throw e;
            }

            try {
                destAccount.deposit(value);
            } catch (final RuntimeException e) {
                srcAccount.deposit(value); // should never throw.
                throw e;
            } finally {
                this.lock.unlock();
            }
        }
        case EXERCISE_3 -> {
            Account srcAccount;
            Account destAccount;
            this.lock.lock();

            try {
                srcAccount = this.getAccountOrThrow(srcAccountId);
                destAccount = this.getAccountOrThrow(destAccountId);
            } catch (final Exception e) {
                this.lock.unlock();
                throw e;
            }

            srcAccount.lock.lock();
            destAccount.lock.lock();
            this.lock.unlock();

            try {
                srcAccount.withdraw(value);
            } catch (final RuntimeException e) {
                srcAccount.lock.unlock();
                destAccount.lock.unlock();
                throw e;
            }

            try {
                destAccount.deposit(value);
            } catch (final RuntimeException e) {
                srcAccount.deposit(value); // should never throw.
                throw e;
            } finally {
                srcAccount.lock.unlock();
                destAccount.lock.unlock();
            }
        }
        }
    }

    public int totalBalance() {
        return switch (this.version) {
        case ORIGINAL, EXERCISE_1 ->
            Arrays.stream(this.accounts).mapToInt(Account::balance).sum();
        case EXERCISE_2 -> {
            this.lock.lock();
            try {
                yield Arrays
                    .stream(this.accounts)
                    .mapToInt(Account::balance)
                    .sum();
            } finally {
                this.lock.unlock();
            }
        }
        case EXERCISE_3 -> {
            // In this case we only lock the bank and each account for as long
            // as needed. The downside is we need to iterate the accounts twice.
            // Alternatively, we could just lock the entire bank and iterate
            // once, just like in EXERCISE_2, but we wouldn't be taking
            // advantage of account-specific locking.
            this.lock.lock();
            for (final var account : this.accounts) {
                account.lock.lock();
            }
            this.lock.unlock();
            var sum = 0;
            for (final var account : this.accounts) {
                // account.balance() doesn't throw, so no try/finally block is
                // required. If it were to throw, we'd need a finally block
                // where we iterate over the rest of the accounts whose balance
                // hasn't been taken (including this one) and unlock it.
                sum += account.balance();
                account.lock.unlock();
            }
            yield sum;
        }
        };
    }

    public IntStream getRandomAccounts(final int limit) {
        return this.rng.ints(0, this.accounts.length).distinct().limit(limit);
    }

    private final class Account {
        private int balance;
        private final ReentrantLock lock;

        private Account(final int balance, final ReentrantLock lock) {
            this.balance = balance;
            this.lock = lock;
        }

        public int balance() {
            return this.balance;
        }

        private void deposit(final int value) {
            if (value < 0) {
                throw new RuntimeException("cannot deposit negative values");
            }
            if (this.balance > Integer.MAX_VALUE - value) {
                // balance integer overflow.
                throw new RuntimeException(
                    "value exceeds maximum account balance"
                );
            }
            this.balance += value;
        }

        private void withdraw(final int value) {
            if (value < 0) {
                throw new RuntimeException("cannot withdraw negative values");
            }
            if (this.balance < Integer.MIN_VALUE + value) {
                // balance integer underflow.
                throw new RuntimeException(
                    "value exceeds minimum account balance"
                );
            }
            this.balance -= value;
        }
    }
}
