package com.metricv.mirai.router;

import com.metricv.mirai.matcher.MatchOption;
import com.metricv.mirai.matcher.MatchResult;
import com.metricv.mirai.matcher.Matcher;
import com.metricv.mirai.matcher.PsuedoMatcher;
import net.mamoe.mirai.message.MessageEvent;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageSource;
import net.mamoe.mirai.message.data.SingleMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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

    public class MatcherNode {
        @NotNull Matcher innerMatcher;
        @Nullable String matcherName;
        int nodeId;
        /**
         * Whether this is the terminating node.
         * If set to true, the matching process will terminate here, and the attached function will be executed.
         *
         * This value overrides the list of children. If {@code isTerminate == true} and current node matched successfully,
         * route will always stop, and target function will always be executed.
         */
        boolean isTerminate;

        /**
         * Root constructor. Increments counter and set own counter
         *
         * @param matcherName Name of the matcher. Could be null, which means unnamed.
         * @param matcher The matcher of this node.
         */
        protected MatcherNode(@Nullable String matcherName, @NotNull Matcher matcher) {
            this.innerMatcher = matcher;
            this.matcherName = matcherName;
            this.target = (rr) -> {};
            this.nextNodes = new LinkedList<>();
            this.nodeId = nodeCounter++;
        }

        /**
         * Convenience constructor.
         * @param matcher The matcher of this node.
         */
        protected MatcherNode(@NotNull Matcher matcher) {
            this(null, matcher);
        }

        /**
         * Target function to execute, if this is a terminating node.
         * Target function should be a {@link Consumer}<{@link RoutingResult}.
         *
         * If current node is not a terminating node, this value will not be used.
         */
        @NotNull Consumer<RoutingResult> target;

        /**
         * Nodes following this node.
         * Will be executed in a thread pool after the current node get matcher.
         */
        @NotNull List<MatcherNode> nextNodes;

        /**
         * Execute this node.
         * @param rc Routing context, inherited from the previous node.
         * @param msg The remaining message.
         * @param rr RoutingResult up to this point. Will be passed on to child nodes.
         */
        protected void execute(RoutingContext rc, List<SingleMessage> msg, RoutingResult rr) {
            System.out.println((matcherName!=null? matcherName : "null") + " Executing on " + msg.toString());
            if(msg.size() == 0) {
                if(this.isTerminate) {
                    System.out.println((matcherName!=null? matcherName : "null") + " Executing on " + msg.toString() + "TARGET CALLED");
                    target.accept(rr);
                } else {
                    return;
                }
            }

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
                System.out.println((matcherName!=null? matcherName : "null") + " Executing on " + msg.toString() + "MATCHED");
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
                    System.out.println((matcherName!=null? matcherName : "null") + " Executing on " + msg.toString() + "TARGET CALLED");
                    target.accept(newRr);
                } else {
                    // Put child nodes into thread pool.
                    System.out.println((matcherName!=null? matcherName : "null") + " Executing on " + msg.toString() + "NO TERM-CONTINUE");
                    nextNodes.forEach((node) -> {
                        threadPool.submit(() -> {
                            node.execute(rc, nextMsg, newRr);
                        });
                    });
                }
            } else {
                System.out.println((matcherName!=null? matcherName : "null") + " Executing on " + msg.toString() + "NO MATCH");
                // If not match, do nothing. Route stops here.
            }

        }

        /**
         * Add a named child matcher to this node. Returns the child node.
         *
         * @param name Name of the child.
         * @param child The child matcher with name.
         * @return The newly created child node with given matcher.
         */
        public MatcherNode chainChild(String name, Matcher child) {
            MatcherNode childNode = new MatcherNode(name, child);
            nextNodes.add(childNode);
            ComplexRouting.this.allNodes.add(childNode);
            return childNode;
        }

        /**
         * Add an unnamed child matcher to this node. Returns the child node.
         *
         * @param child The child matcher with no name.
         * @return The newly created child node with given matcher.
         */
        public MatcherNode chainChild(Matcher child) {
            return chainChild(null, child);
        }

        /**
         * Creates a named new {@link MatcherNode} from given matcher, add it as child, and return self.
         * Child matcher WILL NOT be returned.
         *
         * This function is, generally, a bad idea. You will not have access to the newly created child node.
         * The only way you can fetch that new child node is to search it in parent class {@link ComplexRouting}.
         * @param name Name of the matcher
         * @param child The child matcher to add.
         * @return The node itself.
         */
        public MatcherNode addChild(String name, Matcher child) {
            MatcherNode childNode = new MatcherNode(name, child);
            nextNodes.add(childNode);
            ComplexRouting.this.allNodes.add(childNode);
            return this;
        }

        /**
         * Creates an unnamed new {@link MatcherNode} from given matcher, add it as child, and return self.
         * Child matcher WILL NOT be returned.
         *
         * This function is, generally, a bad idea. You will not have access to the newly created child node.
         * The only way you can fetch that new child node is to search it in parent class {@link ComplexRouting}.
         * @param child The child matcher to add.
         * @return The node itself.
         */
        public MatcherNode addChild(Matcher child) {
            return addChild(null, child);
        }

        /**
         * Add the given MatcherNode to this node as a child
         * Returns this node itself for convenience of adding multiple child.
         * Just don't form a loop.
         *
         * @param childNode The constructed MatcherNode.
         * @return The node itself.
         */
        public MatcherNode addChild(MatcherNode childNode) {
            this.nextNodes.add(childNode);
            ComplexRouting.this.allNodes.add(childNode);
            return this;
        }

        public boolean equals(Object other) {
            return (other instanceof MatcherNode) && (this.nodeId == ((MatcherNode) other).nodeId);
        }

        // TODO Add some static methods that generate template nodes.

        // TODO Add getter and setter methods.
        public void setTarget(@NotNull Consumer<RoutingResult> target) {
            this.target = target;
        }

        public MatcherNode setTerminate() {
            this.isTerminate = true;
            return this;
        }

        public MatcherNode unsetTerminate() {
            this.isTerminate = false;
            return this;
        }
    }

    /*
        Body of ComplexRouting.
     */

    /*
        Fields
     */
    private MatcherNode entryNode;
    private Set<MatcherNode> allNodes;
    private RoutingResult paramedPreResult;

    /**
     * Thread pool specifically for this complex routing.
     * Matcher procedures are put into this thread pool during routing process.
     */
    private ThreadPoolExecutor threadPool;

    /**
     * Private constructor. Initialize things.
     */
    private ComplexRouting() {
        // Let's hardcode this for now. No matcher should take more than 30 seconds right?
        // This is even enough for some light OCR jobs
        threadPool = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                30L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>());
        allNodes = new HashSet<>();
        paramedPreResult = new RoutingResult();
    }

    /*
        Methods
     */

    @Override
    public void startRouting(@NotNull MessageEvent event) {
        System.out.println("ComplexRoute: message from" + event.getSenderName());
        RoutingContext rc = new RoutingContext(event);
        List<SingleMessage> msg = event.getMessage();
        RoutingResult thisRun = new RoutingResult(paramedPreResult);
        thisRun.eventSource = event;

        if(event.getMessage().first(MessageSource.Key) != null) {
            thisRun.msgSource = event.getMessage().first(MessageSource.Key);
            msg = msg.subList(1, event.getMessage().getSize());
        }
        this.getRoot().execute(rc, msg, thisRun);
    }

    public MatcherNode getRoot() {
        return entryNode;
    }

    /**
     * Construct a routing with given Matcher as root.
     * This is a great way to construct a routing if all your commands start with the same prefix.
     *
     * Returns the constructed {@link ComplexRouting}, with given matcher as root node.
     * Use getRoot() to get that node and add child nodes to it.
     *
     * @param matcher A Matcher
     * @return A {@link ComplexRouting}.
     */
    public static ComplexRouting withRoot(Matcher matcher) {
        ComplexRouting newRoute = new ComplexRouting();
        MatcherNode newRoot = newRoute.new MatcherNode(matcher);
        newRoute.entryNode = newRoot;
        newRoute.allNodes.add(newRoot);
        return newRoute;
    }

    /**
     * Construct a routing with a PsuedoMatcher as root.
     * A PsuedoMatcher will not match anything. It simply carries on the message to its children.
     *
     * Returns the constructed {@link ComplexRouting}, with an empty root node.
     * Use getRoot() to get that node and add child nodes to it.
     *
     * @return A {@link ComplexRouting}.
     */
    public static ComplexRouting withBlankRoot() {
        return withRoot(new PsuedoMatcher());
    }

    public MatcherNode makeNode(Matcher matcher) {
        return new MatcherNode(matcher);
    }

    public MatcherNode makeNode(String name, Matcher matcher) {
        return new MatcherNode(name, matcher);
    }

    // TODO Think of ways to initialize a routing and add nodes.
}
