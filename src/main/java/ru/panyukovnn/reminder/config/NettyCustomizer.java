package ru.panyukovnn.reminder.config;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.WriteTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

import static io.netty.handler.timeout.IdleStateEvent.WRITER_IDLE_STATE_EVENT;

/**
 * Конфигурация netty.
 */
@Slf4j
@Component
public class NettyCustomizer implements WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {

    private static final int IDLE_TIMEOUT_S = 60;

    @Override
    public void customize(NettyReactiveWebServerFactory factory) {
        factory.addServerCustomizers(server ->
            server.doOnConnection(connection ->
                    connection.addHandlerFirst(
                            new IdleStateHandler(0, 0, IDLE_TIMEOUT_S) {
                                @Override
                                protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) {
                                    ctx.fireExceptionCaught(
                                            evt.state() == WRITER_IDLE_STATE_EVENT.state()
                                                    ? WriteTimeoutException.INSTANCE
                                                    : ReadTimeoutException.INSTANCE
                                    );
                                    ctx.write(new CloseWebSocketFrame());
                                    ctx.close();
                                }
                            }
                    ))
        );
    }
}