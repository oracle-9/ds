public class Bank {
    public static class Account {
        private int balance;

        private Account(final int balance) {
            this.balance = balance;
        }

        public static Account withBalance(final int balance) {
            return new Account(balance);
        }

        public int balance() {
            return balance;
        }

        public void deposit(final int value) {
            balance += value;
        }
    }
}
