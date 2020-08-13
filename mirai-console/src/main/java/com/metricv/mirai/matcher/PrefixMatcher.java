package com.metricv.mirai.matcher;

import com.metricv.mirai.router.RoutingContext;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.SingleMessage;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * Prefix Matcher.
 *
 * Matches the beginning of a string.
 *
 * Does not support "customization" to avoid causing confusion.
 * MATCH_* or CATCH_* options are <b>IGNORED</b>.
 */
public class PrefixMatcher implements Matcher{
    String wantedPrefix = "";
    String lastRemainder = "";

    public PrefixMatcher(String prefix) {
        this.wantedPrefix = prefix;
    }

    @Override
    public boolean isMatch(RoutingContext context, SingleMessage msg) {
        return (msg instanceof PlainText && ((PlainText) msg).getContent().startsWith(wantedPrefix));
    }

    /**
     * See if the string starts with the wanted prefix.
     * @param context DNC.
     * @param msg The single message to be matched for.
     * @return The remainder of the string, with prefix stripped.
     */
    @Override
    public MatchResult getMatch(@Nullable RoutingContext context, SingleMessage msg) {
        String msgRaw = (msg instanceof PlainText) ? ((PlainText) msg).getContent().strip() : null;
        if(msgRaw == null) return MatchResult.notMatch();
            else if (!msgRaw.startsWith(wantedPrefix)) return MatchResult.notMatch();
            else return MatchResult.match(wantedPrefix, new PlainText(msgRaw.substring(wantedPrefix.length()).strip()));
    }

    /**
     * Same as getMatch.
     * @param context DNC.
     * @param msg The single message to be matched for.
     * @return The remainder of the string, with prefix stripped.
     */
    @Override
    public SingleMessage getMatchRemainder(@Nullable RoutingContext context, final SingleMessage msg) {
        return getMatch(context, msg).getMatchRemainder();
    }

    /**
     * Default options. Immutable for PrefixMatcher.
     * @return Enumset of default options.
     */
    @Override
    public EnumSet<MatchOption> getDefaultOpts() {
        return EnumSet.of (
            MatchOption.SEEK_ADJ,
            MatchOption.MATCH_PART,
            MatchOption.RETAIN,
            MatchOption.CATCH_PART
        );
    }

    /**
     * Current option is always default option for PrefixMatcher.
     * @return Enumset of default options.
     */
    @Override
    public EnumSet<MatchOption> getCurrentOpts() {
        return getDefaultOpts();
    }
}
