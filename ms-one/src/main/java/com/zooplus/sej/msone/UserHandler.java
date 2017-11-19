package com.zooplus.sej.msone;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import com.zooplus.sej.msone.UserActorSupervisor.CreateUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import scala.compat.java8.FutureConverters;

import javax.annotation.PostConstruct;

import static scala.compat.java8.FutureConverters.toJava;

/**
 * @author lazar.agatonovic
 */
@Component
public class UserHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActorSystem actorSystem;

    private ActorRef userActorSupervisor;

    @PostConstruct
    public void init() {
        userActorSupervisor = actorSystem.actorOf(UserActorSupervisor.props(userRepository));
    }

    public Mono<ServerResponse> createUser(ServerRequest request) {
        Mono<UserModel> userModelMono = request.bodyToMono(UserModel.class);
        return Mono.fromCompletionStage(userModelMono.toFuture()
                .thenCompose(um -> toJava(Patterns.ask(userActorSupervisor, new CreateUser(convertUser(um)), 5000)))
                .thenApply(o -> (UserActor.UserResponse) o)
        ).flatMap(urm -> ServerResponse.status(urm.status())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(urm.user())));
    }

    private User convertUser(UserModel userModel) {
        return new User(userModel.getId(), userModel.getName(), userModel.getSurname());
    }
}
