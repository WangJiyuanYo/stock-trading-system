package icu.iseenu.roco.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import icu.iseenu.common.Result;
import icu.iseenu.roco.config.AppConfig;
import icu.iseenu.roco.model.MerchantMonitorResult;
import icu.iseenu.roco.model.TemplateData;
import icu.iseenu.roco.service.MerchantScreenshotAsyncService;
import icu.iseenu.roco.service.RocoMerchantService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RocoMerchantControllerTest {

    private final AppConfig appConfig = new AppConfig();
    private final MerchantScreenshotAsyncService merchantService =
            mock(MerchantScreenshotAsyncService.class);
    private final RocoMerchantService rocoMerchantService =
            mock(RocoMerchantService.class);
    private final RocoMerchantController controller =
            new RocoMerchantController(appConfig, merchantService, rocoMerchantService);

    @Test
    void shouldReturnConfigurationErrorWhenApiKeyIsMissing() {
        Result<TemplateData> result = controller.testMerchantApi();

        assertThat(result.getCode()).isEqualTo(503);
        assertThat(result.getMessage()).contains("ROCOM_API_KEY");
    }

    @Test
    void shouldReturnUpstreamErrorWhenFetchingDataFails() {
        appConfig.setRocomApiKey("test-key");
        when(merchantService.fetchGameData()).thenReturn(null);

        Result<TemplateData> result = controller.testMerchantApi();

        assertThat(result.getCode()).isEqualTo(502);
    }

    @Test
    void shouldReturnProcessedMerchantData() throws Exception {
        appConfig.setRocomApiKey("test-key");
        JsonNode rawData = new ObjectMapper().readTree("{\"merchantActivities\":[]}");
        TemplateData templateData = new TemplateData();
        templateData.setProductCount(2);

        when(merchantService.fetchGameData()).thenReturn(rawData);
        when(merchantService.processData(rawData)).thenReturn(templateData);

        Result<TemplateData> result = controller.testMerchantApi();

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isSameAs(templateData);
        assertThat(result.getData().getProductCount()).isEqualTo(2);
    }

    @Test
    void shouldReturnFullChainResult() {
        MerchantMonitorResult monitorResult = new MerchantMonitorResult();
        monitorResult.setDataFetched(true);
        monitorResult.setScreenshotUploaded(true);
        monitorResult.setNotificationTriggered(true);
        monitorResult.setMessage("远行商人全链路执行成功");
        when(rocoMerchantService.monitorMerchant()).thenReturn(monitorResult);

        Result<MerchantMonitorResult> result = controller.testFullChain();

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isSameAs(monitorResult);
    }

    @Test
    void shouldReturnFailureWhenFullChainIsIncomplete() {
        MerchantMonitorResult monitorResult = new MerchantMonitorResult();
        monitorResult.setDataFetched(true);
        monitorResult.setMessage("截图上传失败");
        when(rocoMerchantService.monitorMerchant()).thenReturn(monitorResult);

        Result<MerchantMonitorResult> result = controller.testFullChain();

        assertThat(result.getCode()).isEqualTo(502);
        assertThat(result.getData().isScreenshotUploaded()).isFalse();
    }
}
