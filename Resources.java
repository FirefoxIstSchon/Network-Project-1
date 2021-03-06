
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
    public static String path = "/Users/k/git/Network-Project-1/";
    public static String foldername = "DriveCloud/";
    public static File dir = new File (path+foldername);
    public static Hashtable hash_table_for_files = new Hashtable();


    public Resources(){}

    public static Boolean is_changed(){

        // changes if any file is changed

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

        // reads the metafile and checks the current status for changes

        File metafile = new File(path+ foldername+"metafile.txt");
        if(metafile.createNewFile()){
            //System.out.println("File Created");
        }
        Scanner sc = new Scanner(metafile);
        while (sc.hasNextLine()){
            String[] line =sc.nextLine().split(" ");
            hash_table_for_files.put(line[0],Long.parseLong(line[1]));
        }
    }


    public static void create_metafile ()throws IOException{

        //creating a metafile to store changes

        File metafile = new File(path+foldername+"metafile.txt");

        //checking if the file already exists

        if (!metafile.createNewFile()) {

            // truncating the file if it already exist

            try (FileChannel outChan = new FileOutputStream(metafile, true).getChannel()) {
                outChan.truncate(0);
            }
        }

        // update the metafile information

        File [] files = dir.listFiles();
        FileWriter fileWriter = new FileWriter(metafile);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        long hashcode = -1;
        String str = "";
        for (File file_entry : files)
        {
            {

                // adding hashcode to the meta file to compare files later on

                hashcode=file_entry.lastModified();
                str=str+file_entry.getName()+ " ";
                if(!str.contains("metafile.txt")&&!str.contains(".DS_Store")){

                    //System.out.println(file_entry.getName());

                    str=str+hashcode;
                    printWriter.println(str);

                }
                str = "";
            }
        }
        printWriter.close();


    }


    public static ArrayList<File> get_changes_files() {


        try {
            get_data_from_metafile ();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<File> changed_files =new ArrayList<File>();

        //get current files

        File [] current_files =dir.listFiles();
        long hashcode = 0;
        if (current_files != null) {

            //this part is to find added and modified files

            for (File file_entry : current_files)
            {
                //We dont need to compare metafile


                if(!file_entry.getName().equals("metafile.txt")&&!file_entry.getName().equals(".DS_Store"))
                {

                    hashcode=file_entry.lastModified();

                    if (hash_table_for_files.containsKey(file_entry.getName()) && (long)hash_table_for_files.get(file_entry.getName())!=hashcode) {
                        // System.out.println("Modified file "+file_entry.getName());
                        changed_files.add(file_entry);

                    }else if(!hash_table_for_files.containsKey(file_entry.getName())){
                        // System.out.println("Added file "+file_entry.getName());
                        changed_files.add(file_entry);
                    }

                    hash_table_for_files.remove(hashcode);

                }
            }
        }

        return changed_files;

    }


    public static ArrayList<String> get_deleted_filenames() {

        try {
            get_data_from_metafile ();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<File> changed_files =new ArrayList<File>();
        ArrayList<String> deleted_files=new ArrayList<String>();

        //get current files

        File [] current_files =dir.listFiles();
        long hashcode = 0;

        if (current_files != null) {
            for (File file_entry : current_files)
            {
                //this part is to find added and modified files

                //We dont need to compare metafile

                if(!file_entry.getName().equals("metafile.txt")&&!file_entry.getName().equals(".DS_Store"))
                {

                    hashcode=file_entry.lastModified();

                    if (hash_table_for_files.containsKey(file_entry.getName()) && (long)hash_table_for_files.get(file_entry.getName())!=hashcode) {
                        changed_files.add(file_entry);

                    }else if(!hash_table_for_files.containsKey(file_entry.getName())){
                        changed_files.add(file_entry);
                    }

                    hash_table_for_files.remove(file_entry.getName());


                }
            }

        }

        //this part is for finding deleted files

        //if it is not empty it means that there are some deleted files. Because we have deleted all modified and unmodified elements before.

        if(!hash_table_for_files .isEmpty())
        {
            //System.out.println("Finding deleted files.");

            //This iteration is for finding the deleted file.
            for(File file: current_files)
            {
                if(hash_table_for_files.containsKey(file.getName()))
                {
                    hash_table_for_files.remove(file.getName());
                }


            }

        }
        if(!hash_table_for_files .isEmpty())
        {
            for (Object deletedfile: hash_table_for_files.keySet()){
                deleted_files.add((String)deletedfile);

            }
        }

        return deleted_files;

    }


    public static String get_changes_names() {


        ArrayList<File> changed_files =get_changes_files();
        ArrayList<String> deleted_files =get_deleted_filenames();
        String str = "";

        for(File changed_file: changed_files)
        {
            str = str + changed_file.getName()+",";

        }

        for(String deleted_file: deleted_files)
        {
            str = str + deleted_file+",";

        }

        if(str.length()>=1){
            str = str.substring(0,str.length()-1);
        }

        return str;

    }


    public static String get_changes_sizes() {

        ArrayList<File> changed_files = get_changes_files();
        ArrayList<String> deleted_files =get_deleted_filenames();
        String str = "";

        for(File changed_file: changed_files)
        {
            str=str + changed_file.length()+",";

        }

        for(String deleted_file: deleted_files)
        {
            str=str + (-999)+",";

        }

        if(str.length()>=1){
            str =str.substring(0,str.length()-1);
        }

        return str;

    }


    public static String get_checksums(){

        ArrayList<File> changed_files = get_changes_files();

        String str = "";
        for(File changed_file: changed_files)
        {
            str=str + changed_file.hashCode()+",";

        }

        if(str.length()>=1){
            str = str.substring(0,str.length()-1);
        }

        return str;

    }


    public static String get_checksum_for(String filename){

        ArrayList<File> changed_files = get_changes_files();
        File [] files = dir.listFiles();
        String str = "";

        for(File file: files)
        {
            if(file.getName().equals(filename)){
                str=str+file.hashCode();
            }

        }

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
                System.out.flush();

                outputStream.flush();
                System.out.flush();

                bufferedInputStream.close();
                fileInputStream.close();

            } catch (Exception e) {
                // System.out.println("Send Error:"+e);
            }

        }

    }


    public static boolean receive_files(Socket socket, String filesToReceive, String size_filesToReceive) {

        InputStream inputStream;
        FileOutputStream fileOutputStream;
        BufferedOutputStream bufferedOutputStream;

        String[] files = filesToReceive.split(",");
        String[] filesSize = size_filesToReceive.split(",");
        // String[] fileCs = fileChecksums.split(",");

        int currentPtr = 0;
        int readBytes;

        byte[] byteArr;

        try {

            inputStream = socket.getInputStream();

            for (int i = 0; i < files.length; i++){

                String file = files[i];

                String file_path = path+foldername+file;

                File file_obj = new File(file_path);
                file_obj.delete();



                if (!filesSize[i].equals("-999") && filesSize[i].length()!=0) {

                    int size = Integer.parseInt(filesSize[i]);
                    byteArr = new byte[size];

                    fileOutputStream = new FileOutputStream(file_path);
                    bufferedOutputStream = new BufferedOutputStream(fileOutputStream);

                    readBytes = inputStream.read(byteArr, 0, byteArr.length);
                    currentPtr = readBytes;

                    do {

                        int newPtr = byteArr.length - currentPtr;

                        readBytes = inputStream.read(byteArr, currentPtr, newPtr);

                        if (readBytes > 0) currentPtr += readBytes;

                        // System.out.println(readBytes);

                    } while (readBytes != 0);

                    bufferedOutputStream.write(byteArr, 0, currentPtr);

                    bufferedOutputStream.flush();

                    bufferedOutputStream.close();
                    fileOutputStream.close();

                    //String this_checksum = Resources.get_checksum_for(file);
                    //String that_checksum = fileCs[i];
//                    if (!this_checksum.equals(that_checksum)) return false;

                }

            }

            return true;

        } catch (IOException e) {
            System.out.println("Receive Error");
        }

        return false;

    }



    public static void main(String[] args) throws IOException,InterruptedException
    {

        //
        //get_data_from_metafile ();
        //Thread.sleep(30*1000);
        ArrayList<File> test = get_changes_files();
        ArrayList<String> test2 = get_deleted_filenames();
        //get_changes_names();
        //get_changes_sizes();
        //create_metafile();
    }


}