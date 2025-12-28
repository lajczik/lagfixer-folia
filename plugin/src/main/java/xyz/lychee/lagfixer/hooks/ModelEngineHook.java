package xyz.lychee.lagfixer.hooks;

import com.ticxo.modelengine.api.ModelEngineAPI;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.HookManager;
import xyz.lychee.lagfixer.objects.AbstractHook;

public class ModelEngineHook extends AbstractHook implements HookManager.ModelContainer {
    public ModelEngineHook(LagFixer plugin, HookManager manager) {
        super(plugin, "ModelEngine", manager);
    }

    public boolean hasModel(@Nullable Entity entity) {
        return entity != null && ModelEngineAPI.isModeledEntity(entity.getUniqueId());
    }

    @Override
    public void load() {
    }

    @Override
    public void disable() {
    }
}