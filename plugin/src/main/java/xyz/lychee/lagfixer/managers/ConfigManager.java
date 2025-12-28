package xyz.lychee.lagfixer.managers;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.Language;
import xyz.lychee.lagfixer.objects.AbstractManager;
import xyz.lychee.lagfixer.utils.MessageUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

@Getter
public class ConfigManager extends AbstractManager implements Listener {
    @Getter
    private static ConfigManager instance;
    private TextComponent prefix;
    private String legacyPrefix;

    public ConfigManager(LagFixer plugin) {
        super(plugin);
        instance = this;
    }

    @Override
    public void load() throws IOException {
        FileConfiguration cfg = this.getPlugin().getConfig();
        InputStream originalCfg = this.getPlugin().getResource("config.yml");
        if (originalCfg != null) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(originalCfg));
            this.formatConfig(cfg, new File(this.getPlugin().getDataFolder(), "config.yml"), yaml);
        }

        YamlConfiguration lang = Language.getYaml();
        InputStream originalLang = this.getPlugin().getResource("lang.yml");
        if (originalLang != null) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(originalLang));
            this.formatConfig(lang, new File(this.getPlugin().getDataFolder(), "lang.yml"), yaml);
        }

        Language.getMainValues().clear();
        lang.getConfigurationSection("messages.Main").getValues(true).forEach((key, val) -> {
            if (val instanceof String) {
                Language.getMainValues().put(key, (String) val);
            }
        });

        this.legacyPrefix = MessageUtils.fixColors(null, this.getPlugin().getConfig().getString("main.prefix"));
        this.prefix = Component.text(this.legacyPrefix).clickEvent(ClickEvent.openUrl("https://modrinth.com/plugin/lagfixer"));
        if (this.getPlugin().getConfig().getBoolean("main.prefix_hover")) {
            this.prefix = this.prefix.hoverEvent(HoverEvent.showText(Component.text(MessageUtils.fixColors(null, this.legacyPrefix + "&fLagFixer &e" + this.getPlugin().getDescription().getVersion() + "\n &8{*} &7Click to open plugin in spigotmc!"))));
        }
    }

    private void formatConfig(FileConfiguration cfg, File file, YamlConfiguration original) {
        YamlConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);

        original.getValues(true).forEach((key, value) -> {
            Object newValue = fileConfig.get(key);
            if (newValue != null && !(value instanceof ConfigurationSection) && !Objects.equals(newValue, value)) {
                Object val = value.getClass().isInstance(newValue) ? newValue : value;
                original.set(key, val);
            }
        });

        try {
            cfg.loadFromString(original.saveToString());
            cfg.save(file);
        } catch (IOException | InvalidConfigurationException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void disable() {
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}

