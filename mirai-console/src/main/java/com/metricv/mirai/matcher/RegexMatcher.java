package com.metricv.mirai.matcher;

import com.metricv.mirai.router.RoutingContext;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.SingleMessage;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Regular expression matcher.
 *
 * Default options are: MATCH_ALL, SEEK_ADJ, DISPOSE, CATCH_MATC.
 *
 * Return Behaviors differ depending on CATCH option.
 * CATCH_ALL: Returns the whole string.
 * CATCH_MATC: Returns a {@link java.util.regex.MatchResult} that contains elements matched.
 *
 * MATCH_PART may not work well. It works by adding ".*" to the start and end to the pattern you provided.
 * Please do understand the implications.
 */
public class RegexMatcher implements Matcher {
    Pattern pattern;
    EnumSet<MatchOption> opt;
    @RegExp String pattern_original = "";
    SingleMessage remainder = null;

    /**
     * Initialize a Regex matcher with patterns you compiled and default options.
     * This must match the whole message. No partial amendment will be performed.
     * @param pattern a java.util.regex.Pattern you compiled.
     */
    public RegexMatcher(@NotNull final Pattern pattern) {
        this.pattern_original = pattern.pattern();
        this.pattern = pattern;
        this.opt = EnumSet.of(
                MatchOption.MATCH_ALL,
                MatchOption.SEEK_ADJ,
                MatchOption.DISPOSE,
                MatchOption.CATCH_PART
        );
    }

    /**
     * Initialize a Regex matcher with the settings you provide.
     * ONLY CHOOSE ONE FROM EACH CATEGORY. Unexpected things will occur if conflicting rules are present.
     * MATCH_PART will be ignored. The whole message must match the pattern you provide.
     * @param pattern A compiled pattern.
     * @param opt Options. Use Enum.of() to construct options.
     */
    public RegexMatcher(@NotNull final Pattern pattern, @NotNull final EnumSet<MatchOption> opt) {
        this(pattern);
        this.opt = opt;
    }

    /**
     * Initialize a Regex matcher with default settings.
     * @param pattern The regular expression pattern.
     */
    public RegexMatcher(@RegExp final String pattern) {
        this(Pattern.compile(pattern));
    }

    /**
     * Initialize a Regex matcher with the settings you provide.
     * ONLY CHOOSE ONE FROM EACH CATEGORY. Unexpected things will occur if conflicting rules are present.
     * @param pattern The regular expression pattern.
     * @param opt Options. Use Enum.of() to construct options.
     */
    public RegexMatcher(@RegExp final String pattern, @NotNull final EnumSet<MatchOption> opt) {
        this.pattern_original = pattern;
        if (opt.contains(MatchOption.MATCH_PART)) {
            this.pattern = Pattern.compile(".*" + pattern + ".*");
        } else {
            this.pattern = Pattern.compile(pattern);
        }
        this.opt = opt;
    }

    public EnumSet<MatchOption> getDefaultOpts() {
        return EnumSet.of(
                MatchOption.MATCH_ALL,
                MatchOption.SEEK_ADJ,
                MatchOption.DISPOSE,
                MatchOption.CATCH_PART
        );
    }

    public EnumSet<MatchOption> getCurrentOpts() {
        return this.opt;
    }

    @Override
    public boolean isMatch(@Nullable RoutingContext context, final SingleMessage msg) {
        if (msg instanceof PlainText && pattern != null) {
            String content = ((PlainText) msg).getContent();
            return pattern.matcher(content).matches();
        } else {
            return false;
        }
    }

    @Override
    public MatchResult getMatch(@Nullable RoutingContext context, final SingleMessage msg) {
        if(!(msg instanceof PlainText)) return MatchResult.notMatch();
        String content = ((PlainText) msg).getContent();
        java.util.regex.Matcher matcher = pattern.matcher(content);

        // Check if we are asked to match the whole thing.
        if(matcher.matches()) {
            if (opt.contains(MatchOption.RETAIN)) {
                remainder = new PlainText(msg.contentToString());
            } else {    // DISPOSE.
                remainder = new PlainText(content.replaceFirst(pattern_original, "").trim());
            }

            if(opt.contains(MatchOption.CATCH_ALL)) {
                return MatchResult.match(content, remainder);
            } else {
                return MatchResult.match(matcher, remainder);
            }
        } else {
            return MatchResult.notMatch();
        }
    }

    @Override
    public SingleMessage getMatchRemainder(RoutingContext context, final SingleMessage msg) {
        return remainder;
    }
}
