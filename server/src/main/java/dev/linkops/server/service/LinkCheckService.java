package dev.linkops.server.service;

import dev.linkops.server.dto.LinkCheckResult;
import dev.linkops.server.dto.LinkCheckSummary;
import dev.linkops.server.entity.BacklinkSource;
import dev.linkops.server.mapper.BacklinkSourceMapper;
import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.net.ssl.SSLException;
import org.springframework.stereotype.Service;

/**
 * 入库初筛后半段:对外链入口发 GET 请求,判断是否还能访问。
 * 不调用模型。403/429/503 多为反爬或限流,记为 blocked 而不是失效,留给后续人工或研判。
 * 状态:ok | dead | tls_invalid | blocked | unreachable | error。
 */
@Service
public class LinkCheckService {

    /** 同时进行的请求数,避免短时间打出大量连接。 */
    private static final int MAX_CONCURRENCY = 20;
    private static final Duration TIMEOUT = Duration.ofSeconds(15);
    private static final int UPDATE_BATCH_SIZE = 100;
    private static final String USER_AGENT =
            "Mozilla/5.0 (compatible; linkops-lite/0.1; +https://github.com/imshiwen/linkops-lite)";

    private final BacklinkSourceMapper mapper;
    private final HttpClient http = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    public LinkCheckService(BacklinkSourceMapper mapper) {
        this.mapper = mapper;
    }

    public LinkCheckSummary check(String onlyStatus, int limit) {
        long start = System.currentTimeMillis();
        List<BacklinkSource> sources = mapper.listByScreenStatus(onlyStatus, limit);
        Semaphore permits = new Semaphore(MAX_CONCURRENCY);
        List<LinkCheckResult> results = new ArrayList<>();

        // 虚拟线程:每个请求一个线程,信号量控制并发上限
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<LinkCheckResult>> futures = new ArrayList<>();
            for (BacklinkSource s : sources) {
                futures.add(pool.submit(() -> {
                    permits.acquire();
                    try {
                        return checkOne(s);
                    } finally {
                        permits.release();
                    }
                }));
            }
            for (Future<LinkCheckResult> f : futures) {
                results.add(f.get());
            }
        } catch (Exception e) {
            throw new IllegalStateException("link check interrupted", e);
        }

        for (int i = 0; i < results.size(); i += UPDATE_BATCH_SIZE) {
            mapper.updateCheckResults(results.subList(i, Math.min(i + UPDATE_BATCH_SIZE, results.size())));
        }
        Map<String, Long> byStatus = results.stream()
                .collect(Collectors.groupingBy(LinkCheckResult::status, Collectors.counting()));
        return new LinkCheckSummary(results.size(), byStatus, System.currentTimeMillis() - start);
    }

    private LinkCheckResult checkOne(BacklinkSource s) {
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(s.getUrl().strip()))
                    .timeout(TIMEOUT)
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "text/html,*/*")
                    .GET()
                    .build();
            HttpResponse<Void> resp = http.send(req, HttpResponse.BodyHandlers.discarding());
            String finalUrl = resp.uri().toString();
            return new LinkCheckResult(s.getId(), classify(resp.statusCode()), resp.statusCode(),
                    finalUrl.equals(s.getUrl().strip()) ? null : finalUrl, null);
        } catch (Exception e) {
            return new LinkCheckResult(s.getId(), classify(e), null, null, describe(e));
        }
    }

    static String classify(int code) {
        if (code >= 200 && code < 400) return "ok";
        if (code == 404 || code == 410) return "dead";
        if (code == 401 || code == 403 || code == 429 || code == 503) return "blocked";
        return "error";
    }

    /**
     * 异常只在证书问题上给出确定结论;超时、握手中断、连接或解析失败可能是本机网络
     * (如代理、DNS 污染)导致,记为 unreachable,需在海外环境复查,不直接判死。
     */
    static String classify(Throwable e) {
        for (Throwable cur = e; cur != null; cur = cur.getCause() == cur ? null : cur.getCause()) {
            if (cur instanceof CertificateException
                    || cur instanceof CertPathBuilderException
                    || cur instanceof CertPathValidatorException) {
                return "tls_invalid";
            }
        }
        Throwable root = rootCause(e);
        if (e instanceof HttpTimeoutException || root instanceof HttpTimeoutException
                || root instanceof UnknownHostException
                || root instanceof ConnectException
                || root instanceof SSLException) {
            return "unreachable";
        }
        return "error";
    }

    private static String describe(Throwable e) {
        Throwable root = rootCause(e);
        String msg = root.getMessage() == null ? "" : ": " + root.getMessage();
        String text = root.getClass().getSimpleName() + msg;
        return text.length() > 300 ? text.substring(0, 300) : text;
    }

    private static Throwable rootCause(Throwable e) {
        Function<Throwable, Throwable> next = Throwable::getCause;
        Throwable cur = e;
        while (next.apply(cur) != null && next.apply(cur) != cur) cur = next.apply(cur);
        return cur;
    }

    public List<Map<String, Object>> statusCounts() {
        return mapper.countByScreenStatus();
    }
}
