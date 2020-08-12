package com.metricv.mirai.router;

import com.metricv.mirai.matcher.MatchOption;
import com.metricv.mirai.matcher.MatchResult;
import com.metricv.mirai.matcher.Matcher;
import net.mamoe.mirai.message.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageSource;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.SingleMessage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class SimpleRouting implements Routing{

    /**
     * The configuration of a matcher, in a chain of matchers from a routing.
     */
    static class MatcherConfig {
        String name;
        Matcher matcher;
        EnumSet<MatchOption> opts;
    }

    protected ArrayList<MatcherConfig> matcherChain;
    protected boolean matchParallel;
    protected Consumer<RoutingResult> target;

    protected SimpleRouting() {
        matcherChain = new ArrayList<>();
    }

    protected SimpleRouting(boolean isParallel) {
        this();
        matchParallel = isParallel;
    }

    public static SimpleRouting serialRoute() {
        return new SimpleRouting(false);
    }

    /**
     * Put a customized parameter into this routing.
     * Routing will become {@link ParameterizedSimpleRouting} . Original Routing is not affected.
     * Parameters will appear in RoutingResult.
     * @param name Name of the parameter
     * @param param Value.
     * @return self, but in the form of a {@link ParameterizedSimpleRouting}.
     */
    public ParameterizedSimpleRouting putParam(String name, Object param) {
        ParameterizedSimpleRouting pr = new ParameterizedSimpleRouting(this);
        pr.putParam(name, param);
        return pr;
    }

    /**
     * Accept and process an event.
     * @param event {@link MessageEvent} from mirai.
     */
    public void startRouting(@NotNull MessageEvent event) {
        RoutingResult initialResult = new RoutingResult();
        initialResult.eventSource = event;
        startRouting(event, initialResult);
    }

    protected void startRouting(@NotNull MessageEvent event, RoutingResult initialResult) {
        MessageChain incomingMessage = event.getMessage();
        List<SingleMessage> contentList = new ArrayList<>(incomingMessage);

        RoutingContext context = new RoutingContext(event);

        // Isolate MessageSource.
        if(incomingMessage.first(MessageSource.Key) != null) {
            initialResult.msgSource = incomingMessage.first(MessageSource.Key);
            contentList.remove(initialResult.msgSource);
        }

        // Start matching
        for(int index=0; index < matcherChain.size(); index += 1) {
            Optional<Object> matchResult = Optional.empty();
            MatcherConfig curr = matcherChain.get(index);
            MatchResult result = curr.matcher.getMatch(context, contentList.get(index));

            if(curr.opts.contains(MatchOption.SEEK_NEXT)) {
                while(index < matcherChain.size()) {
                    if (result.isMatch()) {
                        matchResult = result.getMatchResult();
                        break;
                    }
                    index += 1;
                    result = curr.matcher.getMatch(context, contentList.get(index));
                }
                if(matchResult.isEmpty()) return;
            } else {
                if(curr.matcher.isMatch(context, contentList.get(index))) {
                    matchResult = result.getMatchResult();
                } else {
                    return;
                }
            }

            if(curr.opts.contains(MatchOption.RETAIN)) {
                SingleMessage retained = result.getMatchRemainder();
                if (!(retained instanceof PlainText) || !((PlainText) retained).getContent().equals("")) {
                    contentList.set(index, retained); // does not destroy the original contents.
                    index -= 1; // will be re-incremented by loop
                }
            }

            if(!curr.opts.contains(MatchOption.CATCH_NONE)) {
                if(curr.name != null) {
                    initialResult.put(curr.name, matchResult);
                } else {
                    initialResult.put(matchResult);
                }
            }
        }
        // Routing has ended. All route match.
        target.accept(initialResult);
    }

    /**
     * Attach a matcher that matches the next new message.
     * The previous matcher will be forcifully set to "DISPOSE".
     * @param nextMatcher The next matcher. Construct one yourself.
     * @return self
     */
    public SimpleRouting thenMatch(Matcher nextMatcher) {
        MatcherConfig mc = new MatcherConfig();
        mc.matcher = nextMatcher;
        mc.name = null;
        mc.opts = nextMatcher.getCurrentOpts();

        matcherChain.add(mc);
        return this;
    }

    /**
     * Set a functional interface to this message.
     * @param target A {@link Consumer} accepting a {@link RoutingResult}.
     * @return self
     */
    public SimpleRouting setTarget(Consumer<RoutingResult> target) {
        this.target = target;
        return this;
    }
}
