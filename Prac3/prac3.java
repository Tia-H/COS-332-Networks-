// u23576996 - Shinn-Ru Hung
// u23562732 - Yuthika Tia Harripersad

import java.io.*;
import java.net.*;

public class prac3 {

    // Calculator state
    private static String equation = "";
    private static String finalEquation = "";
    private static String operator = "";
    private static double result = 0;
    private static double num2 = 0;
    private static double finalResult = 0;
    private static String html="";
    private static boolean calc=false;

    public static void main(String[] args) throws IOException {
        int userPort = 9002;
        try (ServerSocket calculatorServer = new ServerSocket(userPort))
        {
            System.out.println("Server alive :). Open http://127.0.0.1:9002 in your browser.");
            while (true)
            {
                //accept connection to server
                Socket client = calculatorServer.accept(); 
                //allow user input to be read
                InputStreamReader i = new InputStreamReader(client.getInputStream());
                BufferedReader userInput = new BufferedReader(i);
                OutputStream display = client.getOutputStream();
                
                //Read HTTP request
                String input = userInput.readLine();
                if (input!=null)
                {
                    if (input.startsWith("GET"))
                    {
                        String reqString = input.substring(4);
                        int space = reqString.indexOf(" ");
                        String file = reqString.substring(0,space);
                        if (file.startsWith("/d"))
                        {
                            String num = file.substring(2);
                            equation += num;
                            finalEquation += num;
                            //get first value
                            if (!finalEquation.contains("+") && !finalEquation.contains("-") && !finalEquation.contains("*") && !finalEquation.contains("/"))
                            {
                                result =  equation.isEmpty() ? 0 : Double.parseDouble(equation);
                            }else{
                                int index = equation.indexOf(operator);
                                    if (index+1< equation.length() && index>0)
                                    {
                                        String numb = equation.substring(index+1);
                                        num2 = Double.parseDouble(numb);
                                    }
                            }
                        }else if (file.startsWith("/op"))
                        {
                            if (!equation.isEmpty()) 
                            {
                                if (calc==true)
                                {
                                    calculate(); // Compute final result 
                                }else{
                                    calc=true;
                                }
                                operator = file.substring(3);
                                finalEquation += operator;
                                equation = String.valueOf(result);
                                equation += operator; 
                                if (operator=="%")
                                {
                                    result /= 100;
                                }
                            }
                            
                        }else if (file.equals("/="))
                        {
                            calculate();
                            finalResult = result;
                            finalEquation="";
                            equation="";
                            operator="";
                            result=-0;
                            calc=false;
                        }

                    }
                }
                generateHTML();
                String h ="HTTP/1.1 200 OK \r\n" +
                "Content-Type: text/html \r\n" + 
                "Content-length:"+ html.length()+"\r\n"+"\r\n"+
                "<!DOCTYPE html> \n" +
                "<html lang=\"en\"> \n" + html;
                display.write(h.getBytes());
                html="";
            }
        }catch (IOException e)
        {
            System.out.println("Server did not start :(");
        }
    }

    private static void calculate()
    {
        switch (operator) 
        {
                case "+":
                    result += num2;break;
                case "-":
                    result -= num2;break;
                case "*":
                    result *= num2;break;
                case "/":
                    result /= num2;break;
                case "%":
                    result /= 100;break;
                default:
                    result= num2; // No operator set, return the number itself
        }
    }

    private static void generateHTML() 
    {
        html = 
                "<html><body>" +
            "<h1>Calculator</h1>"+
            "<p>Result: " + finalResult + "</p>"+
            "<p>Equation: "+finalEquation+"</p>"+
            "<p>";
        // Add digit buttons (0-9)
        for (int i = 0; i < 10; i++) {
            html += "<a href='/d" + i + "'>" + i + "</a> ";
        }

        // Add operator buttons (+, -, *, /, =)
        html += "<br>";
        html += "<a href='/='>=</a>";
        html += "&nbsp;";html += "&nbsp;";
        for (String op : new String[]{"+", "-", "*", "/"}) {
            html += "<a href='/op"+op+"'>"+op+"</a> ";
            html += "&nbsp;";
        }
        html += "<br>";
        html += "&nbsp;"; html += "&nbsp;"; html += "&nbsp;";html += "&nbsp;";
        for (String op : new String[]{"%","x"}) {
            html += "<a href='/op"+op+"'>"+op+"</a> ";
            html += "&nbsp;";
        }
        html += "</p></body></html>";
    }
}