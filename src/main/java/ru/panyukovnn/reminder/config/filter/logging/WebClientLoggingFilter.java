package ru.panyukovnn.reminder.config.filter.logging;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;
import ru.panyukovnn.reminder.config.filter.ClientRequestBodyDecorator;
import ru.panyukovnn.reminder.config.filter.ClientResponseBodyDecorator;
import ru.panyukovnn.reminder.config.filter.WebFilterUtil;

/**
 * ExchangeFilterFunction логирования запросов/ответов.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebClientLoggingFilter implements ExchangeFilterFunction {

    private final WebFilterUtil webFilterUtil;

    @NotNull
    @Override
    public Mono<ClientResponse> filter(@NotNull ClientRequest request, @NotNull ExchangeFunction next) {
        return Mono.deferContextual(ctx -> {
            WebClientLoggingState state = new WebClientLoggingState(request.url().toString(), ctx, webFilterUtil);

            state.logRequest(request.method().name(), request.url().toString(), request.headers());

            ClientRequest clientRequestWithBodyReading = ClientRequest.from(request)
                    .body((originalRequest, context) -> request
                            .body()
                            .insert(new ClientRequestBodyDecorator(originalRequest, dataBuffer -> state.logRequestBody(dataBuffer, request.headers())),
                                    context))
                    .build();

            return next.exchange(clientRequestWithBodyReading)
                    .map(clientResponse -> new ClientResponseBodyDecorator(
                            clientResponse,
                            responseBodyItem -> state.logResponse(responseBodyItem, clientResponse.statusCode(), clientResponse.headers().asHttpHeaders())));
        });
    }
}
