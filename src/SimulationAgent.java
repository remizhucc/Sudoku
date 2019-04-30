import com.fasterxml.jackson.databind.ObjectMapper;
import jade.core.AID;
import jade.core.Agent;
import jade.core.AgentContainer;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;

import javax.naming.ldap.Control;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class SimulationAgent extends Agent {
    ArrayList<AID> analyseAgents;
    AID env;

    protected void setup() {
        System.out.println(getLocalName() + "--> Installed");
        env = null;
        analyseAgents = new ArrayList<>();

        SequentialBehaviour seq = new SequentialBehaviour();
        seq.addSubBehaviour(new GetAnalyseAgentsBehaviour());
        seq.addSubBehaviour(new GetRequestBehavious());
        seq.addSubBehaviour(new SendAnalyseAgentAIDBehaviour(this, 500));

        addBehaviour(seq);
    }


    private class GetRequestBehavious extends Behaviour {
        boolean get;

        GetRequestBehavious() {
            get = false;
        }

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage message = receive(mt);
            if (message != null) {
                env = message.getSender();
                get = true;
            } else {
                block();
            }
        }

        @Override
        public boolean done() {
            return get;
        }

    }

    private class GetAnalyseAgentsBehaviour extends Behaviour {
        int number = 0;

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
            ACLMessage message = receive(mt);
            if (message != null) {
                analyseAgents.add(message.getSender());
                number++;
            }
        }

        @Override
        public boolean done() {
            return number == 27;
        }


    }

    private class SendAnalyseAgentAIDBehaviour extends TickerBehaviour {
        SendAnalyseAgentAIDBehaviour(Agent myAgent, long interval) {
            super(myAgent, interval);
        }

        @Override
        protected void onTick() {
            ACLMessage message;
            for (int i = 0; i < 27; i++) {
                message = new ACLMessage(ACLMessage.AGREE);
                message.addReceiver(env);
                message.addReplyTo(analyseAgents.get(i));
                send(message);
            }
        }


    }

}

