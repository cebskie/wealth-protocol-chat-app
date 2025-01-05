import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class S {
    private static HashMap<String,String> Pass;
    private static HashMap<String,String> GroupPass;

    private static HashMap<String,PrintWriter> writers = new HashMap<>();
    private static HashMap<String,List<String>> members = new HashMap<>();

    public static void main(String[] args) throws Exception {
        Pass=ReadPass(false);
        GroupPass=ReadPass(true);
        System.out.println("The chat server is running...");
        var pool = Executors.newFixedThreadPool(500);
        try (var listener = new ServerSocket(59001)) {
            while (true) {
                pool.execute(new Handler(listener.accept()));
            }
        }
    }

    private static class Handler implements Runnable {
        private String path;
        private String name;
        private String pass;
        private Socket socket;
        private Scanner in;
        private PrintWriter out;

        public Handler(Socket socket) {
            this.socket = socket;
        }
        
        public void run() {
            try {
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);

                while (true) {
                    out.println("SUBMITNAME");
                    name = in.nextLine();
                    if (name == null) {
                        return;
                    }
                    synchronized (Pass) {
                        if (!name.isBlank() && !Pass.containsKey(name)) {
                            out.println("NEWPASS");
                            pass= in.nextLine();
                            Pass.put(name, pass);
                            WriteFile(name+":"+pass,"Pass.txt");
                            break;
                        } else if (Pass.containsKey(name)){
                            out.println("SUBMITPASS");
                            pass= in.nextLine();
                            if(pass.equals(Pass.get(name))) break;
                        }
                    }
                }

                String other;
                while (true) {
                    out.println("NAMEACCEPTED " + name);
                    other = in.nextLine();
                    path = Reset(other, out, in);
                    if(other.startsWith("Group")){
                        other = " Group-"+other.substring(5);
                    } else if(other.startsWith("PC")){
                        other = " PC-"+other.substring(2);
                    }
                    if (path != null) break;
                }

                if(!members.containsKey(path)) {
                    members.put(path,new ArrayList<String>());
                }

                members.get(path).add(name);
                LoadSend(path,out);
                writers.put(name, out);
                out.println("CHATSTART "+ name + other);
                System.out.println("Chat");

                if(name != null){
                    WriteFile("MESSAGE " + name + " has joined",path);
                    System.out.println(name + " is joining");
                    for (String n:members.get(path)) {
                        writers.get(n).println("MESSAGE " + name + " has joined");
                    }
                }

                while (true) {
                    String input = in.nextLine();
                    if (input.toLowerCase().startsWith("/quit")) {
                        return;
                    } else if (input.startsWith("Group") || input.startsWith("PC")) {
                        members.get(path).remove(name);
                        String t;
                        t = path;
                        path = Reset(input,out,in);
                        if(input.startsWith("Group")){
                            input=" Group-"+input.substring(5);
                        } else if(input.startsWith("PC")){
                            input = " PC-"+input.substring(2);
                        }
                        if(path == null) path = t;
                        else {
                            if(!members.containsKey(path))members.put(path,new ArrayList<String>());
                            members.get(path).add(name);
                            out.println("RESET");
                            LoadSend(path,out);
                        }
                        out.println("CHATSTART "+ name + input);
                        
                    } else {
                        WriteFile("MESSAGE " + name + ": " + input,path);
                        for (String n:members.get(path)) {
                            writers.get(n).println("MESSAGE " + name + ": " + input);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
            } finally {
                if (out != null) {
                    writers.remove(name);
                    members.get(path).remove(name);
                }
                if (name != null) {
                    WriteFile("MESSAGE " + name + " has left",path);
                    System.out.println(name + " is leaving");
                    for (String n:members.get(path)) {
                        writers.get(n).println("MESSAGE " + name + " has left");
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }

        private static void LoadSend(String path,PrintWriter writer) {
            System.out.println(path);
            File myObj = new File(path);      
            try{
            if(!myObj.createNewFile()) {
                Scanner myReader = new Scanner(myObj);
                while(myReader.hasNextLine()) {
                    writer.println(myReader.nextLine());
                }
                myReader.close();
            }
        } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }

        private String Reset(String other,PrintWriter out,Scanner in){
            String temp="";
            if(other.startsWith("Group")) synchronized (GroupPass) {
                other = other.substring(5);
                if (!other.isBlank() && !GroupPass.containsKey(other)) {
                    out.println("NEWGROUPPASS");
                    pass = in.nextLine();
                    GroupPass.put(other,pass);
                    temp = "/Group/"+other+".txt";
                    WriteFile(other+":"+pass,"GroupPass.txt");
                    return temp;
                }else if (GroupPass.containsKey(other)){
                    out.println("SUBMITGROUPPASS");
                    pass= in.nextLine();
                    if(pass.equals(GroupPass.get(other))){
                        temp="Group/"+other+".txt";
                        return temp;
                    }else {
                        out.println("MESSAGE (PERSONAL) FAILED PASSWORD");
                    }
                }
            }else if(other.startsWith("PC")){
                other=other.substring(2);
                temp = "PC/" + (name.compareTo(other) < 0 ? name + "_" + other : other + "_" + name) + ".txt";
                return temp;
            }
            System.out.println(temp);
            return null;
        }
    }

    private static HashMap<String,String> ReadPass(boolean is_group) {
        File myObj = new File(!is_group?"Pass.txt":"GroupPass.txt");
        HashMap<String,String>temp=new HashMap<String,String>();
        String[] Line;        
        try{
            if(myObj.createNewFile()) {
                return temp;
            } else {
                Scanner myReader = new Scanner(myObj);
                while(myReader.hasNextLine()) {
                    Line = myReader.nextLine().split(":");
                    temp.put(Line[0],Line[1]);
                    System.out.println(Line[0]+"pass"+Line[1]);
                }
                myReader.close();
                return temp;
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
            return temp;
        }
    }

    private static void WriteFile(String s,String pathaim) {
        try {
            String[] part=pathaim.split("/");
            String where="";
            for(int i = 0; i < part.length-1; i++) {
                where+=part[i]+"/";
            }

            File myFile = new File(where);
            myFile.mkdirs();
            myFile=new File(pathaim);
            myFile.createNewFile();

            FileWriter myObj = new FileWriter(pathaim,true);
            myObj.write(s+"\n");
            myObj.close();
        }catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}