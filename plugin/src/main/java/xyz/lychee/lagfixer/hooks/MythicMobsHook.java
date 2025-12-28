package xyz.lychee.lagfixer.hooks;

import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.HookManager;
import xyz.lychee.lagfixer.objects.AbstractHook;

public class MythicMobsHook extends AbstractHook implements HookManager.ModelContainer {
    public MythicMobsHook(LagFixer plugin, HookManager manager) {
        super(plugin, "MythicMobs", manager);
    }

    public boolean hasModel(@Nullable Entity entity) {
        return entity != null && MythicBukkit.inst().getMobManager().isMythicMob(entity);
    }

    @Override
    public void load() {
    }

    @Override
    public void disable() {
    }
}

