
import jdk.nashorn.internal.runtime.arrays.ArrayIndex;
import sun.plugin.javascript.navig.Array;

import javax.imageio.IIOException;
import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.io.*;


public class Resources {

    public static Path path = Paths.get("C:\\Users\\Berke\\Documents\\School\\Courses\\Comp\\416\\Projects\\P1\\testfolderforsync");
    public static File dir =new File ("C:\\Users\\Berke\\Documents\\School\\Courses\\Comp\\416\\Projects\\P1\\testfolderforsync");
    public static Hashtable hash_table_for_files=new Hashtable();

    public Resources(){}

    public static Boolean is_changed(){

        ArrayList temp= get_changes_files();
        ArrayList<File> changed_files =get_changes_files();
        ArrayList<String> deleted_files =get_deleted_filenames();

        if (changed_files.isEmpty() && deleted_files.isEmpty()){
            return false;
        }else{
            return true;
        }

    }

    public static void get_data_from_metafile () throws  IOException
    {
        File metafile = new File("C:\\Users\\Berke\\Documents\\School\\Courses\\Comp\\416\\Projects\\P1\\testfolderforsync\\metafile.txt");
        Scanner sc = new Scanner(metafile);
        while (sc.hasNextLine()){
            String[] line =sc.nextLine().split(" ");
            hash_table_for_files.put(Integer.parseInt(line[1]), line[0]);
        }
    }

    public static void create_metafile ()throws IOException{
        //creating a metafile to store changes
        File metafile = new File("C:\\Users\\Berke\\Documents\\School\\Courses\\Comp\\416\\Projects\\P1\\testfolderforsync\\metafile");

        //checking if the file already exists
        if(metafile.createNewFile()){
            System.out.println("File Created");
        }else {
            System.out.println("Metafile already exist");
            //truncating the file if it already exist
            try (FileChannel outChan = new FileOutputStream(metafile, true).getChannel()) {
                outChan.truncate(0);
            }
        }

        //file operations to print on file
        File [] files =dir.listFiles();
        FileWriter fileWriter = new FileWriter(metafile);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        int hashcode=-1;
        String str="";
        for (File file_entry : files)
        {
            {

                //adding hashcode to the meta file to compare files later on
                hashcode=file_entry.hashCode();
                str=str+file_entry.getName()+ " ";
                if(!str.contains("metafile.txt ")){
                    System.out.println(file_entry.getName());
                    str=str+hashcode;
                    printWriter.println(str);
                }
                str="";
            }
        }
        printWriter.close();


    }

