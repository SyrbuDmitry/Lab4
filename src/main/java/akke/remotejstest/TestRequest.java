package akke.remotejstest;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;

import java.util.List;

public class TestRequest extends AbstractActor {
    private MultiMap<String,String> store = new MultiValueMap<>();
    @Override
    public Receive createReceive(){
        return ReceiveBuilder.create()
                .match(StoreTestResultMessage.class, m -> {
                store.put(m.getID(), m.getResult());
                System.out.println("receive message! "+m.toString());
                })
                .match(GetResaultMessage.class, req -> {
                    sender().tell(
                            new ResultsMessage(req.getID(), (List<String>) store.get(req.getID())), self());
                        }
                ).build();
    }
}
