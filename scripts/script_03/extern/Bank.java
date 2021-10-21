package extern;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

class Bank {
    private final Map<Integer, Account> accounts;

    private Bank() {
        this.accounts = new HashMap<>();
    }

    private Account getAccountOrThrow(final int accountId) {
        final var account = this.accounts.get(accountId);
        if (account == null) {
            throw new RuntimeException("no such account");
        }
        return account;
    }

    public int createAccount() {
        final var account = new Account();
        final var accountId = this.accounts.size();
        this.accounts.put(accountId, account);
        return accountId;
    }

    public void closeAccount(final int accountId) {
        this.accounts.remove(accountId);
    }

    public int balance(final int accountId) {
        return this.getAccountOrThrow(accountId).balance();
    }

    public void deposit(final int accountId, final int value) {
        this.getAccountOrThrow(accountId).deposit(value);
    }

    public void withdraw(final int accountId, final int value) {
        this.getAccountOrThrow(accountId).withdraw(value);
    }

    public void transfer(
        final int srcAccountId,
        final int destAccountId,
        final int value
    ) {
        final var srcAccount = this.getAccountOrThrow(srcAccountId);
        srcAccount.withdraw(value);
        try {
            this.getAccountOrThrow(destAccountId).deposit(value);
        } catch (final RuntimeException e) {
            srcAccount.deposit(value);
        }
    }

    public int totalBalance() {
        return this.accounts
            .values()
            .stream()
            .mapToInt(Account::balance)
            .sum();
    }

    public int totalBalanceOf(final IntStream accountIds) {
        return accountIds
            .map(id -> this.accounts.get(id).balance())
            .sum();
    }

    private static class Account {
        private int balance;

        private Account() {
            this.balance = 0;
        }

        private int balance() {
            return this.balance;
        }

        private void deposit(final int value) {
            if (this.balance > Integer.MAX_VALUE - value) {
                throw new RuntimeException(
                    "value exceeds maximum account balance"
                );
            }
            this.balance += value;
        }

        private void withdraw(final int value) {
            if (value > this.balance) {
                throw new RuntimeException("value exceeds account balance");
            }
            this.balance -= value;
        }
    }
}
