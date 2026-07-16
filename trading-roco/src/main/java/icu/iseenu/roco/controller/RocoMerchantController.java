package icu.iseenu.roco.controller;

import com.fasterxml.jackson.databind.JsonNode;
import icu.iseenu.common.Result;
import icu.iseenu.roco.config.AppConfig;
import icu.iseenu.roco.model.MerchantMonitorResult;
import icu.iseenu.roco.model.TemplateData;
import icu.iseenu.roco.service.MerchantScreenshotAsyncService;
import icu.iseenu.roco.service.RocoMerchantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 洛克王国远行商人测试接口。
 * 仅测试上游数据获取和解析，不生成截图、不上传图片、不发送通知。
 */
@RestController
@RequestMapping("/api/roco/merchant")
@Slf4j
public class RocoMerchantController {

    private final AppConfig appConfig;
    private final MerchantScreenshotAsyncService merchantService;
    private final RocoMerchantService rocoMerchantService;

    public RocoMerchantController(AppConfig appConfig,
                                  MerchantScreenshotAsyncService merchantService,
                                  RocoMerchantService rocoMerchantService) {
        this.appConfig = appConfig;
        this.merchantService = merchantService;
        this.rocoMerchantService = rocoMerchantService;
    }

    /**
     * 测试远行商人上游接口及当前商品数据解析。
     *
     * @return 当前轮次及商品信息
     */
    @GetMapping("/test")
    public Result<TemplateData> testMerchantApi() {
        if (!appConfig.hasRocomApiKey()) {
            return Result.error(503, "ROCOM_API_KEY 未配置");
        }

        JsonNode rawData = merchantService.fetchGameData();
        if (rawData == null) {
            log.warn("远行商人测试接口调用失败，请查看上游请求日志");
            return Result.error(502, "远行商人上游接口调用失败，请查看服务日志");
        }

        TemplateData templateData = merchantService.processData(rawData);
        return Result.success("远行商人接口调用成功", templateData);
    }

    /**
     * 执行远行商人全链路测试。
     * 会真实生成截图、上传 ImgBB，并向所有已启用通知渠道发送测试通知。
     */
    @PostMapping("/test/full")
    public Result<MerchantMonitorResult> testFullChain() {
        MerchantMonitorResult monitorResult = rocoMerchantService.monitorMerchant();
        if (monitorResult.isFullChainSuccessful()) {
            return Result.success(monitorResult.getMessage(), monitorResult);
        }
        return Result.error(502, monitorResult.getMessage(), monitorResult);
    }
}
