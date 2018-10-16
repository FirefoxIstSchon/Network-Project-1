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

        new Thread(new Runnable() {
            @Override
            public void run() {

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

//                ArrayList<Command_Listener> followers = new ArrayList<>();
//                Command_Listener lastFollower;
//
//
//                followers.add(new Command_Listener(serverSocket));
//
//                lastFollower = followers.get(followers.size()-1);
//
//                new Thread(lastFollower).start();
//
//
//                while(true) {
//
//                    try {
//                        Thread.sleep(timeout_ms);
//                    } catch (InterruptedException e) {
//                        System.out.println("Master : Main Listener failed.");
//                    }
//
//                    if (lastFollower.socket != null
//                            && lastFollower.socket.isConnected()) {
//
//                        followers.add(new Command_Listener(serverSocket));
//                        lastFollower = followers.get(followers.size()-1);
//                        new Thread(lastFollower).start();
//                    }
//                }
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

            socket = serverSocket.accept();
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream());

            String command = reader.readLine();

            while(!command.equals("ClientDone")) {

                switch (command) {

                    case "ClientSend":

                        System.out.println("Master : is receiving..");

                        String filesToReceive = reader.readLine();
                        String size_filesToReceive = reader.readLine();
                        String checksum_filesToReceive = reader.readLine();

//                        boolean success;
//
//                        do {
//
//                            success = Resources.receive_files(socket, filesToReceive, size_filesToReceive, checksum_filesToReceive);
//                            if (success) {
//
//                                writer.println("MasterReceived");
//                                writer.flush();
//
//                            }
//
//                        } while (!success);

                        if (Resources.receive_files(
                                socket,
                                filesToReceive,
                                size_filesToReceive,
                                checksum_filesToReceive
                            )
                        ) { writer.println("MasterReceived"); }
                        else { writer.println(); }

                        writer.flush();

                        break;

                    case "ClientReceive":

                        System.out.println("Master : is sending..");

                        writer.println(Resources.get_changes_names());
                        writer.println(Resources.get_changes_sizes());
                        writer.println(Resources.get_checksums());
                        writer.flush();

                        Resources.send_files(socket, Resources.get_changes_files());

                        break;

                    default:

                        System.out.println("Master : Obtained unknown Command ;" + command);

                        break;
                }

                command = reader.readLine();

            }

            System.out.println("Master : finished interaction with client.");

            try {

                Resources.create_metafile();

            } catch (IOException e) {

                System.out.println("Master : could not create metafile.");

            }

        } catch (IOException e) {

            System.out.println("Master : Transaction failed");

        }

    }

}
