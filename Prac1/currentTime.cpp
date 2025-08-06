

#include <iostream>
#include <fstream>
#include <ctime>
#include <sstream>
using namespace std;

int main()
{
    ifstream timeFile("back-end.txt");
    string input="";
    getline(timeFile,input);
    if (input=="")
    {
        return 0;
    }
    timeFile.close();


    time_t currTime = time(NULL);
    struct tm *dt = gmtime(&currTime);
    
    stringstream ss;
    ss << input;
    int num=0;
    ss >> num;
    dt->tm_hour += num; 

    string country = "South Africa";
    if (num==0)
    {
        country = "Ghana";
    }
    mktime(dt);
    char timeStr[20];
    strftime(timeStr, sizeof(timeStr), "%Y-%m-%d %H:%M:%S", dt);

    cout << "Content-type: text/html" << endl <<endl;
    cout << "<!DOCTYPE html>" << endl;
    cout <<"<html lang='en'>" << endl;
        cout <<"<head>" << endl;
            cout <<"<title>Practical 1</title>" << endl;
        cout <<"</head>" << endl;
        cout <<"<body>" << endl;
            cout <<"<h1>Current Time in " << country << " : " << timeStr << "</h1>" << endl;
            cout <<"<a href='/cgi-bin/Prac1/SouthAfricanTime.cgi'>Switch to South African Time</a>" << endl;
            cout <<"<a href='/cgi-bin/Prac1/GhanaTime.cgi'>Switch to Ghana Time</a>" << endl;
        cout <<"</body>" << endl;
    cout <<"</html>" << endl;
   
    return 0;
}