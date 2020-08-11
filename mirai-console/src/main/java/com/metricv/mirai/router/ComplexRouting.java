package com.metricv.mirai.router;

import com.metricv.mirai.matcher.MatchOption;
import com.metricv.mirai.matcher.MatchResult;
import com.metricv.mirai.matcher.Matcher;
import net.mamoe.mirai.message.MessageEvent;
import net.mamoe.mirai.message.data.SingleMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
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
    /**
     * Counter used to index matcher nodes.
     * This is used when no node name has been specified.
     */
    private static int nodeCounter = 1;

    protected class MatcherNode {
        @NotNull Matcher innerMatcher;
        @Nullable String matcherName;
        int nodeId;
        /**
         * Whether this is the terminating node.
         * If set to true, the matching process will terminate here
         * And the attached function will be executed.
         */
        boolean isTerminate;

        /**
         * Root constructor. Increments counter and set own counter
         * @param matcher The matcher of this node.
         */
        MatcherNode(@NotNull Matcher matcher) {
            this.innerMatcher = matcher;
            this.target = (rr) -> {};
            this.nextNodes = new LinkedList<>();
            this.nodeId = nodeCounter++;
        }

        // TODO Add reasonable ways to construct nodes.

        /**
         * Target function to execute, if this is a terminating node.
         * Target function should be a {@link Consumer}<{@link RoutingResult}.
         *
         * If current node is not a terminating node, this value will not be used.
         */
        @NotNull Consumer<RoutingResult> target;

        /**
         * Nodes following this node.
         * Will be executed in a thread pool if there are multiple targets.
         * Will be directly executed if there is only one child.
         */
        @NotNull List<MatcherNode> nextNodes;

        /**
         * Execute this node.
         * @param rc Routing context, inherited from the previous node.
         * @param msg The remaining message.
         * @param rr RoutingResult up to this point.
         */
        protected void execute(RoutingContext rc, List<SingleMessage> msg, RoutingResult rr) {
            if(msg.size() == 0) return; // Nothing for us to match? Not cool.

            // Copy the routing result to avoid spilling rr in other threads.
            RoutingResult newRr = new RoutingResult(rr);

            SingleMessage curr;
            MatchResult result;
            if(innerMatcher.getCurrentOpts().contains(MatchOption.SEEK_NEXT)) {
                int localIndex = 0;
                do {
                    curr = msg.get(localIndex++);
                    result = innerMatcher.getMatch(rc, curr);
                } while (!result.isMatch() && localIndex < msg.size());
            } else {
                curr = msg.get(0);
                result = innerMatcher.getMatch(rc, curr);

            }

            if(result.isMatch()) {
                // Put result into Routing Result.
                newRr.put(matcherName == null? nodeId : matcherName, result.getMatchResult());

                // Generate MessageChain for child matchers
                List<SingleMessage> nextMsg = new LinkedList<>(msg);
                if(innerMatcher.getCurrentOpts().contains(MatchOption.RETAIN)) {
                    nextMsg.set(0, result.getMatchRemainder());
                } else {
                    nextMsg.remove(0);
                }

                // Termination node?
                if(this.isTerminate) {
                    target.accept(newRr);
                } else {
                    // Put child nodes into thread pool.
                    nextNodes.forEach((node) -> {
                        threadPool.submit(() -> {
                            node.execute(rc, nextMsg, newRr);
                        });
                    });
                }
            } // If not match, do nothing. Route stops here.
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

    // TODO Think of ways to initialize a routing and add nodes.

    @Override
    public void startRouting(@NotNull MessageEvent event) {
        // TODO finish this
    }
}
