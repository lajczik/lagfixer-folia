package xyz.lychee.lagfixer.nms.v1_21_R2;

import ca.spottedleaf.moonrise.common.util.CoordinateUtils;
import ca.spottedleaf.moonrise.patches.chunk_system.level.entity.ChunkEntitySlices;
import io.papermc.paper.threadedregions.TickData;
import io.papermc.paper.threadedregions.TickRegions;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.level.ChunkPos;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.objects.AbstractSupportNms;
import xyz.lychee.lagfixer.objects.RegionsEntityRaport;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

public class SupportNms extends AbstractSupportNms {
    public SupportNms(LagFixer plugin) {
        super(plugin);
    }

    @Override
    public TickReport getTickReport() {
        long currTime = System.nanoTime();
        DoubleArrayList tpsByRegion = new DoubleArrayList();
        DoubleArrayList msptByRegion = new DoubleArrayList();

        for (World world : Bukkit.getWorlds()) {
            ((CraftWorld) world).getHandle().regioniser.computeForAllRegions(region -> {
                TickData.TickReportData report = region.getData().getRegionSchedulingHandle().getTickReport15s(currTime);
                if (report != null) {
                    tpsByRegion.add(report.tpsData().segmentAll().average());
                    msptByRegion.add(report.timePerTickData().segmentAll().average() / 1_000_000.0D);
                }
            });
        }

        if (tpsByRegion.isEmpty()) {
            return new TickReport(20, 0);
        }

        int middle = tpsByRegion.size() >> 1;
        double medTps;
        double medMspt;
        if ((tpsByRegion.size() & 1) == 0) {
            medTps = (tpsByRegion.getDouble(middle - 1) + tpsByRegion.getDouble(middle)) / 2.0d;
            medMspt = (msptByRegion.getDouble(middle - 1) + msptByRegion.getDouble(middle)) / 2.0d;
        } else {
            medTps = tpsByRegion.getDouble(middle);
            medMspt = msptByRegion.getDouble(middle);
        }

        return new TickReport(medMspt, medTps);
    }
}