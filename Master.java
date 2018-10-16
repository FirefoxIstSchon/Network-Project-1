import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class Master {


    ServerSocket serverSocket_files;
    ServerSocket serverSocket_commands;

    int SERVER_PORT_files;
    int SERVER_PORT_commands;


    public Master(int SERVER_PORT_files, int SERVER_PORT_commands){
        this.SERVER_PORT_files = SERVER_PORT_files;
        this.SERVER_PORT_commands = SERVER_PORT_commands;
    }


    public void initialize_connection(){

        try {

            serverSocket_files = new ServerSocket(SERVER_PORT_files);
            serverSocket_commands = new ServerSocket(SERVER_PORT_commands);

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

                    Command_Listener follower_listener = new Command_Listener(serverSocket_commands, serverSocket_files);
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

                    check_cond =
                            lastFollower.socket_commands != null &&
                                    lastFollower.serverSocket_files != null &&
                                    lastFollower.socket_files.isConnected() &&
                                    lastFollower.socket_commands.isConnected();

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

            if (serverSocket_files != null){
                serverSocket_files.close();}
            if (serverSocket_commands != null){
                serverSocket_commands.close();}

        } catch (IOException e) {

            System.out.println("Master : Termination error");

        }

    }


}





class Command_Listener implements Runnable{

    ServerSocket serverSocket_files;
    ServerSocket serverSocket_commands;

    BufferedReader reader;
    PrintWriter writer;
    Socket socket_files;
    Socket socket_commands;


    public Command_Listener(ServerSocket serverSocket_files, ServerSocket serverSocket_commands){
        this.serverSocket_files = serverSocket_files;
        this.serverSocket_commands = serverSocket_commands;
    }

    @Override
    public void run() {

        try {

            socket_files = serverSocket_files.accept();
            socket_commands = serverSocket_commands.accept();

            reader = new BufferedReader(new InputStreamReader(socket_commands.getInputStream()));
            writer = new PrintWriter(socket_files.getOutputStream());

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
                                socket_files,
                                filesToReceive,
                                size_filesToReceive,
                                checksum_filesToReceive
                            )
                        ) { writer.println("MasterReceived") ; writer.flush() ; }

                        break;

                    case "ClientReceive":

                        System.out.println("Master : is sending..");

                        writer.println(Resources.get_changes_names());
                        writer.println(Resources.get_changes_sizes());
                        writer.println(Resources.get_checksums());
                        writer.flush();

                        Resources.send_files(socket_files, Resources.get_changes_files());

                        break;

                    default:

                        System.out.println("Master : Obtained unknown Command ;" + command);

                        break;
                }

                command = reader.readLine();

            }

            System.out.println("Master : finished interaction with client.");

        } catch (IOException e) {

            System.out.println("Master : Transaction failed");

        }

    }

}
