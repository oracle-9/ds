package extern;

import static java.lang.System.out;

public final class Increment implements Runnable {
    public void run() {
        final var N = 100;
        final var threadId = Thread.currentThread().getId();
        for (var i = 0; i < N; ++i) {
            out.println("thread %d: %d".formatted(threadId, i));
        }
        out.println("thread %d finished".formatted(threadId));
    }
}
