class Bank {
    private static class Account {
        private int balance;

        Account(final int balance) {
            this.balance = balance;
        }

        int balance() {
            return balance;
        }

        boolean deposit(final int value) {
            balance += value;
            return true;
        }
    }

    private Account savings = new Account(0);

    public int balance() {
        return savings.balance();
    }

    boolean deposit(final int value) {
        return savings.deposit(value);
    }
}
