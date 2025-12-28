package xyz.lychee.lagfixer.hooks;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.Getter;
import me.lucko.spark.api.Spark;
import me.lucko.spark.api.SparkProvider;
import me.lucko.spark.api.statistic.StatisticWindow;
import me.lucko.spark.api.statistic.misc.DoubleAverageInfo;
import me.lucko.spark.api.statistic.types.DoubleStatistic;
import me.lucko.spark.api.statistic.types.GenericStatistic;
import org.bukkit.Bukkit;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.ErrorsManager;
import xyz.lychee.lagfixer.managers.HookManager;
import xyz.lychee.lagfixer.objects.AbstractHook;

@Getter
public class SparkHook extends AbstractHook {
    private static @Getter SparkMonitor spark;
    private ScheduledTask task;

    public SparkHook(LagFixer plugin, HookManager manager) {
        super(plugin, "spark", manager);
    }

    @Override
    public void load() {
        spark = new SparkMonitor();
        this.task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(getPlugin(), task -> {
            if (ErrorsManager.getInstance().isEnabled() && Bukkit.getOnlinePlayers().size() > 20) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spark profiler open");
            }
        }, 72000L, 72000L);

    }

    @Override
    public void disable() {
        if (this.task != null) {
            this.task.cancel();
        }
    }

    public static class SparkMonitor {
        private final Spark spark = SparkProvider.get();

        public double getTps() {
            DoubleStatistic<StatisticWindow.TicksPerSecond> tps = this.spark.tps();
            if (tps == null) {
                return 0.0d;
            }
            return tps.poll(StatisticWindow.TicksPerSecond.SECONDS_10);
        }

        public double getMspt() {
            GenericStatistic<DoubleAverageInfo, StatisticWindow.MillisPerTick> mspt = this.spark.mspt();
            if (mspt == null) {
                return 0.0d;
            }
            return mspt.poll(StatisticWindow.MillisPerTick.SECONDS_10).median();
        }

        public double cpuProcess() {
            return this.spark.cpuProcess().poll(StatisticWindow.CpuUsage.SECONDS_10);
        }

        public double cpuSystem() {
            return this.spark.cpuSystem().poll(StatisticWindow.CpuUsage.SECONDS_10);
        }
    }
}

