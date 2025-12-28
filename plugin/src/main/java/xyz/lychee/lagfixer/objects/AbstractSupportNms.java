package xyz.lychee.lagfixer.objects;

import lombok.Getter;
import xyz.lychee.lagfixer.LagFixer;

@Getter
public abstract class AbstractSupportNms {
    private final LagFixer plugin;

    public AbstractSupportNms(LagFixer plugin) {
        this.plugin = plugin;
    }

    public abstract TickReport getTickReport();

    public record TickReport(double mspt, double tps) {}
}

