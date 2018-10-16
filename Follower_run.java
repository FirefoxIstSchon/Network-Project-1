import java.io.IOException;

public class Follower_run {


    static String SERVER_ADDRESS = "172.20.40.159";
    static int SERVER_PORT = 4444;

    static Follower follower;


    public static void main(String[] args){

        follower = new Follower(SERVER_ADDRESS, SERVER_PORT);
        follower.initialize_connection();

        if (follower.socket == null) {

            System.out.println("Follower : connectivity is not established.");

        } else {

            System.out.println("Follower : connectivity is established.");

            // update with master

            if (Resources.is_changed()) {

                System.out.println("Follower : changes are present.");

                send();

            } else {

                System.out.println("Follower : changes are not present.");

            }

            receive();

            // end connection

            try {

                Resources.create_metafile();

            } catch (IOException e) {
                System.out.println("Follower : could not create metafile.");
            }

            follower.send_command("ClientDone");

            follower.terminate_connection();

        }

    }


    public static void send() {

        do {

            System.out.println("Follower : is sending..");

            String command = "ClientSend";
            follower.send_command(command);
            follower.send_command(Resources.get_changes_names());
            follower.send_command(Resources.get_changes_sizes());
            follower.send_command(Resources.get_checksums());

            Resources.send_files(follower.socket, Resources.get_changes_files());

        } while (!follower.get_response().equals("MasterReceived"));
        System.out.flush();
    }


    public static void receive() {

        boolean success;

        do {

            System.out.println("Follower : is receiving..");

            String command = "ClientReceive";

            follower.send_command(command);
            String filesToReceive = follower.get_response();
            String size_filesToReceive = follower.get_response();
            String fileChecksums = follower.get_response();

            success = Resources.receive_files(
                        follower.socket,
                        filesToReceive,
                        size_filesToReceive,
                        fileChecksums);

        } while (!success);

    }


}
