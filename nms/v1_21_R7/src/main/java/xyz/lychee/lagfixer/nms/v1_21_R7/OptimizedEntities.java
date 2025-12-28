package xyz.lychee.lagfixer.nms.v1_21_R7;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.entity.vehicle.boat.ChestRaft;
import net.minecraft.world.entity.vehicle.boat.Raft;
import net.minecraft.world.entity.vehicle.minecart.*;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface OptimizedEntities {
    class ORaft extends Raft implements OptimizedEntities {
        ORaft(Raft r) {
            super((EntityType<? extends Raft>) r.getType(), r.level(), r::getDropItem);
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return false;
        }

        @Override
        public boolean isPushable() {
            return false;
        }
    }

    class OChestRaft extends ChestRaft implements OptimizedEntities {
        OChestRaft(ChestRaft cr) {
            super((EntityType<? extends ChestRaft>) cr.getType(), cr.level(), cr::getDropItem);
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return false;
        }

        @Override
        public boolean isPushable() {
            return false;
        }
    }

    class OBoat extends Boat implements OptimizedEntities {
        OBoat(Boat b) {
            super((EntityType<? extends Boat>) b.getType(), b.level(), b::getDropItem);

        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return false;
        }

        @Override
        public boolean isPushable() {
            return true;
        }
    }

    class OChestBoat extends ChestBoat implements OptimizedEntities {
        OChestBoat(ChestBoat cb) {
            super((EntityType<? extends ChestBoat>) cb.getType(), cb.level(), cb::getDropItem);

        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return false;
        }

        @Override
        public boolean isPushable() {
            return true;
        }
    }

    class OMinecart extends Minecart implements OptimizedEntities {
        OMinecart(Minecart m) {
            super(m.getType(), m.level());
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return false;
        }

        @Override
        public boolean isPushable() {
            return false;
        }
    }

    class OMinecartChest extends MinecartChest implements OptimizedEntities {
        OMinecartChest(MinecartChest mc) {
            super((EntityType<? extends MinecartChest>) mc.getType(), mc.level());

        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return false;
        }

        @Override
        public boolean isPushable() {
            return false;
        }
    }

    class OMinecartHopper extends MinecartHopper implements OptimizedEntities {
        OMinecartHopper(MinecartHopper mh) {
            super((EntityType<? extends MinecartHopper>) mh.getType(), mh.level());

        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return false;
        }

        @Override
        public boolean isPushable() {
            return false;
        }
    }

    class OMinecartFurnace extends MinecartFurnace implements OptimizedEntities {
        OMinecartFurnace(MinecartFurnace mf) {
            super((EntityType<? extends MinecartFurnace>) mf.getType(), mf.level());
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return false;
        }

        @Override
        public boolean isPushable() {
            return false;
        }
    }

    class OMinecartSpawner extends MinecartSpawner implements OptimizedEntities {
        OMinecartSpawner(MinecartSpawner other) {
            super((EntityType<? extends MinecartSpawner>) other.getType(), other.level());

            Optional.ofNullable(this.getSpawner().nextSpawnData)
                    .flatMap(sd -> sd.entityToSpawn().read("id", EntityType.CODEC))
                    .ifPresent(type ->
                            this.getSpawner().setEntityId(type, other.level(), other.random, this.blockPosition())
                    );
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return false;
        }

        @Override
        public boolean isPushable() {
            return false;
        }
    }

    class OMinecartTNT extends MinecartTNT implements OptimizedEntities {
        OMinecartTNT(MinecartTNT mt) {
            super((EntityType<? extends MinecartTNT>) mt.getType(), mt.level());
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return false;
        }

        @Override
        public boolean isPushable() {
            return false;
        }
    }
}