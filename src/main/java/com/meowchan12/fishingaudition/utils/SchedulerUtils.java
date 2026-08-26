package com.meowchan12.fishingaudition.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class SchedulerUtils {

    private static boolean isFolia = false;

    private static Method getAsyncSchedulerMethod;
    private static Method getGlobalRegionSchedulerMethod;
    
    private static Method runNowAsyncMethod;
    private static Method runAtFixedRateAsyncMethod;
    
    private static Method executeGlobalMethod;
    private static Method runAtFixedRateGlobalMethod;
    private static Method runDelayedGlobalMethod;
    
    private static Method getEntitySchedulerMethod;
    private static Method executeEntityMethod;
    private static Method runAtFixedRateEntityMethod;
    private static Method runDelayedEntityMethod;
    private static Method cancelTaskMethod;

    static {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            isFolia = true;
            
            getAsyncSchedulerMethod = Bukkit.class.getMethod("getAsyncScheduler");
            getGlobalRegionSchedulerMethod = Bukkit.class.getMethod("getGlobalRegionScheduler");
            
            Class<?> asyncSchedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");
            runNowAsyncMethod = asyncSchedulerClass.getMethod("runNow", Plugin.class, Consumer.class);
            runAtFixedRateAsyncMethod = asyncSchedulerClass.getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class, TimeUnit.class);
            
            Class<?> globalRegionSchedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            executeGlobalMethod = globalRegionSchedulerClass.getMethod("execute", Plugin.class, Runnable.class);
            runAtFixedRateGlobalMethod = globalRegionSchedulerClass.getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class);
            runDelayedGlobalMethod = globalRegionSchedulerClass.getMethod("runDelayed", Plugin.class, Consumer.class, long.class);
            
            getEntitySchedulerMethod = Entity.class.getMethod("getScheduler");
            Class<?> entitySchedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
            executeEntityMethod = entitySchedulerClass.getMethod("execute", Plugin.class, Runnable.class, Runnable.class, long.class);
            runAtFixedRateEntityMethod = entitySchedulerClass.getMethod("runAtFixedRate", Plugin.class, Consumer.class, Runnable.class, long.class, long.class);
            runDelayedEntityMethod = entitySchedulerClass.getMethod("runDelayed", Plugin.class, Consumer.class, Runnable.class, long.class);
            
            Class<?> scheduledTaskClass = Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask");
            cancelTaskMethod = scheduledTaskClass.getMethod("cancel");
            
        } catch (Exception e) {
            isFolia = false;
        }
    }

    public static boolean isFolia() {
        return isFolia;
    }

    public interface ScheduledTask {
        void cancel();
    }

    public static void runAsync(Plugin plugin, Runnable runnable) {
        if (isFolia) {
            try {
                Object scheduler = getAsyncSchedulerMethod.invoke(null);
                Consumer<?> consumer = task -> runnable.run();
                runNowAsyncMethod.invoke(scheduler, plugin, consumer);
            } catch (Exception e) {
                com.meowchan12.fishingaudition.Main.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Scheduler error", e);
            }
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
        }
    }

    public static ScheduledTask runTimerAsync(Plugin plugin, Runnable runnable, long delayTicks, long periodTicks) {
        if (isFolia) {
            try {
                Object scheduler = getAsyncSchedulerMethod.invoke(null);
                Consumer<?> consumer = task -> runnable.run();
                Object foliaTask = runAtFixedRateAsyncMethod.invoke(scheduler, plugin, consumer, delayTicks * 50, periodTicks * 50, TimeUnit.MILLISECONDS);
                return () -> cancelFoliaTask(foliaTask);
            } catch (Exception e) {
                com.meowchan12.fishingaudition.Main.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Scheduler error", e);
                return () -> {};
            }
        } else {
            BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delayTicks, periodTicks);
            return task::cancel;
        }
    }

    public static void runTask(Plugin plugin, Runnable runnable) {
        if (isFolia) {
            try {
                Object scheduler = getGlobalRegionSchedulerMethod.invoke(null);
                executeGlobalMethod.invoke(scheduler, plugin, runnable);
            } catch (Exception e) {
                com.meowchan12.fishingaudition.Main.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Scheduler error", e);
            }
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    public static ScheduledTask runTaskTimer(Plugin plugin, Runnable runnable, long delayTicks, long periodTicks) {
        if (isFolia) {
            try {
                Object scheduler = getGlobalRegionSchedulerMethod.invoke(null);
                Consumer<?> consumer = task -> runnable.run();
                long delay = delayTicks < 1 ? 1 : delayTicks;
                Object foliaTask = runAtFixedRateGlobalMethod.invoke(scheduler, plugin, consumer, delay, periodTicks);
                return () -> cancelFoliaTask(foliaTask);
            } catch (Exception e) {
                com.meowchan12.fishingaudition.Main.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Scheduler error", e);
                return () -> {};
            }
        } else {
            BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, runnable, delayTicks, periodTicks);
            return task::cancel;
        }
    }

    public static void runAtEntity(Plugin plugin, Entity entity, Runnable runnable) {
        if (isFolia) {
            try {
                Object scheduler = getEntitySchedulerMethod.invoke(entity);
                executeEntityMethod.invoke(scheduler, plugin, runnable, null, 1L);
            } catch (Exception e) {
                com.meowchan12.fishingaudition.Main.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Scheduler error", e);
            }
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    public static ScheduledTask runTimerAtEntity(Plugin plugin, Entity entity, Consumer<ScheduledTask> taskConsumer, long delayTicks, long periodTicks) {
        if (isFolia) {
            try {
                Object scheduler = getEntitySchedulerMethod.invoke(entity);
                long delay = delayTicks < 1 ? 1 : delayTicks;
                Consumer<?> consumer = task -> {
                    taskConsumer.accept(() -> cancelFoliaTask(task));
                };
                Object foliaTask = runAtFixedRateEntityMethod.invoke(scheduler, plugin, consumer, null, delay, periodTicks);
                return () -> cancelFoliaTask(foliaTask);
            } catch (Exception e) {
                com.meowchan12.fishingaudition.Main.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Scheduler error", e);
                return () -> {};
            }
        } else {
            SpigotTaskWrapper wrapper = new SpigotTaskWrapper();
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                taskConsumer.accept(wrapper);
            }, delayTicks, periodTicks);
            wrapper.setTask(bukkitTask);
            return wrapper;
        }
    }

    public static ScheduledTask runTaskLaterAtEntity(Plugin plugin, Entity entity, Runnable runnable, long delayTicks) {
        if (isFolia) {
            try {
                Object scheduler = getEntitySchedulerMethod.invoke(entity);
                long delay = delayTicks < 1 ? 1 : delayTicks;
                Consumer<?> consumer = task -> runnable.run();
                Object foliaTask = runDelayedEntityMethod.invoke(scheduler, plugin, consumer, null, delay);
                return () -> cancelFoliaTask(foliaTask);
            } catch (Exception e) {
                com.meowchan12.fishingaudition.Main.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Scheduler error", e);
                return () -> {};
            }
        } else {
            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks);
            return task::cancel;
        }
    }

    private static void cancelFoliaTask(Object foliaTask) {
        try {
            if (foliaTask != null && cancelTaskMethod != null) {
                cancelTaskMethod.invoke(foliaTask);
            }
        } catch (Exception e) {
            com.meowchan12.fishingaudition.Main.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Scheduler error", e);
        }
    }

    private static class SpigotTaskWrapper implements ScheduledTask {
        private BukkitTask task;
        
        public void setTask(BukkitTask task) {
            this.task = task;
        }

        @Override
        public void cancel() {
            if (task != null) {
                task.cancel();
            }
        }
    }
}
