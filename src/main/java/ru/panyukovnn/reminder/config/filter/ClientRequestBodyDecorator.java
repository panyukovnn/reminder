package ru.panyukovnn.reminder.config.filter;

import jakarta.validation.constraints.NotNull;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.http.client.reactive.ClientHttpRequestDecorator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Обертка для логирования webClient запроса.
 */
public class ClientRequestBodyDecorator extends ClientHttpRequestDecorator {

    private final AtomicBoolean alreadyLogged = new AtomicBoolean(false);

    /**
     * Консьюмер, вызываемый при получении тела запроса.
     */
    private final Consumer<DataBuffer> requestBodyConsumer;

    public ClientRequestBodyDecorator(ClientHttpRequest delegate, Consumer<DataBuffer> requestBodyConsumer) {
        super(delegate);
        this.requestBodyConsumer = requestBodyConsumer;
    }

    @Override
    public @NotNull Mono<Void> writeWith(@NotNull Publisher<? extends DataBuffer> body) {
        boolean needToLog = alreadyLogged.compareAndSet(false, true);

        return needToLog
                ? super.writeWith(Flux.from(body).doOnNext(requestBodyConsumer))
                : super.writeWith(body);
    }

    @Override
    public @NotNull Mono<Void> writeAndFlushWith(@NotNull Publisher<? extends Publisher<? extends DataBuffer>> body) {
        boolean needToLog = alreadyLogged.compareAndSet(false, true);

        return needToLog
                ? super.writeWith(Flux.from(body).flatMap(it -> Flux.from(it).doOnNext(requestBodyConsumer)))
                : super.writeAndFlushWith(body);
    }
}