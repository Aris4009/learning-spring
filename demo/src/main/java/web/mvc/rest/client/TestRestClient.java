package web.mvc.rest.client;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

public class TestRestClient {

	private static final Logger log = LoggerFactory.getLogger(TestRestClient.class);

	public static void main(String[] args) {
		// 创建WebClient
		WebClient webClient = WebClient.create();
		WebClient webClient1 = WebClient.create("http://localhost:8080/api/test");

		Map<String, Object> map = new HashMap<>();
		map.put("1", 2);
		Mono<Map> res1 = webClient.post().uri("http://localhost:8080/api/test/post").body(Mono.just(map), Map.class)
				.retrieve().bodyToMono(Map.class);
		log.info("{}", res1.block());
		log.info("阻塞");
	}
}
