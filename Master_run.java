import java.io.IOException;


public class Master_run {


    static int SERVER_PORT_files = 4444;
    static int SERVER_PORT_commands = 4443;


    public static void main(String[] args){

        Master master = new Master(SERVER_PORT_files, SERVER_PORT_commands);
        master.initialize_connection();

        if (master.serverSocket_files == null || master.serverSocket_commands == null) {

            System.out.println("Master : connectivity is not established.");

        } else {

            System.out.println("Master : connectivity is established.");

            // update with follower

            master.sync_with_follower(10);

            // update with drive

            if (Resources.is_changed()) {

                System.out.println("Master : changes are present.");

                // todo : send_to_drive();

            } else {

                System.out.println("Master : changes are not present.");

            }

            // todo : receive_from_drive();

            // end connection

            // master.terminate_connection();

//            try {
//
//                Resources.create_metafile();
//
//
//            } catch (IOException e) {
//
//                System.out.println("Master : could not create metafile.");
//
//            }

        }

    }


}
