package com.zooplus.sej.msone;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.*;
import reactor.ipc.netty.http.server.HttpServer;

@SpringBootApplication
@EnableAutoConfiguration
@EnableJpaRepositories
public class MsOneApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsOneApplication.class, args);
	}

	@Bean
	public RouterFunction<?> router(UserHandler userHandler) {
		return RouterFunctions.route(RequestPredicates.POST("/users/create"), userHandler::createUser);
	}

	@Bean
	public HttpServer server(RouterFunction<?> router) {
		HttpHandler reqHandler = RouterFunctions.toHttpHandler(router);
		HttpServer server = HttpServer.create(8090);
		server.start(new ReactorHttpHandlerAdapter(reqHandler));
		return server;
	}

	@Bean
	public ActorSystem getActorSystem() {
		final Config settings = ConfigFactory.load();
		return ActorSystem.create("msOneActorSystem", settings);
	}
}
