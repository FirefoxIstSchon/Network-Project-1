import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class Master {


    ServerSocket serverSocket;

    int SERVER_PORT;


    public Master(int SERVER_PORT){
        this.SERVER_PORT = SERVER_PORT;
    }


    public void initialize_connection(){

        try {

            serverSocket = new ServerSocket(SERVER_PORT);

        } catch (IOException e) {

            System.out.println("Master : Creation error.");

        }

    }


    public void sync_with_follower(int timeout) {

        int timeout_ms = timeout * 1_000;

        // create threads for new followers

        new Thread(() -> {

            ArrayList<Thread> threads = new ArrayList<>();

            ArrayList<Command_Listener> followers = new ArrayList<>();
            Command_Listener lastFollower;

            int follower_count = 0;
            boolean check_cond;

            while(true) {

                Command_Listener follower_listener = new Command_Listener(serverSocket);
                Thread this_thread = new Thread(follower_listener);

                this_thread.start();
                threads.add(this_thread);
                followers.add(follower_listener);

                try {
                    Thread.sleep(timeout_ms);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                lastFollower = followers.get(followers.size()-1);

                check_cond = lastFollower.socket != null
                        && lastFollower.socket.isConnected();

                if (check_cond) follower_count +=1;

            }

        }).start();

        // handle existing threads

//        for (int i = 0; i < follower_count; i++){
//            Thread this_thread = threads.get(i);
//            try {
//                this_thread.join();
//            } catch (InterruptedException e) {
//                System.out.println("Master : Thread join error."); }
//        }

    }


    public void sync_with_cloud(int timeout){

        new Thread(new Drive_Listener(timeout)).start();

    }


    public void terminate_connection() {

        try {

            if (serverSocket != null){serverSocket.close();}

        } catch (IOException e) {

            System.out.println("Master : Termination error");

        }

    }


}





class Command_Listener implements Runnable{

    ServerSocket serverSocket;

    BufferedReader reader;
    PrintWriter writer;
    Socket socket;


    public Command_Listener(ServerSocket serverSocket){ this.serverSocket = serverSocket; }

    @Override
    public void run() {

        try {

            // prepares to listen for commands

            socket = serverSocket.accept();
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream());

            String command = reader.readLine();

            // listen for commands until follower sends "ClientDone"

            while(!command.equals("ClientDone")) {

                switch (command) {

                    case "ClientSend":

                        // follower sends until master reports success

                        String filesToReceive = reader.readLine();
                        String size_filesToReceive = reader.readLine();

                        if (Resources.receive_files(
                                socket,
                                filesToReceive,
                                size_filesToReceive)
                        ) { writer.println("MasterReceived"); }
                        else { writer.println(""); }

                        writer.flush();

                        break;

                    case "ClientReceive":

                        // follower requests changes until success

                        writer.println(Resources.get_changes_names());
                        writer.println(Resources.get_changes_sizes());
                        writer.flush();

                        Resources.send_files(socket, Resources.get_changes_files());

                        try {

                            // after files are received, metafile is created wrt changes

                            Resources.create_metafile();

                        } catch (IOException e) {

                            System.out.println("Master : could not create metafile.");

                        }

                        break;

                    default:

                        System.out.println("Master : Obtained unknown Command ;" + command);

                        break;
                }

                command = reader.readLine();

            }

            // single client is done interacting

            System.out.println("Master : finished interaction with client.");

        } catch (IOException e) {

            System.out.println("Master : Transaction failed");

        }

    }

}


class Drive_Listener implements Runnable{

    int timeout_ms;

    public Drive_Listener(int timeout) { this.timeout_ms = timeout * 1_000; }

    @Override
    public void run() {

        while (true) {

            if (Resources.is_changed()) {

                System.out.println("Master : changes are present.");

                // todo : send_to_drive();

            } else {

                System.out.println("Master : changes are not present.");

            }

            // todo : receive_from_drive();

            try {
                Thread.sleep(timeout_ms);
            } catch (InterruptedException e) { System.out.println("Master : Drive thread interrupted"); }

        }

    }
}
