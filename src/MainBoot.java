import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;


public class MainBoot {
    public static String MAIN_PROPERTIES_FILE = "main_container";

    public static void main(String[] args) {
        Runtime rt = Runtime.instance();
        Profile p = null;
        try {
            p = new ProfileImpl(MAIN_PROPERTIES_FILE);
            AgentContainer mc = rt.createMainContainer(p);
            AgentController environment = mc.createNewAgent("Environment",
                    "EnvironmentAgent", null);
            environment.start();

            AgentController simulation = mc.createNewAgent("Simulation",
                    "SimulationAgent", null);
            simulation.start();
            for(int i=1;i<=27;i++){
                AgentController analyse = mc.createNewAgent("AnalyseAgent"+String.valueOf(i),
                        "AnalyseAgent", null);
                analyse.start();
            }
        } catch (Exception ex) {

        }
    }
}
