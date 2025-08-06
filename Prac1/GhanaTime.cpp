
#include <iostream>
#include <fstream>
#include <ctime>
#include <sstream>
using namespace std;

int main()
{
    ofstream newTime("/var/www/cgi-bin/Prac1/back-end.txt");
    if (newTime)
    {
        newTime << "0";
        newTime.close();
    }
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

    mktime(dt);
    char timeStr[20];
    strftime(timeStr, sizeof(timeStr), "%Y-%m-%d %H:%M:%S", dt);

    cout << "Content-type: text/html" << endl <<endl;
    cout << "<!DOCTYPE html>" << endl;
    cout <<"<html lang='en'>" << endl;
        cout <<"<head>" << endl;
            cout <<"<title>Swicthed to Ghana Time</title>" << endl;
        cout <<"</head>" << endl;
        cout <<"<body>" << endl;
            cout <<"<h1>Ghana Time:</h1>" << timeStr << endl <<endl;
            cout <<"<a href='/cgi-bin/Prac1/currentTime.cgi'>View Time</a>" << endl;
        cout <<"</body>" << endl;
    cout <<"</html>" << endl;
   
    return 0;
}