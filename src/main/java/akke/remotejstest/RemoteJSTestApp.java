package akke.remotejstest;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import scala.concurrent.Future;

import java.io.IOException;
import java.util.concurrent.CompletionStage;


public class RemoteJSTestApp extends AllDirectives {
    public static void main(String[] args) throws IOException {

        ActorSystem system = ActorSystem.create("lab4");
        ActorRef RouteActor = system.actorOf(Props.create(RouterActor.class));
        final Http http = Http.get(system);
        RemoteJSTestApp instance = new RemoteJSTestApp();
        final ActorMaterializer materializer = ActorMaterializer.create(system);
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = instance.createRoute(RouteActor).flow(system, materializer);

        final CompletionStage<ServerBinding> binding = http.bindAndHandle(
                routeFlow,
                ConnectHttp.toHost("localhost", 8085),
                materializer
        );
        System.out.println("Server online at http://localhost:8085/\nPress RETURN to stop...");
        System.in.read();
        binding
                .thenCompose(ServerBinding::unbind)
                .thenAccept(unbound -> system.terminate());
    }


    private Route createRoute(ActorRef RouteActor) {
        return
                route(
                        pathSingleSlash(() ->
                                get(() ->
                                        parameter("packageID", id -> {
                                                    Future<Object> result = Patterns.ask(RouteActor, new GetResultMessage(Integer.parseInt(id)), 5000);
                                                    return completeOKWithFuture(result, Jackson.marshaller());
                                                }
                                        )
                                )
                        ),

                        pathSingleSlash(() ->
                                post(() -> entity(Jackson.unmarshaller(PostRequestMessage.class), msg -> {
                                    RouteActor.tell(msg,ActorRef.noSender());
                                    return complete("Tests started!\n");
                                }))
                        )
                );
    }
}
