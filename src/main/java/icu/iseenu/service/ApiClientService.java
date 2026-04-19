package icu.iseenu.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * 远程 API 调用服务
 * 使用 Spring WebFlux ??WebClient 进行 HTTP 请求
 */
@Service
public class ApiClientService {

    @Value("${app.api.base-url:https://api.example.com}")
    private String baseUrl;

    @Value("${app.api.timeout:30000}")
    private int timeout;

    private final WebClient webClient;

    public ApiClientService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * GET 请求
     *
     * @param uri         请求路径
     * @param responseType 响应类型
     * @return 响应对象
     */
    public <T> Mono<T> get(String uri, Class<T> responseType) {
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(responseType)
                .timeout(Duration.ofMillis(timeout));
    }

    /**
     * GET 请求（带查询参数??
     *
     * @param uri          请求路径
     * @param queryParams  查询参数
     * @param responseType 响应类型
     * @return 响应对象
     */
    public <T> Mono<T> get(String uri, Map<String, Object> queryParams, Class<T> responseType) {
        return webClient.get()
                .uri(uriBuilder -> {
                    queryParams.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(responseType)
                .timeout(Duration.ofMillis(timeout));
    }

    /**
     * POST 请求
     *
     * @param uri         请求路径
     * @param body        请求??
     * @param responseType 响应类型
     * @return 响应对象
     */
    public <T> Mono<T> post(String uri, Object body, Class<T> responseType) {
        return webClient.post()
                .uri(uri)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(responseType)
                .timeout(Duration.ofMillis(timeout));
    }

    /**
     * PUT 请求
     *
     * @param uri         请求路径
     * @param body        请求??
     * @param responseType 响应类型
     * @return 响应对象
     */
    public <T> Mono<T> put(String uri, Object body, Class<T> responseType) {
        return webClient.put()
                .uri(uri)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(responseType)
                .timeout(Duration.ofMillis(timeout));
    }

    /**
     * DELETE 请求
     *
     * @param uri         请求路径
     * @param responseType 响应类型
     * @return 响应对象
     */
    public <T> Mono<T> delete(String uri, Class<T> responseType) {
        return webClient.delete()
                .uri(uri)
                .retrieve()
                .bodyToMono(responseType)
                .timeout(Duration.ofMillis(timeout));
    }

    /**
     * 同步 GET 请求（阻塞式，慎用）
     *
     * @param uri         请求路径
     * @param responseType 响应类型
     * @return 响应对象
     */
    public <T> T getSync(String uri, Class<T> responseType) {
        return get(uri, responseType).block();
    }

    /**
     * 同步 POST 请求（阻塞式，慎用）
     *
     * @param uri         请求路径
     * @param body        请求??
     * @param responseType 响应类型
     * @return 响应对象
     */
    public <T> T postSync(String uri, Object body, Class<T> responseType) {
        return post(uri, body, responseType).block();
    }
}
