final class Increment implements Runnable {
    public void run() {
        final var N = 100;
        for (var i = 0; i < N; i++) {
            System.out.println(i);
        }
    }
}
