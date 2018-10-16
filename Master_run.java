

public class Master_run {


    static int SERVER_PORT = 4444;


    public static void main(String[] args){

        // create a master (server) instance

        Master master = new Master(SERVER_PORT);
        master.initialize_connection();

        if (master.serverSocket == null) {

            System.out.println("Master : connectivity is not established.");

        } else {

            System.out.println("Master : connectivity is established.");

            // update with follower

            master.sync_with_follower(5);

            // update with drive

            master.sync_with_cloud(10);

            // end connection

            // master.terminate_connection();

//            try {
//
//                Resources.create_metafile();
//
//            } catch (IOException e) {
//
//                System.out.println("Master : could not create metafile.");
//
//            }

        }

    }


}
