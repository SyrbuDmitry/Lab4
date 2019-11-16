package akke.remotejstest;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.Patterns;
import akka.routing.RoundRobinPool;
import javafx.geometry.Pos;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import static java.util.concurrent.TimeUnit.SECONDS;

public class RouterActor extends AbstractActor {
    private ActorRef StoreActor = getContext().actorOf(Props.create(StoreActor.class),"storeActor");
    private ActorRef TestRouter = getContext().actorOf(
            new RoundRobinPool(5)
                    .props(Props.create(TestActor.class))
    );
    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(TestResultMessage.class, t->StoreActor.tell(t,self()))
                .match(TestScript.class, s->TestRouter.tell(s,self()))
                .match(PostRequestBody.class, msg->{
                    for (Test t : msg.tests) {
                        TestRouter.tell(new TestScript(msg.packageId, msg.functionName, msg.JsScript, t.params), self());
                    }
                })
                .match(GetResultMessage.class, r->{
                    StoreActor.tell(r,sender());
                })
                .build();
    }
}
