import java.io.IOException;

public class Follower_run {


    static String SERVER_ADDRESS = "172.20.40.159";
    static int SERVER_PORT = 4444;

    static Follower follower;


    public static void main(String[] args){

        // create a follower instance

        follower = new Follower(SERVER_ADDRESS, SERVER_PORT);

        follower.initialize_connection();

        if (follower.socket == null) {

            System.out.println("Follower : connectivity is not established.");

        } else {

            System.out.println("Follower : connectivity is established.");

            // update with master

            new Thread(new Master_Connecter(follower, 10)).start();

            // follower.terminate_connection();

        }

    }


}