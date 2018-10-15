import java.io.IOException;

public class Follower_run {


    static String SERVER_ADDRESS = "localhost"; //"192.168.1.20";
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

        System.out.println("Follower : is sending..");

        String command = "ClientSend";
        follower.send_command(command);
        follower.send_command(Resources.get_changes_names());
        follower.send_command(Resources.get_changes_sizes());

        Resources.send_files(follower.socket, Resources.get_changes_files());

    }


    public static void receive() {

        System.out.println("Follower : is receiving..");

        String command = "ClientReceive";
        follower.send_command(command);
        String filesToReceive = follower.get_response();
        String size_filesToReceive = follower.get_response();

        Resources.receive_files(follower.socket, filesToReceive, size_filesToReceive);

    }


}
