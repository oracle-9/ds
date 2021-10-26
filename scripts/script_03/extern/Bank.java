package extern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class Bank {
    private final Map<Integer, Account> accounts;
    private ReentrantLock lock;

    public Bank() {
        this.accounts = new HashMap<>();
        this.lock = new ReentrantLock();
    }

    private Account getAccountOrThrow(final int accountId) {
        final var account = this.accounts.get(accountId);
        if (account == null) {
            throw new RuntimeException("no such account");
        }
        return account;
    }

    public int registerAccount() {
        this.lock.lock();
        try {
            final var accountId = this.accounts.size();
            this.accounts.put(accountId, new Account());
            return accountId;
        } finally {
            this.lock.unlock();
        }
    }

    public int closeAccount(final int accountId) {
        Account account;
        this.lock.lock();
        try {
            account = this.getAccountOrThrow(accountId);
        } catch (final RuntimeException e) {
            this.lock.unlock();
            throw e;
        }
        this.accounts.remove(accountId);
        account.lock.lock();
        this.lock.unlock();
        try {
            return account.balance();
        } finally  {
            account.lock.unlock();
        }
    }

    public int balance(final int accountId) {
        Account account;
        this.lock.lock();
        try {
            account = this.getAccountOrThrow(accountId);
        } catch (RuntimeException e) {
            this.lock.unlock();
            throw e;
        }
        account.lock.lock();
        this.lock.unlock();
        try {
            return account.balance();
        } finally {
            account.lock.unlock();
        }
    }

    public void deposit(final int accountId, final int value) {
        Account account;
        this.lock.lock();
        try {
            account = this.getAccountOrThrow(accountId);
        } catch (RuntimeException e) {
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

    public void withdraw(final int accountId, final int value) {
        Account account;
        this.lock.lock();
        try {
            account = this.getAccountOrThrow(accountId);
        } catch (RuntimeException e) {
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

    public void transfer(
        final int srcAccountId,
        final int destAccountId,
        final int value
    ) {
        Account srcAccount;
        Account destAccount;
        this.lock.lock();

        try {
            srcAccount = this.getAccountOrThrow(srcAccountId);
            destAccount = this.getAccountOrThrow(destAccountId);
        } catch (final RuntimeException e) {
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
            srcAccount.deposit(value);
            throw e;
        } finally {
            srcAccount.lock.unlock();
            destAccount.lock.unlock();
        }
    }

    public int totalBalance() {
        this.lock.lock();
        final var accounts = this.accounts.values();
        for (final var account : accounts) {
            account.lock.lock();
        }
        this.lock.unlock();
        var sum = 0;
        for (final var account : accounts) {
            sum += account.balance();
            account.lock.unlock();
        }
        return sum;
    }

    public int totalBalanceOf(final int... accountIds) {
        final var accounts = new ArrayList<Account>();
        accounts.ensureCapacity(accountIds.length);
        this.lock.lock();
        for (final var accountId : accountIds) {
            Account account;
            try {
                account = this.getAccountOrThrow(accountId);
            } catch (final RuntimeException e) {
                // We need to unlock every valid user up until this point before
                // we propagate the error.
                for (final var lockedAccount : accounts) {
                    lockedAccount.lock.unlock();
                }
                this.lock.unlock();
                throw e;
            }
            accounts.add(account);
            account.lock.lock();
        }
        this.lock.unlock();
        var sum = 0;
        for (final var account : accounts) {
            sum += account.balance();
            account.lock.unlock();
        }
        return sum;
    }

    private static class Account {
        private int balance;
        private ReentrantLock lock;

        private Account() {
            this.balance = 0;
            this.lock = new ReentrantLock();
        }

        private int balance() {
            return this.balance;
        }

        private void deposit(final int value) {
            if (value < 0) {
                throw new RuntimeException("cannot deposit negative values");
            }
            if (this.balance > Integer.MAX_VALUE - value) {
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
