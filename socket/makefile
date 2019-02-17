CC = gcc

SRCS = Event.c function.c graph.c main.c ocelot.c server.c ServerSocket.c

SERVER_MAIN = ../server

.PHONY: clean all
all:
	$(CC) -o $(SERVER_MAIN) $(SRCS)
clean:
	$(RM) $(SERVER_MAIN)
