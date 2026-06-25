package xyz.lychee.lagfixer.objects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeprecatedSupportNms implements ISupportNms {
    @Override
    public TickReport getTickReport() {
        return FALLBACK_TICK_REPORT;
    }
}