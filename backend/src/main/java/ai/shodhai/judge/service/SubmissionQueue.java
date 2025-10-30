package ai.shodhai.judge.service;

import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class SubmissionQueue {

    private final BlockingQueue<UUID> queue = new LinkedBlockingQueue<>();

    public void enqueue(UUID submissionId) {
        queue.offer(submissionId);
    }

    public UUID take() throws InterruptedException {
        return queue.take();
    }
}
