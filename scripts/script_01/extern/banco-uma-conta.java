class Bank {
    private static class Account {
        private int balance;

        private Account(final int balance) {
            this.balance = balance;
        }

        public int balance() {
            return balance;
        }

        public boolean deposit(final int value) {
            balance += value;
            return true;
        }
    }

    private Account savings = new Account(0);

    public int balance() {
        return savings.balance();
    }

    public boolean deposit(final int value) {
        return savings.deposit(value);
    }
}
