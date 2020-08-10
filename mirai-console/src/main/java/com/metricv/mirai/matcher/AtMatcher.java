package com.metricv.mirai.matcher;

import com.metricv.mirai.router.RoutingContext;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.AtAll;
import net.mamoe.mirai.message.data.SingleMessage;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * Matches an @.
 * This matcher does not care about MatchOptions.
 * It will always match an At or AtAll element, and return qqid as match.
 * qqid=0 means @all.
 *
 * Does not care about MATCH_* or CATCH_* options.
 * It will always match the @At, and returns the id being "ATted".
 * - In case of -1 (match anyone), the one who's being actually "ATted" will be returned as the matched result.
 * - In case of 0 (@all), 0 will be the matched result.
 */
public class AtMatcher implements Matcher {
    public long qqid;

    /**
     * Create a AtMatcher.
     * qqid=-1 means anyone.
     * qqid=0 means @all.
     * @param qqid
     */
    public AtMatcher(long qqid) {
        this.qqid = qqid;
    }

    @Override
    public boolean isMatch(RoutingContext context, SingleMessage msg) {
        if(msg instanceof At) {
            return qqid == -1 || ((At) msg).getTarget() == qqid;
        } else if (msg instanceof AtAll) {
            return qqid == 0;
        }
        return false;
    }

    @Override
    public MatchResult getMatch(RoutingContext context, SingleMessage msg) {
        if(msg instanceof At) {
            if (qqid == -1 || ((At) msg).getTarget() == qqid)
                return MatchResult.match(((At) msg).getTarget(), null);
        } else if (msg instanceof AtAll) {
            return MatchResult.match(0, null);
        }
        return MatchResult.notMatch();
    }

    @Override
    public SingleMessage getMatchRemainder(RoutingContext context, SingleMessage msg) {
        return null;
    }

    @Override
    public EnumSet<MatchOption> getDefaultOpts() {
        return EnumSet.of(
                MatchOption.MATCH_ALL,
                MatchOption.SEEK_ADJ,
                MatchOption.DISPOSE,
                MatchOption.CATCH_PART
        );
    }

    @Override
    public EnumSet<MatchOption> getCurrentOpts() {
        return getDefaultOpts();
    }
}
