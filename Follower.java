
import java.io.*;
import java.net.Socket;


public class Follower {


    Socket socket;
    BufferedReader reader;
    PrintWriter writer;

    String SERVER_ADDRESS;
    int SERVER_PORT;


    public Follower(String SERVER_ADDRESS, int SERVER_PORT){
        this.SERVER_ADDRESS = SERVER_ADDRESS;
        this.SERVER_PORT = SERVER_PORT;
    }


    public void initialize_connection() {

        boolean is_init = false;

        do {

            try {

                socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream());

                is_init = true;

            } catch (IOException e) {

                System.out.println("Follower : Creation error.");

            }

            if (!is_init) {

                try {

                    Thread.sleep(5*1_000);

                } catch (InterruptedException e) {

                    System.out.println("Follower : Suspend error.");

                }

            }

        } while(!is_init);

    }


    public void send_command(String cmd) {

        writer.println(cmd);
        writer.flush();

    }


    public String get_response() {

        String response = "";

        try {

            response = reader.readLine();

        } catch (IOException e) {

            System.out.println("Follower : Response error.");

        }

        return response;

    }


    public void terminate_connection() {

        try {

            if (socket!= null) {socket.close();}
            if (reader != null) {reader.close();}
            if (writer != null) {writer.close();}

        } catch (IOException e) {

            System.out.println("Follower : Termination error.");

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

                String str="";
                do {

                    //client sends ClientSend command, and the details of transaction

                    String command = "ClientSend";
                    follower.send_command(command);
                    follower.send_command(Resources.get_changes_names());
                    follower.send_command(Resources.get_changes_sizes());

                    //send changes until success

                    Resources.send_files(follower.socket, Resources.get_changes_files());

                    str =follower.get_response();


                } while (!str.equals("MasterReceived"));

                System.out.println("reach");
                System.out.flush();


            } else {

                System.out.println("Follower : changes are not present.");

            }

            // receive data for changes in master

            boolean success;

            do {

                //client sends ClientReceive command, and gets the details of transaction

                String command = "ClientReceive";
                follower.send_command(command);
                String filesToReceive = follower.get_response();
                String size_filesToReceive = follower.get_response();

                //ask for changes until success

                success = Resources.receive_files(
                        follower.socket,
                        filesToReceive,
                        size_filesToReceive);

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