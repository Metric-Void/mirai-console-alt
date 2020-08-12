package com.metricv.mirai.matcher;

import com.metricv.mirai.router.ComplexRouting;
import com.metricv.mirai.router.RoutingContext;
import net.mamoe.mirai.message.data.SingleMessage;

import java.util.EnumSet;

/**
 * A placeholder. Matches everything, does not consume the message element.
 *
 * This is useful when you want to create an empty node in {@link ComplexRouting} that controls route flow, but
 * do not change the message itself.
 */
public class PsuedoMatcher implements Matcher{

    @Override
    public boolean isMatch(RoutingContext context, SingleMessage msg) {
        return true;
    }

    @Override
    public MatchResult getMatch(RoutingContext context, SingleMessage msg) {
        return MatchResult.match(msg, msg);
    }

    @Override
    public SingleMessage getMatchRemainder(RoutingContext context, SingleMessage msg) {
        return msg;
    }

    @Override
    public EnumSet<MatchOption> getDefaultOpts() {
        return EnumSet.of(
                MatchOption.RETAIN,
                MatchOption.SEEK_ADJ,
                MatchOption.CATCH_ALL,
                MatchOption.MATCH_ALL
        );
    }

    @Override
    public EnumSet<MatchOption> getCurrentOpts() {
        return getDefaultOpts();
    }
}
