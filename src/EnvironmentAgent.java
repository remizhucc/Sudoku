import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jade.core.AID;
import jade.core.Agent;
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class EnvironmentAgent extends Agent {

    Case[][] grid;

    protected void setup() {
        grid = new Case[9][9];
        System.out.println(getLocalName() + "--> Installed");

        ParallelBehaviour par = new ParallelBehaviour(ParallelBehaviour.WHEN_ANY);
        par.addSubBehaviour(new ReceiveResultBehaviour());
        par.addSubBehaviour(new ExecuteAnalyseBehaviour());

        SequentialBehaviour seq = new SequentialBehaviour();
        seq.addSubBehaviour(new ReceiveOriginGridBehaviour());
        seq.addSubBehaviour(par);
        seq.addSubBehaviour(new PrintResultBehaviour());

        addBehaviour(seq);
        addBehaviour(new CancelBehaviour());
    }

    private void readFile(String path) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String linearGrid = "";
            String line = br.readLine();
            while (line != null) {
                linearGrid += (line + " ");
                line = br.readLine();
            }
            br.close();
            String[] newGrid = linearGrid.split(" ");
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    grid[i][j] = new Case(i, j, Integer.valueOf(newGrid[i * 9 + j]));
                }
            }
        } catch (Exception exception) {
        }
    }

    private void printGrid() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                System.out.print(grid[i][j].getCaseValue() + " ");
            }
            System.out.println();
        }
    }

    private boolean isOver() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (grid[i][j].caseValue == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private class ReceiveOriginGridBehaviour extends Behaviour {
        boolean get;

        ReceiveOriginGridBehaviour() {
            get = false;
        }

        public void action() {
            ACLMessage message = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

            if (message != null) {
                readFile(message.getContent());
                ACLMessage newGridMessage = new ACLMessage(ACLMessage.REQUEST);
                newGridMessage.addReceiver(new AID("Simulation", AID.ISLOCALNAME));
                myAgent.send(newGridMessage);
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

    private class ExecuteAnalyseBehaviour extends Behaviour {
        int zone = 0;

        public void action() {
            ACLMessage message = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.AGREE));

            if (message != null) {
                ACLMessage analyseRequest = message.createReply();
                analyseRequest.setPerformative(ACLMessage.REQUEST);
                analyseRequest.setContent(Case.arrayListToJSON(getNextZone()));
                send(analyseRequest);

            } else {
                block();
            }
        }

        @Override
        public boolean done() {
            return isOver();
        }

        private ArrayList<Case> getNextZone() {
            ArrayList<Case> cases = new ArrayList();
            if (zone == 27) {
                zone -= 27;
            }
            if (0 <= zone && zone < 9) {
                cases = getRow(zone);
            }
            if (9 <= zone && zone < 18) {
                cases = getColomn(zone - 9);
            }
            if (18 <= zone && zone < 27) {
                cases = getGrid(zone - 18);
            }
            zone++;
            return cases;
        }

        private ArrayList<Case> getRow(int i) {
            ArrayList<Case> cases = new ArrayList();
            for (int j = 0; j < 9; j++) {
                cases.add(grid[i][j]);
            }
            return cases;
        }

        private ArrayList<Case> getColomn(int i) {
            ArrayList<Case> cases = new ArrayList();
            for (int j = 0; j < 9; j++) {
                cases.add(grid[j][i]);
            }
            return cases;
        }

        private ArrayList<Case> getGrid(int i) {
            ArrayList<Case> cases = new ArrayList();
            int row = Math.floorDiv(i, 3);
            int colomn = i % 3;
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++)
                    cases.add(grid[row * 3 + j][colomn * 3 + k]);
            }
            return cases;
        }
    }

    private class ReceiveResultBehaviour extends Behaviour {

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage message = receive(mt);
            if (message != null) {
                ArrayList<Case> cases = Case.JSONToArrayList(message.getContent());
                cases.forEach(aCase -> intersection(aCase));
            }

        }


        private void intersection(Case c) {
            ArrayList<Integer> newList = new ArrayList<Integer>();
            if (grid[c.indiceLigne][c.indiceColonne].caseValue == 0 && c.caseValue != 0) {
                grid[c.indiceLigne][c.indiceColonne].caseValue = c.caseValue;
                System.out.println(c.indiceLigne + " " + c.indiceColonne + " = " + c.caseValue);
                grid[c.indiceLigne][c.indiceColonne].emptyValuePossi();
            }
            for (Integer t : grid[c.indiceLigne][c.indiceColonne].valuePossi) {
                if (c.valuePossi.contains(t)) {
                    newList.add(t);
                }
            }

            grid[c.indiceLigne][c.indiceColonne].valuePossi = newList;
        }

        @Override
        public boolean done() {
            return isOver();
        }


    }

    private class PrintResultBehaviour extends OneShotBehaviour {
        @Override
        public void action() {
            System.out.println("****** Solved ******");
            printGrid();
        }
    }

    private class CancelBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CANCEL);
            ACLMessage message = receive(mt);
            if (message != null) {
                printGrid();
            }
        }
    }
}


