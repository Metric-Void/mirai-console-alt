package com.metricv.mirai.router;

import net.mamoe.mirai.message.MessageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * A parametrized routing. Constructed by adding parameters to a {@link SimpleRouting} with Routing::putParam().
 */
public class ParameterizedSimpleRouting extends SimpleRouting {
    Map<String, Object> parameters;

    /**
     * Copy constructor.
     * @param routing Routing to copy from
     */
    protected ParameterizedSimpleRouting(@NotNull SimpleRouting routing) {
        this.matcherChain = routing.matcherChain;
        this.target = routing.target;
        this.matchParallel = routing.matchParallel;
    }

    /**
     * Add parameter to this routing.
     * @param name Name of the parameter
     * @param param Value.
     * @return self
     */
    public ParameterizedSimpleRouting putParam(String name, Object param) {
        parameters.put(name, param);
        return this;
    }

    /**
     * Attach parameters, then accept an event.
     * @param event {@link MessageEvent} from mirai.
     */
    @Override
    public void startRouting(@NotNull MessageEvent event) {
        RoutingResult initialResult = new RoutingResult();
        initialResult.eventSource = event;

        for(Map.Entry<String, Object> e : parameters.entrySet()) {
            initialResult.put(e.getKey(), e.getValue());
        }

        startRouting(event, initialResult);
    }
}
