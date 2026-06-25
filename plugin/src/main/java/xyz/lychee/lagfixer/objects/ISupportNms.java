package xyz.lychee.lagfixer.objects;

public interface ISupportNms {
    TickReport FALLBACK_TICK_REPORT = new TickReport(0, 20);

    TickReport getTickReport();

    record TickReport(double mspt, double tps) {}
}