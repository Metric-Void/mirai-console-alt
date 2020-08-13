package com.metricv.mirai.matcher;

import net.mamoe.mirai.message.data.SingleMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Wraps the results of a match.
 */
public class MatchResult {
    boolean isMatch;
    @NotNull Object matchResult;
    @Nullable SingleMessage matchRemainder;

    MatchResult(boolean isMatch, @NotNull Object matchResult, @Nullable SingleMessage matchRemainder) {
        this.isMatch = isMatch;
        this.matchRemainder = matchRemainder;
        this.matchResult = matchResult;
    }

    public static MatchResult notMatch() {
        return new MatchResult(false, Optional.empty(), null);
    }

    public static MatchResult match(@NotNull Object matchResult, @Nullable SingleMessage matchRemainder) {
        return new MatchResult(true, matchResult, matchRemainder);
    }

    public boolean isMatch() {
        return isMatch;
    }

    public void setMatch(boolean match) {
        isMatch = match;
    }

    @NotNull public Object getMatchResult() {
        return matchResult;
    }

    public void setMatchResult(@NotNull Object matchResult) {
        this.matchResult = matchResult;
    }

    public @Nullable SingleMessage getMatchRemainder() {
        return matchRemainder;
    }

    public void setMatchRemainder(@Nullable SingleMessage matchRemainder) {
        this.matchRemainder = matchRemainder;
    }
}
