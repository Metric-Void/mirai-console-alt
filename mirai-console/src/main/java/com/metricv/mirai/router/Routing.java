package com.metricv.mirai.router;

import net.mamoe.mirai.message.MessageEvent;
import org.jetbrains.annotations.NotNull;

public interface Routing {
    public void startRouting(@NotNull MessageEvent event);

    public static SimpleRouting serialRoute() {
        return new SimpleRouting(false);
    }
}
