package ru.vdomrachev.study.devhands.rest.config;

import net.openhft.affinity.AffinityLock;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@Profile("pinned")
public class PinnedThreadTomcatConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> pinnedThreadCustomizer() {
        return factory -> {
            factory.addConnectorCustomizers(connector -> {
                connector.getProtocolHandler().setExecutor(new PinnedThreadExecutor());
            });
        };
    }

    static class PinnedThreadExecutor extends org.apache.tomcat.util.threads.ThreadPoolExecutor {
        public PinnedThreadExecutor() {
            super(25, 200, 60, java.util.concurrent.TimeUnit.SECONDS,
                    new java.util.concurrent.LinkedBlockingQueue<>(),
                    new PinnedThreadFactory("pinned-tomcat"));
        }
    }

    static class PinnedThreadFactory implements ThreadFactory {
        private final AtomicInteger threadCount = new AtomicInteger(0);
        private final String namePrefix;
        private final int availableCores = Runtime.getRuntime().availableProcessors();

        PinnedThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            int coreId = threadCount.getAndIncrement() % availableCores;
            return new Thread(() -> {
                try (AffinityLock lock = AffinityLock.acquireLock(coreId)) {
                    r.run();
                }
            }, namePrefix + "-" + threadCount.get());
        }
    }
}