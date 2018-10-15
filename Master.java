
import java.io.*;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class Master {


    ServerSocket serverSocket;
    BufferedReader reader;
    PrintWriter writer;
    Socket socket;

    int SERVER_PORT;

    Boolean is_listening = false;


    public Master(int SERVER_PORT){

        this.SERVER_PORT = SERVER_PORT;
    }


    public void initialize_connection(){
        try {

            serverSocket = new ServerSocket(SERVER_PORT);

        } catch (IOException e) {

            System.out.println("Creation error.");

        }

    }


    public void listen_for_commands(){

        try {

            if (socket == null) socket = serverSocket.accept();
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream());

            String command = reader.readLine();

            is_listening = true;

            while(!command.equals("ClientDone")) {

                switch (command) {

                    case "ClientSend":

                        System.out.println("Master : is receiving..");

                        String filesToReceive = reader.readLine();
                        String size_filesToReceive = reader.readLine();

                        Resources.receive_files(socket, filesToReceive, size_filesToReceive);

                        break;

                    case "ClientReceive":

                        System.out.println("Master : is sending..");

                        writer.println(Resources.get_changes_names());
                        writer.println(Resources.get_changes_sizes());
                        writer.flush();

                        Resources.send_files(socket, Resources.get_changes_files());

                        break;

                    default:

                        System.out.println("Master : Obtained unknown Command.");

                        break;
                }

                command = reader.readLine();

            }

            is_listening = false;

        } catch (IOException e) {

            System.out.println("Master : Transaction failed");

        }

    }


    public void start_listening(int timeout) {

        Runnable runner = new Runnable() {
            @Override
            public void run() {

                listen_for_commands();

            }
        };

        int ctr = timeout / 10;

        for (int i = 1; i < ctr; i++) {

            new Thread(runner).start();

            if (is_listening) new Thread(runner).start();

            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                System.out.println("Thread sleep interrupted");
            }
        }

    }


    public void terminate_connection() {

        try {

            if (reader != null){reader.close();}
            if (writer != null){writer.close();}
            if (socket != null){socket.close();}
            if (serverSocket != null){serverSocket.close();}

        } catch (IOException e) {

            System.out.println("Master : Termination error");

        }

    }


}
