// u23576996 - Shinn-Ru Hung
// u23562732 - Tia Harripersad
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

public class HtmlFileMonitor {
    private static final int POLL_INTERVAL = 5000; // 5 seconds for testing
    private static final int FTP_PORT = 21;
    private static final int BUFFER_SIZE = 4096;
    
    private String server;
    private String username;
    private String password;
    private String remoteDir;
    private String localFile;
    private String remoteFile;
    private long lastModified;
    private long lastUploadTime;
    
    public HtmlFileMonitor(String server, String username, String password, 
                         String remoteDir, String localFile, String remoteFile) 
                         {
        this.server = server;
        this.username = username;
        this.password = password;
        this.remoteDir = remoteDir;
        this.localFile = localFile;
        this.remoteFile = remoteFile;
        this.lastModified = 0;
        this.lastUploadTime = 0;
    }
    
    public void startMonitoring() 
    {
        System.out.println("Starting to monitor file: " + localFile);
        System.out.println("Remote destination: " + remoteDir + "/" + remoteFile);
        
        File file = new File(localFile);
        if (!file.exists()) 
        {
            System.err.println("ERROR: Local file does not exist!");
            return;
        }
        
        lastModified = file.lastModified();
        System.out.println("Initial last modified time: " + new Date(lastModified));
        
        while (true) 
        {
            try {
                file = new File(localFile);
                long currentModified = file.lastModified();

                System.out.println("\n[Check at " + new Date() + "]");
                System.out.println("Current modified: " + new Date(currentModified));
                System.out.println("Last known modified: " + new Date(lastModified));
                
                if (currentModified > lastModified) 
                {
                    System.out.println("\n>>> File change detected! <<<");
                    if (uploadFile()) 
                    {
                        System.out.println("*** Upload successful at " + new Date() + " ***");
                        lastModified = currentModified;
                        lastUploadTime = System.currentTimeMillis();
                    } 
                    else 
                    {
                        System.err.println("!!! Upload failed !!!");
                    }
                } 
                else 
                {
                    System.out.println("No changes detected");
                }
                
                Thread.sleep(POLL_INTERVAL);
            } 
            catch (Exception e) 
            {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private boolean uploadFile() 
    {
    Socket controlSocket = null;
    Socket dataSocket = null;
    
    try 
    {
        // 1. Establish control connection
        controlSocket = new Socket(server, FTP_PORT);
        BufferedReader controlReader = new BufferedReader(
            new InputStreamReader(controlSocket.getInputStream()));
        PrintWriter controlWriter = new PrintWriter(
            controlSocket.getOutputStream(), true);
        
        // Read welcome message
        String response = readFtpResponse(controlReader);
        if (!response.startsWith("220")) 
        {
            throw new IOException("FTP server not ready: " + response);
        }
        
        // 2. Login
        sendFtpCommand(controlWriter, "USER " + username);
        response = readFtpResponse(controlReader);
        if (!response.startsWith("331")) 
        {
            throw new IOException("Username not accepted: " + response);
        }
        
        sendFtpCommand(controlWriter, "PASS " + password);
        response = readFtpResponse(controlReader);
        if (!response.startsWith("230")) 
        {
            throw new IOException("Login failed: " + response);
        }
        
        // 3. Set passive mode
        sendFtpCommand(controlWriter, "PASV");
        response = readFtpResponse(controlReader);
        int dataPort = parsePassivePort(response);
        if (dataPort == -1) 
        {
            throw new IOException("Could not parse passive port");
        }
        dataSocket = new Socket(server, dataPort);
        
        // 5. Set binary mode
        sendFtpCommand(controlWriter, "TYPE I");
        response = readFtpResponse(controlReader);
        if (!response.startsWith("200")) 
        {
            throw new IOException("Could not set binary mode: " + response);
        }
        
        // 6. Start file transfer with full path
        sendFtpCommand(controlWriter, "STOR " + remoteFile);
        response = readFtpResponse(controlReader);
        if (!response.startsWith("150")) 
        {
            throw new IOException("STOR command failed: " + response);
        }
        
        // 7. Transfer file data
        try (InputStream fileInput = new FileInputStream(localFile);
             OutputStream dataOutput = dataSocket.getOutputStream()) 
            {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = fileInput.read(buffer)) != -1) 
            {
                dataOutput.write(buffer, 0, bytesRead);
            }
        }
        
        // 8. Verify completion
        dataSocket.close();
        response = readFtpResponse(controlReader);
        if (!response.startsWith("226")) 
        {
            throw new IOException("Transfer not completed: " + response);
        }

        sendFtpCommand(controlWriter, "SYST" );
        response = readFtpResponse(controlReader);
        if (!response.startsWith("215")) 
        {
            throw new IOException("SYST command failed: " + response);
        }
        sendFtpCommand(controlWriter, "STAT" );
        response = readFtpResponse(controlReader);
        while (!response.contains("End of status"))
        {
            response = controlReader.readLine();
            System.out.println(response);
        }
        sendFtpCommand(controlWriter, "STAT " + "index.html");
        response = readFtpResponse(controlReader);
        while (!response.contains("End of status"))
        {
            response = controlReader.readLine();
            System.out.println(response);
        }

        sendFtpCommand(controlWriter, "SIZE " +"index.html");
        response = readFtpResponse(controlReader);
        
        return true;
    } catch (Exception e) 
    {
        System.err.println("Upload error: " + e.getMessage());
        e.printStackTrace();
        return false;
    } 
    finally 
    {
        try { if (dataSocket != null) dataSocket.close(); } catch (IOException e) {}
        try { if (controlSocket != null) controlSocket.close(); } catch (IOException e) {}
    }
}
    
    private void sendFtpCommand(PrintWriter writer, String command) 
    {
        System.out.println("Sending: " + command);
        writer.println(command);
    }
    
    private String readFtpResponse(BufferedReader reader) throws IOException 
    {
        String response = reader.readLine();
        System.out.println("Received: " + response);
        return response;
    }
    
    private int parsePassivePort(String response) 
    {
        Pattern pattern = Pattern.compile("\\(([0-9]+,[0-9]+,[0-9]+,[0-9]+,[0-9]+,[0-9]+)\\)");
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            String[] parts = matcher.group(1).split(",");
            if (parts.length == 6) {
                int p1 = Integer.parseInt(parts[4]);
                int p2 = Integer.parseInt(parts[5]);
                return p1 * 256 + p2;
            }
        }
        return -1;
    }
    
    public static void main(String[] args) 
    {
        if (args.length < 6) {
            System.out.println("Usage: java HtmlFileMonitor <server> <username> <password> " +
                            "<remote_dir> <local_file> <remote_file>");
            System.out.println("Example: java HtmlFileMonitor 192.168.1.100 myuser mypass " +
                            "/var/www/html C:\\path\\to\\file.html index.html");
            return;
        }
        
        HtmlFileMonitor monitor = new HtmlFileMonitor(
            args[0], args[1], args[2], args[3], args[4], args[5]);
        
        File file = new File(args[4]);
        if (!file.exists()) 
        {
            System.err.println("Error: Local file does not exist!");
            return;
        }

        
        
        monitor.lastModified = file.lastModified();
        monitor.startMonitoring();
    }
}
