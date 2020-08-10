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
    @NotNull Optional<Object> matchResult;
    @Nullable SingleMessage matchRemainder;

    MatchResult(boolean isMatch, @NotNull Optional<Object> matchResult, @Nullable SingleMessage matchRemainder) {
        this.isMatch = isMatch;
        this.matchRemainder = matchRemainder;
        this.matchResult = matchResult;
    }

    public static MatchResult notMatch() {
        return new MatchResult(false, Optional.empty(), null);
    }

    public static MatchResult match(@NotNull Object matchResult, @Nullable SingleMessage matchRemainder) {
        return new MatchResult(true, Optional.of(matchResult), matchRemainder);
    }

    public boolean isMatch() {
        return isMatch;
    }

    public void setMatch(boolean match) {
        isMatch = match;
    }

    @NotNull public Optional<Object> getMatchResult() {
        return matchResult;
    }

    public void setMatchResult(Optional<Object> matchResult) {
        this.matchResult = matchResult;
    }

    public SingleMessage getMatchRemainder() {
        return matchRemainder;
    }

    public void setMatchRemainder(SingleMessage matchRemainder) {
        this.matchRemainder = matchRemainder;
    }
}
