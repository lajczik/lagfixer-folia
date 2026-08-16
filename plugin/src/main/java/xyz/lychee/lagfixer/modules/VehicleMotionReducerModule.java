package xyz.lychee.lagfixer.modules;

import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.inventory.PlayerInventory;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.AbstractModule;
import xyz.lychee.lagfixer.utils.ReflectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Getter
public class VehicleMotionReducerModule extends AbstractModule implements Listener {
    private NMS vehicleMotionReducer;
    private boolean forceLoad;

    private boolean minecart;
    private boolean minecart_collides;
    private boolean minecart_pushable;
    private boolean minecart_silent;

    private boolean boat;
    private boolean boat_collides;
    private boolean boat_pushable;
    private boolean boat_silent;

    public VehicleMotionReducerModule(LagFixer plugin, ModuleManager manager) {
        super(plugin, manager, Impact.LOW, "VehicleMotionReducer",
                new String[]{
                        "Optimizes all vehicles such as Boats and Minecarts.",
                        "Removes minecarts with chests spawned in mineshafts.",
                        "Particularly useful when minecarts are frequently used on the server.",
                        "Enhances server performance by optimizing vehicle mechanics and removing unnecessary entities."
                },
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTJjMjA1MGVjYTBlZmRkMDMxZTY1OGI5OTZjMjM5YmY3ZGEzYWVmODY1NjEyMzY3ZWQ5ZDg5NWFlN2EwZGE5ZiJ9fX0=");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityPlace(EntityPlaceEvent event) {
        Entity entity = event.getEntity();
        if (this.canContinue(entity.getWorld()) && entity instanceof Vehicle vehicle) {
            boolean cancel = this.vehicleMotionReducer.optimize(vehicle);

            if (cancel) {
                event.setCancelled(true);

                Player player = event.getPlayer();
                if (player != null && player.getGameMode() != GameMode.CREATIVE) {
                    PlayerInventory inventory = player.getInventory();

                    String mainHand = inventory.getItemInMainHand().getType().name().toLowerCase();
                    String offHand = inventory.getItemInOffHand().getType().name().toLowerCase();
                    if (mainHand.contains("boat") || mainHand.contains("minecart") || mainHand.contains("raft")) {
                        inventory.setItemInMainHand(null);
                    } else if (offHand.contains("boat") || offHand.contains("minecart") || offHand.contains("raft")) {
                        inventory.setItemInOffHand(null);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLoad(EntitiesLoadEvent e) {
        if (this.canContinue(e.getWorld())) return;

        for (Entity ent : e.getEntities()) {
            this.vehicleMotionReducer.optimize(ent);
        }
    }

    public boolean isEnabled(Entity ent) {
        return ent instanceof Minecart && this.minecart || ent instanceof Boat && this.boat;
    }

    @Override
    public void load() {
        Bukkit.getPluginManager().registerEvents(this, this.getPlugin());

        if (this.forceLoad) {
            RegionScheduler scheduler = Bukkit.getServer().getRegionScheduler();
            this.getAllowedWorlds().forEach(world -> {
                Map<SupportManager.RegionPos, List<Chunk>> regions = SupportManager.createRegionMap(world);
                regions.forEach((regionPos, chunks) -> {
                    Executor executor = task -> scheduler.execute(this.getPlugin(), world, regionPos.getX() << 3, regionPos.getZ() << 3, task);
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        for (Chunk chunk : chunks) {
                            Entity[] entities = chunk.getEntities();
                            for (Entity entity : entities) {
                                if (this.isEnabled(entity)) {
                                    this.vehicleMotionReducer.optimize(entity);
                                }
                            }
                        }
                    }, executor);
                });
            });
        }
    }

    @Override
    public boolean loadConfig() {
        this.vehicleMotionReducer = ReflectionUtils.createInstance("VehicleMotionReducer", this);

        this.forceLoad = this.getSection().getBoolean("force_load");

        this.minecart = this.getSection().getBoolean("minecart.enabled");
        if (this.minecart) {
            this.minecart_collides = this.getSection().getBoolean("minecart.collides");
            this.minecart_pushable = this.getSection().getBoolean("minecart.pushable");
            this.minecart_silent = this.getSection().getBoolean("minecart.silent");
        }

        this.boat = this.getSection().getBoolean("boat.enabled");
        if (this.boat) {
            this.boat_collides = this.getSection().getBoolean("boat.collides");
            this.boat_pushable = this.getSection().getBoolean("boat.pushable");
            this.boat_silent = this.getSection().getBoolean("boat.silent");
        }

        return this.vehicleMotionReducer != null;
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }

    @Getter
    public static abstract class NMS {
        private final VehicleMotionReducerModule module;

        public NMS(VehicleMotionReducerModule module) {
            this.module = module;
        }

        public abstract boolean optimize(Entity vehicle);
    }
}

