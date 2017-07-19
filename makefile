all: Server.class Client.class
Server.class:Server.java
	javac -classpath . Server.java
Client.class:Client.java
	javac -classpath . Client.java
clean:
	rm -f *.class