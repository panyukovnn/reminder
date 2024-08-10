package ru.panyukovnn.reminder.config.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ClientHttpResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyExtractor;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Consumer;

/**
 * Обертка для извлечения webClient ответа.
 */
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("NullableProblems")
public class ClientResponseBodyDecorator implements ClientResponse {

    private final ClientResponse delegate;
    /**
     * Консьюмер, вызываемый при получении тела ответа.
     */
    private final Consumer<Object> responseBodyConsumer;

    @Override
    public HttpStatusCode statusCode() {
        return delegate.statusCode();
    }

    @Override
    public Headers headers() {
        return delegate.headers();
    }

    @Override
    public MultiValueMap<String, ResponseCookie> cookies() {
        return delegate.cookies();
    }

    @Override
    public ExchangeStrategies strategies() {
        return delegate.strategies();
    }

    @Override
    public <T> T body(BodyExtractor<T, ? super ClientHttpResponse> extractor) {
        return delegate.body(extractor);
    }

    @Override
    public <T> Mono<T> bodyToMono(Class<? extends T> elementClass) {
        return delegate.<T>bodyToMono(elementClass)
                .doOnNext(responseBodyConsumer);
    }

    @Override
    public <T> Mono<T> bodyToMono(ParameterizedTypeReference<T> elementTypeRef) {
        return delegate.bodyToMono(elementTypeRef)
                .doOnNext(responseBodyConsumer);
    }

    @Override
    public <T> Flux<T> bodyToFlux(Class<? extends T> elementClass) {
        return delegate.<T>bodyToFlux(elementClass)
                .doOnNext(responseBodyConsumer);
    }

    @Override
    public <T> Flux<T> bodyToFlux(ParameterizedTypeReference<T> elementTypeRef) {
        return delegate.bodyToFlux(elementTypeRef)
                .doOnNext(responseBodyConsumer);
    }

    @Override
    public Mono<Void> releaseBody() {
        return delegate.releaseBody();
    }

    @Override
    public <T> Mono<ResponseEntity<T>> toEntity(Class<T> bodyClass) {
        return delegate.toEntity(bodyClass);
    }

    @Override
    public <T> Mono<ResponseEntity<T>> toEntity(ParameterizedTypeReference<T> bodyTypeReference) {
        return delegate.toEntity(bodyTypeReference);
    }

    @Override
    public <T> Mono<ResponseEntity<List<T>>> toEntityList(Class<T> elementClass) {
        return delegate.toEntityList(elementClass);
    }

    @Override
    public <T> Mono<ResponseEntity<List<T>>> toEntityList(ParameterizedTypeReference<T> elementTypeRef) {
        return delegate.toEntityList(elementTypeRef);
    }

    @Override
    public Mono<ResponseEntity<Void>> toBodilessEntity() {
        return delegate.toBodilessEntity();
    }

    @Override
    public Mono<WebClientResponseException> createException() {
        return delegate.createException();
    }

    @Override
    public <T> Mono<T> createError() {
        return delegate.createError();
    }

    @Override
    public String logPrefix() {
        return delegate.logPrefix();
    }

    @Override
    public Builder mutate() {
        return ClientResponse.super.mutate();
    }
}
