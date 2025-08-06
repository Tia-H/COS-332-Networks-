// u23576996 - Shinn-Ru Hung
// u23562732 - Yuthika Tia Harripersad

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;

public class prac4 {
    private static String html="";
    private static boolean databaseFound=true;
    private static ArrayList<String> friends = new ArrayList<String>(); //arrayList populated using textfile

    public static void main(String[] args) throws IOException {
        readFile();
        if (databaseFound==false)
        {
            return;
        }

        int userPort = 9002;
        try (ServerSocket friendServer = new ServerSocket(userPort))
        {
            System.out.println("Server alive :). Open http://127.0.0.1:9002 in your browser.");
            while (true)
            {
                //accept connection to server
                Socket client = friendServer.accept(); 
                //allow user input to be read
                InputStreamReader i = new InputStreamReader(client.getInputStream());
                BufferedReader userInput = new BufferedReader(i);
                PrintWriter display = new PrintWriter(client.getOutputStream(),true);
                
                //Read HTTP request
                String input = userInput.readLine();
                if (input!=null)
                {
                    if (input.startsWith("GET"))
                    {
                        String reqString = input.substring(4);
                        int space = reqString.indexOf(" ");
                        String file = reqString.substring(0,space);
                        if (file.equals("/"))
                        {
                            generateHomePage();
                            display.println(html);
                        }else if (file.startsWith("/home"))
                        {
                            generateHomePage();
                            display.println(html);
                        }else if (file.startsWith("/addFriend")) //addFriend?name=...&number=...
                        {
                            int amp = file.indexOf("&");
                            String name = file.substring(16,amp);
                            String number = file.substring(amp+8);
                            if (name=="" || number=="")
                            {
                                generateErrorPage();
                                display.println(html);
                                generateHomePage();
                            }else{
                                String contact = name + ":" + number;
                                friends.add(contact);
                                generateSuccessPage("FRIEND ADDED :)");
                                        display.println(html);
                                        generateHomePage();
                                try {
                                    FileWriter writer = new FileWriter("telnetDatabase.txt",false);
                                    BufferedWriter buffWriter = new BufferedWriter(writer);
                                    for (int j=0; j<friends.size();j++)
                                    {
                                        buffWriter.write(friends.get(j));
                                        buffWriter.write('\n');
                                    }
                                    buffWriter.close();
                                }catch(Exception e)
                                {

                                }
                            }
                        }else if (file.startsWith("/deleteFriend"))
                        {
                            int amp = file.indexOf("&");
                            String name = file.substring(19,amp);
                            String number = file.substring(amp+8);
                            if (name=="" || number=="")
                            {
                                generateErrorPage();
                                display.println(html);
                                generateHomePage();
                            }else{
                                String contact = name + ":" + number;
                                friends.remove(contact);
                                generateSuccessPage("FRIEND DELETED :)");
                                        display.println(html);
                                        generateHomePage();
                                try {
                                    FileWriter writer = new FileWriter("telnetDatabase.txt",false);
                                    BufferedWriter buffWriter = new BufferedWriter(writer);
                                    for (int j=0; j<friends.size();j++)
                                    {
                                        buffWriter.write(friends.get(j));
                                        buffWriter.write('\n');
                                    }
                                    buffWriter.close();
                                }catch(Exception e)
                                {
                                    
                                }
                            }
                        }else if (file.startsWith("/search"))
                        {//search?name=...&number=...
                            int amp = file.indexOf("&");
                            String name = file.substring(13,amp);
                            String number = file.substring(amp+8);
                            if (name=="" || number=="")
                            {
                                generateErrorPage();
                                display.println(html);
                                generateHomePage();
                            }else{
                                String contact = name + ":" + number;
                                boolean found=false;
                                for (int j=0; j<friends.size();j++)
                                {
                                    if (friends.get(j).equals(contact))
                                    {
                                        generateSuccessPage("FRIEND FOUND :)");
                                        display.println(html);
                                        generateHomePage();
                                        found=true;
                                    }    
                                }
                                if (found==false)
                                {
                                    generateSuccessPage("FRIEND NOT FOUND");
                                        display.println(html);
                                        generateHomePage();
                                }
                                
                            }
                        }else if (file.startsWith("/updateFriend"))
                        { //updateFriend?name=...&number=...&newName=...&newNumber=...
                            // /updateFriend?name=def&number=456&newName=defg&NewNumber=456
                            int amp = file.indexOf("&");
                            String name = file.substring(19,amp); //name
                            String file2 = file.substring(amp+1);
                            int amp2 = file2.indexOf("&");
                            String number = file2.substring(7,amp2); //number
                            String file3 = file2.substring(amp2+1);
                            int amp3 = file3.indexOf("&");
                            String newName = file3.substring(8,amp3); //newName
                            String newNumber= file3.substring(amp3+11); //newNumber

                            boolean found=false;
                            if (name=="" || number=="" || newName=="" || newNumber=="")
                            {
                                generateErrorPage();
                                display.println(html);
                                generateHomePage();
                            }else{
                                String contact = name + ":" + number;
                                String newContact = newName + ":" + newNumber;
                                for (int j=0; j<friends.size();j++)
                                {
                                    if (friends.get(j).equals(contact))
                                    {
                                        found=true;
                                        friends.set(j,newContact);
                                        generateSuccessPage("FRIEND UPDATED :)");
                                        display.println(html);
                                        generateHomePage();
                                        try {
                                            FileWriter writer = new FileWriter("telnetDatabase.txt",false);
                                            BufferedWriter buffWriter = new BufferedWriter(writer);
                                            for (int k=0; k<friends.size();k++)
                                            {
                                                buffWriter.write(friends.get(k));
                                                buffWriter.write('\n');
                                            }
                                            buffWriter.close();
                                        }catch(Exception e)
                                        {
                                            
                                        }
                                    }    
                                }
                                if (found==false)
                                {
                                    generateSuccessPage("FRIEND NOT FOUND");
                                        display.println(html);
                                        generateHomePage();
                                }
                            }
                        }else if (file.startsWith("/display"))
                        {
                            generateFriendPage();
                            display.println(html);
                        }
                    }else{
                        generateErrorPage();
                        display.println(html);
                    }
                }else{
                    generateErrorPage();
                    display.println(html);
                }
            }
        }catch (IOException e)
        {
            System.out.println("Server did not start :(");
        }
    }

