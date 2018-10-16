
import java.io.*;
import java.net.Socket;


public class Follower {


    Socket socket_files;
    Socket socket_commands;

    BufferedReader reader;
    PrintWriter writer;

    String SERVER_ADDRESS;
    int SERVER_PORT_files;
    int SERVER_PORT_commands;


    public Follower(String SERVER_ADDRESS, int SERVER_PORT_files, int SERVER_PORT_commands){
        this.SERVER_ADDRESS = SERVER_ADDRESS;
        this.SERVER_PORT_files = SERVER_PORT_files;
        this.SERVER_PORT_commands = SERVER_PORT_commands;
    }


    public void initialize_connection() {

        boolean is_init = false;

        do {

            try {

                socket_files = new Socket(SERVER_ADDRESS, SERVER_PORT_files);
                socket_commands = new Socket(SERVER_ADDRESS, SERVER_PORT_commands);

                reader = new BufferedReader(new InputStreamReader(socket_commands.getInputStream()));
                writer = new PrintWriter(socket_commands.getOutputStream());

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

            if (socket_commands != null) {socket_commands.close();}
            if (socket_files != null) {socket_files.close();}
            if (reader != null) {reader.close();}
            if (writer != null) {writer.close();}

        } catch (IOException e) {

            System.out.println("Follower : Termination error.");

        }

    }


}

