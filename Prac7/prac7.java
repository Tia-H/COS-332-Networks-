// u23576996 - Shinn-Ru Hung
// u23562732 - Yuthika Tia Harripersad

import java.io.*;
import java.net.*;
import java.util.*;

public class pop3Server {
    private static final String emailServer = "localhost";
    private static final int POP3_PORT = 110; //995 for encryption 
    private static final String email = "tia@prac7.com";
    private static final String emailPass = "prac7";

    public static void main(String[] args)
    {
        connectToEmail();
    }

    public static void connectToEmail()
    {
        boolean runServer=true; //track server state
        try (Socket pop3Socket = new Socket(emailServer,POP3_PORT))
        {
            InputStreamReader i = new InputStreamReader(pop3Socket.getInputStream());
            BufferedReader emailInput = new BufferedReader(i);
            PrintWriter terminal = new PrintWriter(pop3Socket.getOutputStream(),true);
            //login credentials
            String user = "USER " + email;
            String pass = "PASS "+emailPass;

            String serverResponse = emailInput.readLine();
            System.out.println("EMAIL START: " + serverResponse );

            terminal.println(user);
            emailInput.readLine();
            terminal.println(pass);
            String res = emailInput.readLine();
            System.out.println("EMAIL LOGIN RESPONSE: " + res +'\n');

            if (res.equals("+OK Mailbox locked and ready")) 
            {
                HashMap<Integer,Integer> emails = listEmails(terminal,emailInput);

                deleteEmails(terminal,emailInput,emails); 
                while (runServer==true) 
                {
                    InputStreamReader in = new InputStreamReader(System.in); 
                    BufferedReader userInput = new BufferedReader(in);
                    String input = userInput.readLine();
                    switch (input) 
                    { 
                        case "RESET":
                            terminal.println("RSET");
                            res = emailInput.readLine();
                            System.out.println( res );
                            break;
                        case "QUIT":
                            quit(terminal, emailInput);
                            break;
                        case "GET":
                            String id = userInput.readLine();
                            getEmail(terminal, emailInput, id);
                            break;
                        case "LIST":
                            listEmails(terminal, emailInput);
                            break;
                        case "DELETE":
                            emails = listEmails(terminal,emailInput);
                            deleteEmails(terminal,emailInput,emails);
                            break;
                        case "BYE":
                            runServer=false;
                            break;
                        case "RECONNECT":
                            connectToEmail();
                            break;
                        default: 
                            break;

                    }
                }
            }
        }catch (IOException e)
        {
            System.out.println("Could not start server");
        }
    }

    public static HashMap<Integer,Integer> listEmails(PrintWriter terminal,BufferedReader emailInput)
    {
        HashMap<Integer,Integer> emails = new HashMap<Integer,Integer>();
        terminal.println("LIST");
        try{
            String res = emailInput.readLine();
            System.out.println("EMAILS:");
            System.out.println( res );
            while (!(res = emailInput.readLine()).equals("."))
            {
                System.out.println( res );
                if (!res.equals("."))
                {
                    String[] idLen = res.split("\\s+");
                    int id = Integer.parseInt(idLen[0]);
                    int len = Integer.parseInt(idLen[1]);
                    emails.put(id,len);
                }
            }

            for (int id: emails.keySet())
            {
                int len = emails.get(id);
                terminal.println("TOP "+id+" 0"); 
                System.out.println("------------------------------");
                 System.out.println(" EMAIL " +id);
                    while (!(res = emailInput.readLine()).equals("."))
                    {
                        if (res.contains("From:") || res.contains("Subject:"))
                        {
                            System.out.println( res );
                        }
                        if (res.contains("FROM:") || res.contains("SUBJECT:"))
                        {
                            System.out.println( res );
                        }
                    }
                System.out.println("Size: " + len);
                System.out.println("------------------------------");
            }
            return emails;
        }catch (IOException E)
        {
            return emails;
        }
    }
    public static void deleteEmails(PrintWriter terminal,BufferedReader emailInput,HashMap<Integer,Integer> emails)
    {
        InputStreamReader i = new InputStreamReader(System.in);
        BufferedReader userInput = new BufferedReader(i);

        System.out.println("DELETE EMAILS? ");
        try{
            String input = userInput.readLine();
            String[] idStr = (input.split(","));
            for (String k:idStr)
            {
                terminal.println("DELE " + k);
                String res = emailInput.readLine();
                System.out.println( res );
            }
        }catch (IOException e)
        {

        }

    }

    public static void getEmail(PrintWriter terminal,BufferedReader emailInput,String id)
    {
        terminal.println("RETR " + id);
        String res;
        try{
                 System.out.println(" EMAIL " +id);
                    while (!(res = emailInput.readLine()).equals("."))
                    {
                        //if (res.contains("From:") || res.contains("Subject:"))
                        {
                            System.out.println( res );
                        }
                    }
        }catch (IOException e)
        {

        }

    }
    public static void quit(PrintWriter terminal,BufferedReader emailInput)
    {
         try{
            terminal.println("QUIT");
                String res = emailInput.readLine();
                System.out.println( res );
        }catch (IOException e)
        {

        }
        
    }
    
}

