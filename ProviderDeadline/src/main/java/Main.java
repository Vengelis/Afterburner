import fr.vengelis.afterburner.handler.DeadlineConnect;

public class Main {

    public static void main(String[] args) {

        DeadlineConnect DC = new DeadlineConnect("10.0.0.7", 8081);
        System.out.println(DC.getJobsQueries().requeueJob("664cb162a6253400ace0624d"));

    }

}
