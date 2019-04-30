import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.wrapper.AgentController;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class AnalyseAgent extends Agent {

    protected void setup() {
        System.out.println(getLocalName() + "--> Installed");
        addBehaviour(new IntervenirBehaviour());
        addBehaviour(new AnalyseBehaviour());
    }

    public void algo1(ArrayList<Case> cellules) {
        // Lorsqu'une cellule n'a plus qu'une valeur possible,
        // celle-ci en devient son contenu et la liste des possibles est vidée

        // ArrayList<Integer> valueDelete = new ArrayList<>();
        for (Case c : cellules) {
            // deja init valuePossi partout, pas possible d etre vide
            if (c.getValuePossi().size() == 1) {
                c.setCaseValue(c.getValuePossi().get(0));
                c.emptyValuePossi();
                // valueDelete.add(c.getCaseValue());
            }
            // else {
            // for(Integer v:valueDelete) {
            // c.removeValuePossi(v);
            // }
            // }
        }
    }

    public void algo2(ArrayList<Case> cellules) {
        // Si une cellule a un contenu determine
        // alors il doit etre retire des possibles de
        // toutes les autres cellules non determinees.

        ArrayList<Integer> valueDelete = new ArrayList<>();
        for (Case c : cellules) {
            if (c.getCaseValue() != 0) {
                valueDelete.add(c.getCaseValue());
            }
        }
        for (Case c : cellules) {
            for (Integer v : valueDelete) {
                if (c.getCaseValue() == 0) {
                    c.removeValuePossi(v);
                }
            }
        }
    }

    public void algo3(ArrayList<Case> cellules) {
        // Une valeur ne se trouvant que dans une seule liste de possibles
        // est la valeur de cette cellule.
        int flag = 0;
        int fromIndex;
        int toIndex = cellules.size();
        for (int i = 1; i < 10; i++) {
            fromIndex = 0;
            for (Case c : cellules) {
                fromIndex++;
                if (c.getValuePossi().contains(i)) {
                    // i is one of the possible value of c
                    flag = 1; // find the first present of number i
                    for (Case b : cellules.subList(fromIndex, toIndex)) {
                        if (b.getValuePossi().contains(i)) {
                            flag = 0; // find the second present of number i
                            break;
                        }
                        if (flag == 0) break;
                    }
                    if (flag == 1) {
                        c.emptyValuePossi();
                        c.setCaseValue(i);
                    } else {
                        break;
                    }
                }
            }
        }
    }

    public void algo4(ArrayList<Case> cellules) {
        // Si seulement deux cellules contiennent les deux memes valeurs possibles
        int value1 = 0;
        int value2 = 0;
        int flag = 0;
        int fromIndex = 0;
        int toIndex = cellules.size();
        for (Case c : cellules) {
            fromIndex++;
            if (c.getValuePossi().size() == 2) {
                value1 = c.getValuePossi().get(0);
                value2 = c.getValuePossi().get(1);
                for (Case b : cellules.subList(fromIndex, toIndex)) {
                    if (b.getValuePossi().size() == 2) {
                        if (value1 == b.getValuePossi().get(0) && value2 == b.getValuePossi().get(1)) {
                            flag = 1;
                            break;
                        } else if (value2 == b.getValuePossi().get(0) && value1 == b.getValuePossi().get(1)) {
                            flag = 1;
                            break;
                        }
                    }
                }
                if (flag == 1) break;
            }
        }

        // alors les possibles des autres cellules ne peuvent contenir ces valeurs.
        if (flag == 1) {
            for (Case c : cellules) {
                if (c.getValuePossi().contains(value1) && (!c.getValuePossi().contains(value2))) {
                    c.removeValuePossi(value1);
                } else if (c.getValuePossi().contains(value2) && (!c.getValuePossi().contains(value1))) {
                    c.removeValuePossi(value2);
                } else if (c.getValuePossi().size() > 2 && c.getValuePossi().contains(value1) && c.getValuePossi().contains(value2)) {
                    c.removeValuePossi(value1);
                    c.removeValuePossi(value2);
                }
            }
        }
    }

    class IntervenirBehaviour extends OneShotBehaviour {
        @Override
        public void action() {
            ACLMessage message = new ACLMessage(ACLMessage.PROPOSE);
            message.addReceiver(new AID("Simulation", AID.ISLOCALNAME));
            send(message);
        }
    }

    class AnalyseBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage message = receive(mt);
            if (message != null) {
                ArrayList<Case> cases = Case.JSONToArrayList(message.getContent());
                algo1(cases);
                algo2(cases);
                algo3(cases);
                algo4(cases);
                ACLMessage reply = message.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent(Case.arrayListToJSON(cases));
                send(reply);
            } else {
                block();
            }
        }
    }

}

