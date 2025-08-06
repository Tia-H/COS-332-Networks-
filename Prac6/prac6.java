// u23576996 - Shinn-Ru Hung
// u23562732 - Yuthika Tia Harripersad
//sssssss
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import javax.mail.*;
import javax.mail.internet.*;

public class prac6 {
    private static final String SMTP_SERVER = "127.0.0.1";
    private static final int SMTP_PORT = 25;
    private static final String SENDER_EMAIL = "alarm@localhost.com";
    private static final String RECIPIENT_EMAIL = "owner@localhost.com";
    
    private static final Map<String, String> SENSORS = new HashMap<>();
    static {
        SENSORS.put("1", "Front door opened");
        SENSORS.put("2", "Back door opened");
        SENSORS.put("3", "Window movement detected");
        SENSORS.put("4", "Motion in living room");
        SENSORS.put("5", "Motion in bedroom");
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("> Press 1-5 to trigger alarms, 'c' to check inbox, 'q' to quit.");
        
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();
                
                if (input.equalsIgnoreCase("q")) {
                    System.out.println("Shutting down alarm system.");  // <-- THIS LINE ADDED
                    break;
                }
                
                if (input.equalsIgnoreCase("c")) {
                    checkInbox();
                    continue;
                }
                
                if (SENSORS.containsKey(input)) {
                    String alert = SENSORS.get(input);
                    System.out.println("ALERT: " + alert);
                    sendAlertEmail(alert);
                    Thread.sleep(1000);
                    checkInbox();
                }
            }
        }
    }

    private static void sendAlertEmail(String alertMessage) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try (Socket socket = new Socket(SMTP_SERVER, SMTP_PORT);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                // Read server greeting
                String response = in.readLine();
                System.out.println("Server: " + response);
                if (!response.startsWith("220")) {
                    throw new IOException("SMTP server not ready");
                }

                // Send HELO
                out.println("HELO localhost");
                response = in.readLine();
                System.out.println("Server: " + response);
                if (!response.startsWith("250")) {
                    throw new IOException("HELO failed");
                }

                // Set sender
                out.println("MAIL FROM: <" + SENDER_EMAIL + ">");
                response = in.readLine();
                System.out.println("Server: " + response);
                if (!response.startsWith("250")) {
                    throw new IOException("MAIL FROM failed");
                }

                // Set recipient
                out.println("RCPT TO: <" + RECIPIENT_EMAIL + ">");
                response = in.readLine();
                System.out.println("Server: " + response);
                if (!response.startsWith("250")) {
                    throw new IOException("RCPT TO failed");
                }

                // Send email data
                out.println("DATA");
                response = in.readLine();
                System.out.println("Server: " + response);
                if (!response.startsWith("354")) {
                    throw new IOException("DATA failed");
                }

                // Email headers and body
                out.println("From: " + SENDER_EMAIL);
                out.println("To: " + RECIPIENT_EMAIL);
                out.println("Subject: ALARM: " + alertMessage);
                out.println(); // Empty line separates headers from body
                out.println("ALARM NOTIFICATION");
                out.println("Time: " + new Date());
                out.println("Event: " + alertMessage);
                out.println("Please check your property immediately!");
                out.println("."); // End of email

                response = in.readLine();
                System.out.println("Server: " + response);
                if (!response.startsWith("250")) {
                    throw new IOException("Message not accepted");
                }

                // Quit
                out.println("QUIT");
                response = in.readLine();
                System.out.println("Server: " + response);

                System.out.println("Alert email sent successfully!");
            } catch (IOException e) {
                System.err.println("Failed to send alert email: " + e.getMessage());
            }
        });
        executor.shutdown();
    }

    public static void checkInbox() {
        final String host = "localhost";
        final String username = "owner@localhost.com";
        final String password = "owner123";
        final int port = 143; // IMAP port (993 for SSL)

        Properties props = new Properties();
        props.put("mail.imap.host", host);
        props.put("mail.imap.port", port);
        props.put("mail.imap.auth", "true");

        try {
            Session session = Session.getInstance(props);
            Store store = session.getStore("imap");
            store.connect(host, username, password);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.getMessages();
            System.out.println("Total Emails: " + messages.length);
            
            for (Message msg : messages) {
                System.out.println("Subject: " + msg.getSubject());
                System.out.println("Content: " + msg.getContent().toString().trim());
            }

            inbox.close(false);
            store.close();
        } catch (Exception e) {
            System.err.println("Failed to check inbox: " + e.getMessage());
        }
    }

    
}