    public static ArrayList<File> get_changes_files() {


        // todo : what kinda thing is returned?
        try {
            get_data_from_metafile ();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<Object> result =new ArrayList<Object>();
        ArrayList<File> changed_files =new ArrayList<File>();
        ArrayList<String> deleted_files=new ArrayList<String>(  );
        //get current files
        File [] current_files =dir.listFiles();
        int hashcode =0;

        //this part is to find added and modified files
        for (File file_entry : current_files)
        {
            //We dont need to compare metafile
            if(!file_entry.getName().equals("metafile.txt"))
            {
                hashcode=file_entry.hashCode();
                //checking whether the file is changed or  not.
                String filename_hash=(String) hash_table_for_files.get(hashcode);
                String filename_current=(String) file_entry.getName();
                if(hash_table_for_files.containsKey(hashcode))
                {

                    System.out.println("The file called "+file_entry.getName()+" doesn't changed.");

                }
                else {
                    System.out.println("The file called "+file_entry.getName()+" changed.");
                    changed_files.add(file_entry);

                }

                hash_table_for_files.remove(hashcode);


            }
        }

        //this part is for finding deleted files
        //if it is not empty it means that there are some deleted files. Because we have deleted all modified and unmodified elements before.
        if(!hash_table_for_files .isEmpty())
        {
            System.out.println("Finding deleted files.");

            List<String> list_of_remaining_values = new ArrayList<>(hash_table_for_files.values());
            //This iteration is for finding the deleted file.
            boolean check=true;
            for(String name_of_the_file: list_of_remaining_values)
            {
                for(File changed_file: changed_files)
                {
                    if(changed_file.getName().contains(name_of_the_file)){
                        check=false;
                    }
                }
                if(check){
                    System.out.println(name_of_the_file+" is deleted.");
                    deleted_files.add(name_of_the_file);
                }

                check=true;
            }

        }
        result.add(changed_files);
        result.add(deleted_files);

        return changed_files;
    }
    public static ArrayList<String> get_deleted_filenames() {
        try {
            get_data_from_metafile ();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<Object> result =new ArrayList<Object>();
        ArrayList<File> changed_files =new ArrayList<File>();
        ArrayList<String> deleted_files=new ArrayList<String>(  );
        //get current files
        File [] current_files =dir.listFiles();
        int hashcode =0;

        //this part is to find added and modified files
        for (File file_entry : current_files)
        {
            //We dont need to compare metafile
            if(!file_entry.getName().equals("metafile.txt"))
            {
                hashcode=file_entry.hashCode();
                //checking whether the file is changed or  not.
                String filename_hash=(String) hash_table_for_files.get(hashcode);
                String filename_current=(String) file_entry.getName();
                if(hash_table_for_files.containsKey(hashcode))
                {

                    System.out.println("The file called "+file_entry.getName()+" doesn't changed.");

                }
                else {
                    System.out.println("The file called "+file_entry.getName()+" changed.");
                    changed_files.add(file_entry);

                }

                hash_table_for_files.remove(hashcode);


            }
        }

        //this part is for finding deleted files
        //if it is not empty it means that there are some deleted files. Because we have deleted all modified and unmodified elements before.
        if(!hash_table_for_files .isEmpty())
        {
            System.out.println("Finding deleted files.");

            List<String> list_of_remaining_values = new ArrayList<>(hash_table_for_files.values());
            //This iteration is for finding the deleted file.
            boolean check=true;
            for(String name_of_the_file: list_of_remaining_values)
            {
                for(File changed_file: changed_files)
                {
                    if(changed_file.getName().contains(name_of_the_file)){
                        check=false;
                    }
                }
                if(check){
                    System.out.println(name_of_the_file+" is deleted.");
                    deleted_files.add(name_of_the_file);
                }

                check=true;
            }

        }
        result.add(changed_files);
        result.add(deleted_files);

        return deleted_files;





    }
    public static ArrayList <File> get_changes_files1(){
        // todo : what kinda thing is returned?

        ArrayList<File> files = new ArrayList<>();

        //check if the path resolved to a file
        try {

            Boolean isFolder = (Boolean) Files.getAttribute(path, "basic:isDirectory");
            if (!isFolder) {
                throw new IllegalArgumentException("Path: " + path + " is not a folder");
            }
        } catch (IOException ioe) {
            // Folder does not exists
            ioe.printStackTrace();
        }




        return files;
    }

    public static String get_changes_names() {


        ArrayList<File> changed_files =get_changes_files();
        String str ="";
        for(File changed_file: changed_files)
        {
            str=str+changed_file.getName()+", ";

        }


        str =str.substring(0,str.length()-2);
        return str;

    }

    public static String get_changes_sizes() {
        ArrayList<File> changed_files = get_changes_files();
        String str ="";
        for(File changed_file: changed_files)
        {
            str=str+changed_file.length()+", ";

        }
        str =str.substring(0,str.length()-2);

        return str;

    }




    public static void send_files(Socket socket, ArrayList<File> files) {

        BufferedInputStream bufferedInputStream;
        FileInputStream fileInputStream;
        OutputStream outputStream;

        byte[] byteArray;
        int byteArrayLen;

        for (File file : files) {
            try {

                byteArray = new byte[(int) file.length()];
                byteArrayLen = byteArray.length;
                fileInputStream = new FileInputStream(file);

                bufferedInputStream = new BufferedInputStream(fileInputStream);
                bufferedInputStream.read(byteArray, 0, byteArrayLen);

                outputStream = socket.getOutputStream();
                outputStream.write(byteArray, 0, byteArrayLen);
                outputStream.flush();

                bufferedInputStream.close();
                fileInputStream.close();
                outputStream.close();

            } catch (Exception e) {
                System.out.println("Send Error");
            }
        }

    }

    public static void receive_files(Socket socket, String filesToReceive, String size_filesToReceive) {

        InputStream inputStream;
        FileOutputStream fileOutputStream;
        BufferedOutputStream bufferedOutputStream;

        String[] files = filesToReceive.split(",");
        String[] filesSize = size_filesToReceive.split(",");

        int currentPtr = 0;
        int readBytes;

        byte[] byteArr;

        try {

            inputStream = socket.getInputStream();

            for (int i = 0; i < files.length; i++){

                String file = files[i];
                int size = Integer.parseInt(filesSize[i]);
                byteArr = new byte[size];

                fileOutputStream = new FileOutputStream(file);
                bufferedOutputStream = new BufferedOutputStream(fileOutputStream);

                readBytes = inputStream.read(byteArr,0,byteArr.length);
                currentPtr = readBytes;

                do {

                    int newPtr = byteArr.length - currentPtr;
                    readBytes = inputStream.read(byteArr, currentPtr, newPtr);

                    if (readBytes >= 0) currentPtr += readBytes;

                } while (readBytes > -1);

                bufferedOutputStream.write(byteArr, 0 , currentPtr);
                bufferedOutputStream.flush();

                bufferedOutputStream.close();
                fileOutputStream.close();

            }

        } catch (IOException e) {
            System.out.println("Receive Error");
        }

    }





    public static void main(String[] args) throws IOException,InterruptedException
    {

        //create_metafile();
        get_data_from_metafile ();
        //Thread.sleep(30*1000);
        ArrayList<File> test = get_changes_files();
        ArrayList<String> test2 = get_deleted_filenames();
        get_changes_names();
        get_changes_sizes();


    }


}
