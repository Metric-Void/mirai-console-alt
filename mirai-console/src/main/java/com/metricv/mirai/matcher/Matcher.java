package com.metricv.mirai.matcher;

import com.metricv.mirai.router.Routing;
import com.metricv.mirai.router.RoutingContext;
import net.mamoe.mirai.message.data.SingleMessage;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * Matches an element.
 *
 * <p>Beware of "options". Different matchers have different interpretations of options. </p>
 *
 * <p>"MATCH_*" and "CATCH_*" options are handled by matchers individually.
 * Check their individual JavaDocs to get details on how they handle these options. </p>
 *
 * <p>"SEEK_*" and "DISPOSE/RETAIN" are handled by {@link Routing}.
 * Their behavior should be consistent, but beware what each matcher define as "remainder". </p>
 */
public interface Matcher {
    /**
     * Whether a single message element is a match.
     * @param msg The message to match for.
     * @return Indicates whether this field is a match.
     */
    public boolean isMatch(RoutingContext context, SingleMessage msg);

    /**
     * Match a single message. Return object differs from matcher to matcher.
     * @param msg The single message to be matched for.
     * @return Object matched. [Optional] . Empty if no match.
     */
    public MatchResult getMatch(RoutingContext context, final SingleMessage msg);

    /**
     * Get the remainder after a single match.
     * @deprecated Use getMatch() instead. Its result contains a remainder field.
     * @return Remainder packed within a SingleMessage.
     */
    @Deprecated
    public SingleMessage getMatchRemainder(RoutingContext context, final SingleMessage msg);

    /**
     * Get the default options for this specific matcher.
     * @return An EnumSet of options.
     */
    public EnumSet<MatchOption> getDefaultOpts();

    /**
     * Get current options for this specific matcher.
     * @return An EnumSet of options.
     */
    public EnumSet<MatchOption> getCurrentOpts();
}
