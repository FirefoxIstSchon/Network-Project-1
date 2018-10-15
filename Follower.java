
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

        try {

            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream());

        } catch (IOException e) {

            System.out.println("Follower : Creation error.");

        }

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