    private static void readFile()
    {
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
    }

    private static void generateFriendPage() 
    {
        html = 
                "HTTP/1.1 200 OK \r\n" +
                "Content-Type: text/html \r\n\r\n" + 
                "<!DOCTYPE html> \n" +
                "<html>" +
                "<title>FriendBook</title>" +
                "<body>" +

                "<h1>WELCOME TO AUSTIN & TIA'S PHONEBOOK !!!</h1>" +
                "<b>All friends</b> <br>" ;
                Collections.sort(friends);
                for (int i=0;i<friends.size();i++)
                {
                   html += (i+1) + ". " + friends.get(i) +"<br>";
                }

                html+= "<form method='get' action='http://127.0.0.1:9002/home'>" +
                "<input type='submit' value='Back To Home Page'>" +
                "</form>" +

                "</body></html>";
    }

    private static void generateErrorPage() 
    {
        html = 
                "HTTP/1.1 400 Bad Request \r\n" +
                "Content-Type: text/html \r\n\r\n" + 
                "<!DOCTYPE html> \n" +
                "<html>" +
                "<title>FriendBook</title>" +
                "<body>" +

                "<h1>Error 400: Bad request</h1>" +
                "<b>Invalid input or request</b> <br>" +

                "<form method='get' action='http://127.0.0.1:9002/home'>" +
                "<input type='submit' value='OK'>" +
                "</form>" +

                "</body></html>";
    }

    private static void generateSuccessPage(String s) 
    {
        html = 
                "HTTP/1.1 200 OK \r\n" +
                "Content-Type: text/html \r\n\r\n" + 
                "<!DOCTYPE html> \n" +
                "<html>" +
                "<title>FriendBook</title>" +
                "<body>" +

                "<h1>Success 200: OK</h1>" +
                "<b>" + s + "</b> <br>" +

                "<form method='get' action='http://127.0.0.1:9002/home'>" +
                "<input type='submit' value='OK'>" +
                "</form>" +

                "</body></html>";
    }

    private static void generateHomePage() 
    {
        html = 
                "HTTP/1.1 200 OK \r\n" +
                "Content-Type: text/html \r\n\r\n" + 
                "<!DOCTYPE html> \n" +
                "<html>" +
                "<title>FriendBook</title>" +
                "<body>" +

                "<h1>WELCOME TO AUSTIN & TIA'S PHONEBOOK !!!</h1>" +
                "<form method='get' action='http://127.0.0.1:9002/addFriend'>" +
                "<b>Add New Friend</b> <br>" + 
                "Friend Name: <input type='text' name='name'><br>" + 
                "Friend Number: <input type='text' name='number'><br>" + 
                "<input type='submit' value='Add Friend'>" +
                "</form>" +
                "<br>" +

                "<form method='get' action='http://127.0.0.1:9002/deleteFriend'>" +
                "<b>Delete Friend</b> <br>" + 
                "Friend Name: <input type='text' name='name'><br>" + 
                "Friend Number: <input type='text' name='number'><br>" + 
                "<input type='submit' value='Delete Friend'>" +
                "</form>" +
                "<br>" +

                "<form method='get' action='http://127.0.0.1:9002/updateFriend'>" +
                "<b>Update Friend</b> <br>" + 
                "Friend Name: <input type='text' name='name'><br>" + 
                "Friend Number: <input type='text' name='number'><br>" + 
                "Current Name: <input type='text' name='newName'><br>" + 
                "Current Number: <input type='text' name='NewNumber'><br>" + 
                "<input type='submit' value='Update Friend'>" +
                "</form>" +
                "<br>" +

                "<form method='get' action='http://127.0.0.1:9002/search'>" +
                "<b>Search Friend</b> <br>" + 
                "Friend Name: <input type='text' name='name'><br>" + 
                "Friend Number: <input type='text' name='number'><br>" + 
                "<input type='submit' value='Search Friend'>" +
                "</form>" +
                "<br>" +

                "<form method='get' action='http://127.0.0.1:9002/display'>" +
                "<input type='submit' value='Display all friends'>" +
                "</form>" +

                "</body></html>";
    }
}