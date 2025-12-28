package xyz.lychee.lagfixer.nms.v1_21_R5;

import com.google.common.util.concurrent.AtomicDouble;
import io.papermc.paper.threadedregions.TickData;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.*;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.objects.AbstractSupportNms;
import xyz.lychee.lagfixer.objects.RegionsEntityRaport;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

public class SupportNms extends AbstractSupportNms {
    public SupportNms(LagFixer plugin) {
        super(plugin);
    }

    @Override
    public TickReport getTickReport() {
        long currTime = System.nanoTime();
        AtomicDouble tpsByRegion = new AtomicDouble();
        AtomicDouble msptByRegion = new AtomicDouble();
        LongAdder regions = new LongAdder();

        for (World world : Bukkit.getWorlds()) {
            ((CraftWorld) world).getHandle().regioniser.computeForAllRegions(region -> {
                TickData.TickReportData report = region.getData().getRegionSchedulingHandle().getTickReport15s(currTime);
                if (report != null) {
                    tpsByRegion.addAndGet(report.tpsData().segmentAll().average());
                    msptByRegion.addAndGet(report.timePerTickData().segmentAll().average() / 1_000_000.0D);
                    regions.increment();
                }
            });
        }

        int regionsInt = regions.intValue();
        if (regionsInt == 0) {
            return new TickReport(0, 20);
        }

        return new TickReport(msptByRegion.get() / regionsInt, tpsByRegion.get() / regionsInt);
    }
}