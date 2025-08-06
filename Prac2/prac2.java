// u23576996 - Shinn-Ru Hung
// u23562732 - Yuthika Tia Harripersad

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;

public class prac2{
    public static void main(String[]  args) 
    {
        boolean databaseFound=true;
        int portNum = 8080;
        //use ArrayList to allow array to dynamically grow or shrink in size
        ArrayList<String> friends = new ArrayList<String>(); //arrayList populated using textfile
        //check if textfile exists ,else create one 
        File dbFile = new File("telnetDatabase.txt");
            if (dbFile.exists()==false)
            {
                try
                {
                    dbFile.createNewFile();
                }catch (Exception e)
                {
                    //System.out.println("File could not be created");
                    databaseFound=false;
                }
            }
         //populate friends arrayList with textfile
        try {
            FileReader fileReader = new FileReader("telnetDatabase.txt");
            BufferedReader buffReader = new BufferedReader(fileReader);
            String fileLine= buffReader.readLine();
            while (fileLine!=null)
            {
                friends.add(fileLine);
                fileLine= buffReader.readLine();
            }
            buffReader.close();
        }catch(IOException e)
        {
            //System.out.println("telnetDatabase.txt not found");
            databaseFound=false;
        }

        //System.err.println("Connecting on server on 8080...");
        //attempt to create server on portNum
        try (ServerSocket telnetServer = new ServerSocket(portNum))
        {
            //System.out.println("Telnet server alive :)"); //shows server connection successful
            //allow multiple users to access server
            while (true) {
                //allow users to connect to telnet Server
                Socket user = telnetServer.accept();
                //System.out.println("User connected");
                //user input from terminal
                BufferedReader userInput = new BufferedReader(new InputStreamReader(user.getInputStream()));
                PrintWriter cmdDisplay = new PrintWriter(user.getOutputStream(), true);

                //Display messages to user
                cmdDisplay.write(27);
                cmdDisplay.print("[2J");
                cmdDisplay.write(27);
                cmdDisplay.print("[1;20H");
                cmdDisplay.println(" <-----   Welcome to Austin and Tia's Telnet Server   ----->");
                cmdDisplay.write(27);
                cmdDisplay.print("[3;1H");
                cmdDisplay.println("Here you can store your friends' phone numbers! (Type 'bye' to quit.)");
                if (friends.size()==0)
                {
                    cmdDisplay.println("No friends' phone numbers added yet...");
                }
                if (databaseFound==false)
                {
                    cmdDisplay.println("Database error occurred,cannot store phone numbers :(");
                }
                cmdDisplay.write(27);
                cmdDisplay.print("[4;10H");
                cmdDisplay.println("The following commands exist: add, search, update, delete");
                cmdDisplay.write(27);
                cmdDisplay.print("[5;12H");
                cmdDisplay.println("Command format: command-FriendName:PhoneNumber");
                cmdDisplay.write(27);
                cmdDisplay.print("[6;12H");
                cmdDisplay.println("update format: command-FriendName:PhoneNumber=FriendName:PhoneNumber");
                int line=8;
                cmdDisplay.write(27);
                cmdDisplay.print("["+line+";1H");

                //read user input
                String input = (userInput.readLine()).trim();
                while (input != null)
                {
                    line++;
                    line++;
                    if (input.equals("bye")) {
                        cmdDisplay.write(27);
                        cmdDisplay.print("[2J");
                        cmdDisplay.write(27);
                        cmdDisplay.print("[1;20H");
                        cmdDisplay.println("Bye bye!");
                        break;
                    }
                    if (input.contains("-"))
                    {
                        cmdDisplay.println(input);
                        //line++;
                        String[] commandParts = input.split("-");
                        String command = (commandParts[0]).trim();
                        String friendData = (commandParts[1]).trim();
                        switch (command)
                        {
                            case "add":
                                        friends.add(friendData);
                                        try {
                                            FileWriter writer = new FileWriter("telnetDatabase.txt",true);
                                            BufferedWriter buffWriter = new BufferedWriter(writer);
                                            buffWriter.write(friendData);
                                            buffWriter.write('\n');
                                            buffWriter.close();
                                        }catch(Exception e)
                                        {
                                            cmdDisplay.write(27);
                                            cmdDisplay.print("["+line+";1H");
                                            cmdDisplay.println("Friend not added");
                                            line++;
                                        }
                                break;
                            case "search":
                                        if (friends.contains(friendData)==true)
                                        {
                                            cmdDisplay.write(27);
                                            cmdDisplay.print("["+line+";1H");
                                            cmdDisplay.println("Friend found");
                                            line++;
                                        }else{
                                            cmdDisplay.write(27);
                                            cmdDisplay.print("["+line+";1H");
                                            cmdDisplay.println("Friend not found");
                                            line++;
                                        }
                                break;
                            case "update":
                                        String[] updateParts = friendData.split("=");
                                        String old = (updateParts[0]).trim();
                                        String newDetails = (updateParts[1]).trim();
                                        if (friends.contains(old)==true)
                                        {
                                            int index = friends.indexOf(old);
                                            friends.set(index,newDetails);
                                            cmdDisplay.write(27);
                                            cmdDisplay.print("["+line+";1H");
                                            cmdDisplay.println("Friend updated");
                                            line++;
                                        }else{
                                            cmdDisplay.write(27);
                                            cmdDisplay.print("["+line+";1H");
                                            cmdDisplay.println("Friend not found");
                                            line++;
                                        }
                                        try {
                                            FileWriter writer = new FileWriter("telnetDatabase.txt",false);
                                            BufferedWriter buffWriter = new BufferedWriter(writer);
                                            for (int i=0; i<friends.size();i++)
                                            {
                                                buffWriter.write(friends.get(i));
                                                buffWriter.write('\n');
                                            }
                                            buffWriter.close();
                                        }catch(Exception e)
                                        {
                                            cmdDisplay.write(27);
                                            cmdDisplay.print("["+line+";1H");
                                            cmdDisplay.print("Friend not updated");
                                            line++;
                                        }
                                break;
                            case "delete":
                                        friends.remove(friendData);
                                        try {
                                            FileWriter writer = new FileWriter("telnetDatabase.txt",false);
                                            BufferedWriter buffWriter = new BufferedWriter(writer);
                                            for (int i=0; i<friends.size();i++)
                                            {
                                                buffWriter.write(friends.get(i));
                                                buffWriter.write('\n');
                                            }
                                            buffWriter.close();
                                        }catch(Exception e)
                                        {
                                            cmdDisplay.write(27);
                                            cmdDisplay.print("["+line+";1H");
                                            cmdDisplay.print("Friend not found");
                                            line++;
                                        }
                                break;
                            case "sort": //sort-asc, sort/desc
                                        if (friendData.equals("asc"))
                                        {
                                            //System.out.println("Sorted friends list in ascending order:");
                                            //line++;
                                            Collections.sort(friends);
                                            for (int i=0; i<friends.size();i++)
                                            {
                                                cmdDisplay.write(27);
                                                cmdDisplay.print("[" + line + ";1H");
                                                cmdDisplay.println(friends.get(i));
                                                line++;
                                            }
                                            line++;
                                        }
                                        if (friendData.equals("desc"))
                                        {
                                            //System.out.println("Sorted friends list in descending order:");
                                            //line++;
                                            Collections.sort(friends);
                                            for (int i=friends.size()-1; i>=0;i--)
                                            {
                                                cmdDisplay.write(27);
                                                cmdDisplay.print("[" + line + ";1H");
                                                cmdDisplay.println(friends.get(i));
                                                line++;
                                            }
                                            line++;
                                        }
                            break;
                        }
                    }
                    input = (userInput.readLine()).trim();
                }

                user.close();
                //System.out.println("User disconnected.");
            }

        }catch (IOException e){
            //System.out.println("start server unsuccessful :(");
        }
    }
    

}

