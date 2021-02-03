package web.mvc._4_2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

	@Bean
	public WebSocketHandler webSocketHandler() {
		return new MyHandler();
	}

	@Bean
	public HandshakeInterceptor handshakeInterceptor() {
		return new MyInterceptor();
	}

	@Bean
	public HandshakeInterceptor httpHandshakeInterceptor() {
		return new HttpSessionHandshakeInterceptor();
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(webSocketHandler(), "/myHandler")
				.addInterceptors(handshakeInterceptor(), httpHandshakeInterceptor()).setAllowedOrigins("*");
	}

}
