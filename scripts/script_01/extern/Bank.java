package extern;

public final class Bank {
    private Bank() {}

    public static class Account {
        private int balance;

        private Account(final int balance) {
            this.balance = balance;
        }

        public static Account withBalance(final int balance) {
            return new Account(balance);
        }

        public int balance() {
            return this.balance;
        }

        public void deposit(final int value) {
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
    }
}
