package xyz.lychee.lagfixer.nms.v1_20_R2;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.*;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftBoat;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftMinecart;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftMinecartChest;
import org.bukkit.entity.Vehicle;
import xyz.lychee.lagfixer.modules.VehicleMotionReducerModule;

import java.util.IdentityHashMap;
import java.util.function.Function;

public class VehicleMotionReducer extends VehicleMotionReducerModule.NMS {
    private static final IdentityHashMap<Class<? extends Entity>, Function<Entity, Entity>> VEHICLES = new IdentityHashMap<>(10);

    static {
        VEHICLES.put(Boat.class, e -> new OptimizedEntities.OBoat((Boat) e));
        VEHICLES.put(ChestBoat.class, e -> new OptimizedEntities.OChestBoat((ChestBoat) e));

        VEHICLES.put(MinecartChest.class, e -> new OptimizedEntities.OMinecartChest((MinecartChest) e));
        VEHICLES.put(MinecartHopper.class, e -> new OptimizedEntities.OMinecartHopper((MinecartHopper) e));
        VEHICLES.put(MinecartFurnace.class, e -> new OptimizedEntities.OMinecartFurnace((MinecartFurnace) e));
        VEHICLES.put(MinecartSpawner.class, e -> new OptimizedEntities.OMinecartSpawner((MinecartSpawner) e));
        VEHICLES.put(MinecartTNT.class, e -> new OptimizedEntities.OMinecartTNT((MinecartTNT) e));
        VEHICLES.put(Minecart.class, e -> new OptimizedEntities.OMinecart((Minecart) e));
    }

    public VehicleMotionReducer(VehicleMotionReducerModule module) {
        super(module);
    }

    @Override
    public boolean optimizeVehicle(Vehicle vehicle) {
        if (vehicle instanceof CraftBoat) {
            if (!this.getModule().isBoat()) return false;

            return this.processEntity(((CraftBoat) vehicle).getHandle());
        } else if (vehicle instanceof CraftMinecart) {
            if (!this.getModule().isMinecart()) return false;

            if (vehicle instanceof CraftMinecartChest && getModule().isMinecart_remove_chest()) {
                AbstractMinecartContainer mc = ((CraftMinecartChest) vehicle).getHandle();
                mc.clearContent();
                mc.removeVehicle();
                return true;
            }

            return this.processEntity(((CraftMinecart) vehicle).getHandle());
        }
        return false;
    }

    private boolean processEntity(Entity original) {
        if (original instanceof OptimizedEntities) return false;

        Function<Entity, ? extends Entity> factory = VEHICLES.get(original.getClass());
        if (factory == null) return false;

        Entity newVehicle = factory.apply(original);
        newVehicle.setSilent(true);
        copyLocation(original, newVehicle);
        copyItems(original, newVehicle);

        original.removeVehicle();
        original.level().addFreshEntity(newVehicle);
        return true;
    }

    private void copyItems(Entity from, Entity to) {
        if (from instanceof ContainerEntity && to instanceof ContainerEntity) {
            for (int i = 0; i < ((ContainerEntity) from).getContainerSize(); i++) {
                ItemStack is = ((ContainerEntity) from).getItem(i);
                if (!is.isEmpty()) {
                    ((ContainerEntity) to).setItem(i, is.copyAndClear());
                }
            }
            ((ContainerEntity) from).clearContent();
        }
    }

    private void copyLocation(Entity from, Entity to) {
        to.setPos(from.xo, from.yo, from.zo);
        to.xo = from.xo;
        to.yo = from.yo;
        to.zo = from.zo;

        float yaw = Location.normalizeYaw(from.yRotO);
        to.setYRot(yaw);
        to.yRotO = yaw;
        to.setYHeadRot(yaw);
    }
}