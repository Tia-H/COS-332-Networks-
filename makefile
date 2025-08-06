all:
	make clean && make main && java prac5 ${task}

main:
	javac *.java

clean:
	rm -f *.class