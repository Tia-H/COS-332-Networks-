// u23576996 - Shinn-Ru Hung
// u23562732 - Yuthika Tia Harripersad

import java.io.*;
import java.net.*;

public class prac5 {

    static OutputStream terminal;
    static InputStream input;
    static String password = "ATserver2025";
    public static void main(String[] args) throws IOException {

        InputStreamReader i = new InputStreamReader(System.in);
        BufferedReader userInput = new BufferedReader(i);

        try (Socket s = new Socket("localhost",389))
        {
            while (true)
            {
                System.out.print("Friend's name: ");
                String friendName = userInput.readLine();
                if (friendName.equals("bye"))
                {
                    System.out.println("      GOODBYE!");
                    s.close();
                    break;
                }
            terminal = s.getOutputStream();
            //PrintWriter terminal = new PrintWriter(response,true);
            input = ( s.getInputStream());
            //BufferedReader input = new BufferedReader(new InputStreamReader(text));

            connectLDAP();

            performQuery(friendName);

            boolean found=false;
            byte[] packet = new byte[1024];
            int packetLength = input.read(packet);
            if (packetLength<=0)
            {
                System.out.println("Empty response packet returned");
            }else{
                int index=7;
                if (packet[index]==0x04)
                {
                    index++;
                    int dnLength = packet[index++] & 0xFF;
                    index += dnLength+4;
                    if (packet[index] == 0x04) 
                    {
                        index++;
                        int fieldLength = packet[index++]&0xFF;
                        String field = new String(packet,index,fieldLength);
                        index+=fieldLength;
                        if (field.equals("telephoneNumber")) 
                        {
                            index+=2;
                            if (packet[index] == 0x04) 
                            {
                                index++;
                                int numLength = packet[index++] & 0xFF;
                                String telNum = new String(packet, index, numLength);
                                System.out.println("Friend found: " + telNum);
                                found=true;
                            }
                        }
                    }
                }
            }
            if (found==false)
            {
                System.out.println("Friend not found");
            }
           
            /*terminal.println("BIND cn=admin,dc=nodomain");
            terminal.println("PASSWORD "+password);
            terminal.println("END");

            s.setSoTimeout(5000);


            terminal.println("SEARCH ou=Friends,dc=nodomain");
            terminal.println("FILTER (cn="+friendName+")");
            terminal.println("ATTRIBUTES telephoneNumber");
            terminal.println("END");

            String findNum;
            String phoneNum="";
            while ((findNum=input.readLine())!=null)
            {
                System.out.println(findNum);
                if (!(findNum.equals("END")))
                {
                    if (findNum.startsWith("telephoneNumber:"))
                    {
                        phoneNum = findNum;
                    }
                }
            }*/
        }

        }
    }

    private static void connectLDAP()
    {
            ByteArrayOutputStream data1 = new ByteArrayOutputStream();

            data1.write(0x02); 
            data1.write(0x01);
            data1.write(0x01);

            ByteArrayOutputStream data2 = new ByteArrayOutputStream();
            data2.write(0x02); //version
            data2.write(0x01);
            data2.write(0x03);

            data2.write(0x04); //dn "cn=admin,dc=nodomain"
            String dn = "cn=admin,dc=nodomain";
            try{
                data2.write(dn.getBytes().length);
                data2.write(dn.getBytes());
            }catch (IOException e)
            {
                System.out.println("error reading dn bytes");
            }

            data2.write(0x80); //PASSWORD
            try{
                data2.write(password.getBytes().length);
                data2.write(password.getBytes());
            }catch (IOException e)
            {
                System.out.println("error reading password bytes");
            }

            byte[] connectLDAP = data2.toByteArray();
            data1.write(0x60);
            data1.write(connectLDAP.length);
            try{
                data1.write(connectLDAP);
            }catch (IOException e)
            {

            }

            ByteArrayOutputStream packet = new ByteArrayOutputStream();
            byte[] d1 = data1.toByteArray();
            packet.write(0x30);
            packet.write(d1.length);
            try{
                packet.write(d1);
            }catch (IOException e)
            {

            }

        try {
            terminal.write(packet.toByteArray());
            terminal.flush();
        }catch (IOException e)
        {
            System.out.println("connection failed.");
        }
    }

    private static void performQuery(String name)
    {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
            data.write(0x02); 
            data.write(0x01);
            data.write(0x02);

            ByteArrayOutputStream data2 =  new ByteArrayOutputStream();
            data2.write(0x04); //dn "cn=admin,dc=nodomain"
            String dn = "ou=Friends,dc=nodomain";
            data2.write(dn.getBytes().length);
            try{
                data2.write(dn.getBytes());
            }catch (IOException e)
            {
                System.out.println("error reading dn bytes");
            }

            data2.write(0x0A); 
            data2.write(0x01);data2.write(0x02);

            data2.write(0x0A);
            data2.write(0x01);data2.write(0x00);

            data2.write(0x02);
            data2.write(0x01);data2.write(0x00);

            data2.write(0x02);
            data2.write(0x01);data2.write(0x00);

            data2.write(0x01);
            data2.write(0x01);data2.write(0x00);

            ByteArrayOutputStream data3 =  new ByteArrayOutputStream();
            data2.write(0xA3);
            //data3.write(4+name.getBytes().length + "cn".getBytes().length);
            data3.write(0x04);
            //data3.write(0x04);
            try{
                data3.write("cn".getBytes().length);
                data3.write("cn".getBytes());
            }catch (IOException e)
            {

            }

            data3.write(0x04);
            data3.write(name.getBytes().length);
            try{
                data3.write(name.getBytes());
            }catch (IOException e)
            {
                System.out.println("error reading name bytes");
            }
            
            byte[] data3B = data3.toByteArray();
            data2.write(data3B.length);
            try{
                data2.write(data3B);
            }catch (IOException e)
            {}

            data2.write(0x30);
            String field = "telephoneNumber";
            data2.write(2+ field.getBytes().length);
            data2.write(0x04);
            data2.write(field.getBytes().length);
            try{
                data2.write(field.getBytes());
            }catch (IOException e)
            {
                System.out.println("error reading name bytes");
            }

            byte[] query = data2.toByteArray();
            data.write(0x63);
            data.write(query.length);
            try{
                data.write(query);
            }catch (IOException e)
            {}

            byte[] finQuery = data.toByteArray();
            ByteArrayOutputStream fin=  new ByteArrayOutputStream();
            fin.write(0x30);
            fin.write(finQuery.length);
            try{
                fin.write(finQuery);
            }catch (IOException e)
            {}
            
            try{
                terminal.write(fin.toByteArray());
                terminal.flush();
            }catch(IOException e)
            {
                System.out.println("connection failed.");
            }
            byte[] idk = new byte[4096];
            try{
            int numSomething = input.read(idk);
            //System.out.println("Raw response (" + len + " bytes):");
            for (int j = 0; j < numSomething; j++) {
                //System.out.printf("%02X ", response[j]);
                //if ((j+1) % 16 == 0) System.out.println();
            }
        }catch (IOException e)
        {

        }
    }
}