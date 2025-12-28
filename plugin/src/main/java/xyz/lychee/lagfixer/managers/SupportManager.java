package xyz.lychee.lagfixer.managers;

import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.*;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.objects.AbstractManager;
import xyz.lychee.lagfixer.objects.AbstractSupportNms;
import xyz.lychee.lagfixer.objects.RegionsEntityRaport;
import xyz.lychee.lagfixer.utils.ReflectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

@Getter
@Setter
public class SupportManager extends AbstractManager {
    private static @Getter SupportManager instance;

    private final Map<String, String> versions = new HashMap<>();
    private final RegionsEntityRaport regionsReport = new RegionsEntityRaport();
    private AbstractSupportNms nms = null;
    private ScheduledTask task = null;

    public SupportManager(LagFixer plugin) {
        super(plugin);

        instance = this;

        this.versions.put("1.20.5", "v1_20_R4");
        this.versions.put("1.20.6", "v1_20_R4");
        this.versions.put("1.21", "v1_21_R1");
        this.versions.put("1.21.1", "v1_21_R1");
        this.versions.put("1.21.2", "v1_21_R2");
        this.versions.put("1.21.3", "v1_21_R2");
        this.versions.put("1.21.4", "v1_21_R3");
        this.versions.put("1.21.5", "v1_21_R4");
        this.versions.put("1.21.6", "v1_21_R5");
        this.versions.put("1.21.7", "v1_21_R5");
        this.versions.put("1.21.8", "v1_21_R5");
        this.versions.put("1.21.9", "v1_21_R6");
        this.versions.put("1.21.10", "v1_21_R6");
        this.versions.put("1.21.11", "v1_21_R7");
    }

    @Override
    public void load() {
        try {
            String version = ReflectionUtils.getVersion("SupportNms");
            Class<?> clazz = Class.forName("xyz.lychee.lagfixer.nms." + version + ".SupportNms");
            this.nms = (AbstractSupportNms) clazz.getConstructor(LagFixer.class).newInstance(this.getPlugin());
            this.getPlugin().getLogger().info(" &8• &rLoaded nms support ~ " + this.nms.getClass().getCanonicalName());
        } catch (Throwable ex) {
            this.getPlugin().getLogger().info("   &cOptimal support folia not found!");
            this.getPlugin().getLogger().info("   &7Supported versions: &e1.20.1 - 1.21.11");
            Bukkit.getPluginManager().disablePlugin(this.getPlugin());
            return;
        }

        this.task = Bukkit.getAsyncScheduler()
                .runAtFixedRate(
                        this.getPlugin(),
                        t -> {
                            LongAdder chunksAdder = new LongAdder();
                            LongAdder entitiesAdder = new LongAdder();
                            LongAdder playersAdder = new LongAdder();
                            LongAdder creaturesAdder = new LongAdder();
                            LongAdder itemsAdder = new LongAdder();
                            LongAdder vehiclesAdder = new LongAdder();
                            LongAdder projectilesAdder = new LongAdder();

                            List<CompletableFuture<Void>> futures = new ArrayList<>();

                            for (World world : Bukkit.getWorlds()) {
                                if (world.getPlayers().isEmpty()) continue;

                                playersAdder.add(world.getPlayerCount());

                                RegionScheduler scheduler = Bukkit.getServer().getRegionScheduler();

                                Map<RegionPos, List<Chunk>> regions = SupportManager.createRegionMap(world);
                                regions.forEach((regionPos, chunks) -> {
                                    chunksAdder.add(chunks.size());

                                    Executor executor = task -> scheduler.execute(this.getPlugin(), world, regionPos.getX() << 3, regionPos.getZ() << 3, task);
                                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                                        for (Chunk chunk : chunks) {
                                            for (Entity ent : chunk.getEntities()) {
                                                if (ent instanceof Mob) {
                                                    creaturesAdder.increment();
                                                } else if (ent instanceof Item) {
                                                    itemsAdder.increment();
                                                } else if (ent instanceof Projectile) {
                                                    projectilesAdder.increment();
                                                } else if (ent instanceof Vehicle) {
                                                    vehiclesAdder.increment();
                                                }
                                                entitiesAdder.increment();
                                            }
                                        }
                                    }, executor);

                                    futures.add(future);
                                });
                            }

                            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                                    .orTimeout(5, TimeUnit.SECONDS)
                                    .thenAccept(v -> {
                                        this.regionsReport.setChunks(chunksAdder);
                                        this.regionsReport.setEntities(entitiesAdder);
                                        this.regionsReport.setPlayers(playersAdder);
                                        this.regionsReport.setCreatures(creaturesAdder);
                                        this.regionsReport.setItems(itemsAdder);
                                        this.regionsReport.setProjectiles(projectilesAdder);
                                        this.regionsReport.setVehicles(vehiclesAdder);
                                    })
                                    .exceptionally(ex -> {
                                        this.getPlugin().printError(ex);
                                        return null;
                                    });
                        },
                        30,
                        30,
                        TimeUnit.SECONDS
                );
    }

    public static Map<RegionPos, List<Chunk>> createRegionMap(World world) {
        Map<RegionPos, List<Chunk>> regions = new HashMap<>();

        for (Chunk chunk : world.getLoadedChunks()) {
            int regionX = chunk.getX() >> 3;
            int regionZ = chunk.getZ() >> 3;

            RegionPos pos = new RegionPos(regionX, regionZ);
            regions.computeIfAbsent(pos, k -> new ArrayList<>()).add(chunk);
        }
        return regions;
    }

    @Override
    public void disable() {
        if (this.task != null) {
            this.task.cancel();
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Getter
    public static final class RegionPos {
        private final int x;
        private final int z;
        private final int hash;

        public RegionPos(int x, int z) {
            this.x = x;
            this.z = z;
            this.hash = (x * 7340033) ^ z;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof RegionPos other)) return false;

            return this.x == other.x && this.z == other.z;
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}

