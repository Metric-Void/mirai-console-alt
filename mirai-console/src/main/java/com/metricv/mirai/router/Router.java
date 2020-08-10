package com.metricv.mirai.router;

import net.mamoe.mirai.console.MiraiConsoleLogger;
import net.mamoe.mirai.message.FriendMessageEvent;
import net.mamoe.mirai.message.GroupMessageEvent;
import net.mamoe.mirai.message.TempMessageEvent;
import org.bouncycastle.util.Properties;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * A router consisting of multiple routings.
 * All routings will be executed in parallel when message is received.
 *
 * Having only one router is advised. Use getIInstance() to get an instance instead of constructing one.
 */
public class Router {
    private class RoutingConfig {
        /**
         * The actual routing.
         */
        Routing routing;

        /**
         * "GroupID". Using the name of your plugin is recommended.
         * This value helps you find a routing you want to operate on.
         * For example, you can easily activate, deactivate, or remove a named routing.
         */
        String groupId;

        /**
         * "RouteID". Name of this route.
         * This value helps you find a routing you want to operate on.
         * For example, you can easily activate, deactivate, or remove a named routing.
         */
        String routeId;

        /**
         * Indicates whether this routing is activated.
         */
        boolean isActive;
    }

    private static Router instance = null;
    private static int POOL_THREADS = 64;

    /**
     * The common thread pool. All messages will reach this thread pool.
     */
    ThreadPoolExecutor threadPool;
    /*
     * Routings are separated by nature.
     * Separating them into different groups increases efficiency by a little.
     */

    private Collection<Routing> friendMsgRoutings;
    private Collection<Routing> tempMsgRoutings;
    private Collection<Routing> groupMsgRoutings;

    private Router() {
        friendMsgRoutings = new LinkedList<Routing>();
        tempMsgRoutings = new LinkedList<Routing>();
        groupMsgRoutings = new LinkedList<Routing>();
        try {
            POOL_THREADS = Integer.parseInt(Properties.getPropertyValue("mirai.msgthreads"));
            MiraiConsoleLogger.INSTANCE.invoke("Using " + POOL_THREADS + " threads as from arguments.");
        } catch (Exception ignore) {
            MiraiConsoleLogger.INSTANCE.invoke("Using " + POOL_THREADS + " threads as default.");
        }
        threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(POOL_THREADS);
    }

    public static Router getInstance() {
        instance = instance == null? new Router() : instance;
        return instance;
    }

    public void routeFriendMsg(FriendMessageEvent event) {
        for(Routing routing: friendMsgRoutings) {
            threadPool.submit(() -> {
                routing.startRouting(event);
            });
        }
    }

    public void routeTemporaryMsg(TempMessageEvent event) {
        for(Routing routing: tempMsgRoutings) {
            threadPool.submit(()->{
                routing.startRouting(event);
            });
        }
    }

    public void routeGroupMsg(GroupMessageEvent event) {
        for(Routing routing: groupMsgRoutings) {
            threadPool.submit(()->{
                routing.startRouting(event);
            });
        }
    }

    public void shutdown() {
        threadPool.shutdown();
        Router.instance = null;
    }

    public void addGroupRouting(Routing route) {
        groupMsgRoutings.add(route);
    }

    public void addFriendRouting(Routing route) {
        friendMsgRoutings.add(route);
    }

    public void addTempRouting(Routing route) {
        tempMsgRoutings.add(route);
    }
}
