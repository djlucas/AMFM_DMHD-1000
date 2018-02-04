objects = hdcontrol.o hddefs.o hdlinuxio.o hdcommands.o hdlisten.o

hdrc : hdcmd.cpp $(objects)
	g++ -lpthread -D_REENTRANT hdcmd.cpp $(objects) -Wall -o hdrc

#hdrclib.a : $(objects)
#	ar ru hdrclib.a $(objects)

hdcmd.o : hdcontrol.h hdcmd.cpp hdcontrol.o hddefs.o hdlinuxio.o hdcommands.o hdlisten.o
	g++ -c -D_REENTRANT hdcmd.cpp -Wall

hdcontrol.o : hdcontrol.cpp hddefs.o hdlinuxio.o hdcommands.o hdlisten.o
	g++ -c -fpic -D_REENTRANT hdcontrol.cpp -Wall

libhdcontrol.so : hdcontrol.o
	g++ -shared -o libhdcontrol.so hdcontrol.o

hdcommands.o : hdcommands.cpp
	g++ -c hdcommands.cpp -Wall

hdlisten.o : hdlisten.cpp
	g++ -c -D_REENTRANT hdlisten.cpp -Wall

hdlinuxio.o : hdlinuxio.cpp hddefs.o
	g++ -c hdlinuxio.cpp -Wall

hddefs.o : hddefs.cpp
	g++ -c hddefs.cpp -Wall

.PHONY : clean
clean:
	rm hdrc $(objects) libhdcontrol.so
