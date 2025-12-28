package xyz.lychee.lagfixer.managers;

import com.sun.management.OperatingSystemMXBean;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.hooks.SparkHook;
import xyz.lychee.lagfixer.objects.AbstractManager;
import xyz.lychee.lagfixer.objects.AbstractSupportNms;

import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class MonitorManager extends AbstractManager {
    private static @Getter MonitorManager instance;
    private final OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
    private final int interval;
    private double tps = 0;
    private double mspt = 0;
    private double cpuProcess = 0;
    private double cpuSystem = 0;
    private long ramFree = 0;
    private long ramUsed = 0;
    private long ramMax = 0;
    private long ramTotal = 0;
    private ScheduledTask task;

    public MonitorManager(LagFixer plugin) {
        super(plugin);

        instance = this;
        this.interval = plugin.getConfig().getInt("main.monitor_interval");
    }

    public double format(double d) {
        return Math.round(d * 100.0D) / 100.0D;
    }

    public long formatBytes(long bytes) {
        return bytes / 1024L / 1024L;
    }

    @Override
    public void load() throws Exception {
        this.task = Bukkit.getAsyncScheduler().runAtFixedRate(this.getPlugin(), task -> {
            SparkHook.SparkMonitor spark = SparkHook.getSpark();
            if (spark == null) {
                AbstractSupportNms.TickReport report = SupportManager.getInstance().getNms().getTickReport();
                this.tps = this.format(report.tps());
                this.mspt = this.format(report.mspt());
                this.cpuProcess = this.format(this.osBean.getProcessCpuLoad() * 100D);
                this.cpuSystem = this.format(this.osBean.getSystemLoadAverage() * 100D);
            } else {
                this.tps = this.format(spark.getTps());
                this.mspt = this.format(spark.getMspt());
                this.cpuProcess = this.format(spark.cpuProcess() * 100D);
                this.cpuSystem = this.format(spark.cpuSystem() * 100D);
            }

            Runtime r = Runtime.getRuntime();
            this.ramFree = this.formatBytes(r.freeMemory());
            this.ramUsed = this.formatBytes(r.totalMemory() - r.freeMemory());
            this.ramMax = this.formatBytes(r.maxMemory());
            this.ramTotal = this.formatBytes(r.totalMemory());
        }, 5L, this.interval, TimeUnit.SECONDS);
    }

    @Override
    public void disable() throws Exception {
        if (this.task != null && !this.task.isCancelled()) {
            this.task.cancel();
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

