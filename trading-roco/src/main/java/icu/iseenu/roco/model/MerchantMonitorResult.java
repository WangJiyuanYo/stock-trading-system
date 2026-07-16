package icu.iseenu.roco.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 远行商人完整监控流程执行结果。
 */
@Data
public class MerchantMonitorResult {

    private boolean dataFetched;
    private boolean screenshotUploaded;
    private boolean notificationTriggered;
    private String message;
    private String imageUrl;
    private TemplateData merchantData;
    private List<String> notificationChannels = new ArrayList<>();

    public boolean isFullChainSuccessful() {
        return dataFetched && screenshotUploaded && notificationTriggered;
    }
}
