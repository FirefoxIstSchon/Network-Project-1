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




class Master_Connecter implements Runnable{

    Follower follower;
    int timeout_ms;

    public Master_Connecter(Follower follower, int timeout) {
        this.follower = follower;
        this.timeout_ms = timeout * 1_000;
    }

    @Override
    public void run() {


        while (true) {


            if (Resources.is_changed()) {

                // send files only if changes are present

                System.out.println("Follower : changes are present.");

                do {

                    System.out.println("Follower : is sending..");

                    //client sends ClientSend command, and the details of transaction

                    String command = "ClientSend";
                    follower.send_command(command);
                    follower.send_command(Resources.get_changes_names());
                    follower.send_command(Resources.get_changes_sizes());
                    follower.send_command(Resources.get_checksums());

                    //send changes until success

                    Resources.send_files(follower.socket, Resources.get_changes_files());
                    System.out.println("Follower send files.");

                } while (!follower.get_response().equals("MasterReceived"));

                System.out.flush();

            } else {

                System.out.println("Follower : changes are not present.");

            }

            // receive data for changes in master

            boolean success;

            do {

                System.out.println("Follower : is receiving..");

                //client sends ClientReceive command, and gets the details of transaction

                String command = "ClientReceive";
                follower.send_command(command);
                String filesToReceive = follower.get_response();
                String size_filesToReceive = follower.get_response();
                String fileChecksums = follower.get_response();

                //ask for changes until success

                success = Resources.receive_files(
                        follower.socket,
                        filesToReceive,
                        size_filesToReceive,
                        fileChecksums);

            } while (!success);

            // end connection

            try {

                // after exchange, create a metafile

                Resources.create_metafile();

            } catch (IOException e) {
                System.out.println("Follower : could not create metafile");
            }

            //follower.send_command("ClientDone");

            try {
                Thread.sleep(timeout_ms);
            } catch (InterruptedException e) {
                System.out.println("Master_Connector thread interrupted");
            }

        }

    }

}