package com.metricv.mirai.router;

import com.metricv.mirai.matcher.Matcher;
import net.mamoe.mirai.message.MessageEvent;
import net.mamoe.mirai.message.data.SingleMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * A complex routing resembling a graph that represents a state machine.
 * With the exception that each state have multiple subsequent states that will be executed in parallel.
 *
 * This routing is memory-heavy: With each layer, the message will be copied
 */
public class ComplexRouting implements Routing{
    protected class MatcherNode {
        Matcher innerMatcher;
        /**
         * Whether this is the terminating node.
         * If set to true, the matching process will terminate here
         * And the attached function will be executed.
         */
        boolean isTerminate;

        /**
         * Target function to execute, if this is a terminating node.
         * Target function should be a {@link Consumer}<{@link RoutingResult}.
         *
         * If current node is not a terminating node, this value will not be used.
         */
        @Nullable Consumer<RoutingResult> target;

        /**
         * Nodes following this node.
         * Will be executed in a thread pool if there are multiple targets.
         * Will be directly executed if there is only one child.
         */
        @Nullable List<MatcherNode> nextNodes;

        /**
         *
         * @param rc
         * @param msg
         */
        protected void execute(RoutingContext rc, List<SingleMessage> msg, RoutingResult rr) {
            if(msg.size() == 0) {
                // We're done here. Let's call the consumer.
                if(target != null) target.accept(rr);
                return;
            }
            SingleMessage curr = msg.get(0);
            // TODO finish this
        }
    }

    private MatcherNode entryNode;

    /**
     * Thread pool specifically for this complex routing.
     * Matcher procedures are put into this thread pool during routing process.
     */
    private ThreadPoolExecutor threadPool;

    ComplexRouting() {
        // Let's hardcode this for now. No matcher should take more than 30 seconds right?
        // This is even enough for some light OCR jobs
        threadPool = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                30L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>());
    }

    @Override
    public void startRouting(@NotNull MessageEvent event) {

    }
}
