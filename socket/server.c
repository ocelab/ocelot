/* 
 * File:   main.c
 * Author: daniel
 *
 * Created on 25 gennaio 2019, 18.05
 */
#include <stdio.h>
#include <string.h> //strlen
#include <sys/socket.h>
#include <arpa/inet.h> //inet_addr
#include <unistd.h> //write
#include <sys/time.h>
#include <time.h>

#include "ServerSocket.h"
#include "function.h"

#define PORT 1745
#define SERVER_STOP_TIME 10

/*
 * 
 */
int main(int argc, char** argv) {
    struct sockaddr_in client;
    int client_sock, read_size;
    int server_sock;
    int client_sock_size = sizeof (struct sockaddr_in);

    int startingServerTimestamp = (int)time(NULL);
    int actualServerTimestamp = (int)time(NULL);

    //Create server socket
    server_sock = createSocket(PORT);
    
    int i = 0;
    
    //puts("Waiting for incoming connections...");
    
    while (actualServerTimestamp - startingServerTimestamp < SERVER_STOP_TIME) {
        //Check if there is a client
        //client_sock = accept(server_sock, (struct sockaddr *) &client, (socklen_t*) &client_sock_size);
        client_sock = acceptWithTimeout(server_sock);
        if (client_sock >= 0) {
            i++;
            //Read info from client
            Graph graph;
            read_size = recv(client_sock, &graph.sizeNodes, sizeof(int), 0);
            read_size = recv(client_sock, &graph.sizeEdges, sizeof(int), 0);

            graph.nodes = malloc(sizeof(Node) * graph.sizeNodes);
            read_size = recv(client_sock, graph.nodes, (sizeof(Node) * graph.sizeNodes), 0);

            graph.edges = malloc(sizeof(Edge) * graph.sizeEdges);
            read_size = recv(client_sock, graph.edges, (sizeof(Edge) * graph.sizeEdges), 0);


            //Get parameters
            FunctionParameters functionParameters = extractParametersFromGraph(graph);


            //Execute function
            int eventSize = 0;
            Event *eventList = executeFunction(functionParameters, &eventSize);

            //Crash simulation
            if (i == 35666) {
                abort();
            }

            //Send events to client
            write(client_sock, &eventSize, sizeof(int));
            write(client_sock, eventList, (sizeof(Event) * eventSize));


            //Free memory
            free(eventList);
            free(functionParameters.d);
            free(functionParameters.str);
            free(graph.nodes);
            free(graph.edges);

            //Reset time
            startingServerTimestamp = (int)time(NULL);
        }

        closeSocket(client_sock);

        actualServerTimestamp = (int)time(NULL);
    }

    closeSocket(server_sock);

    return (EXIT_SUCCESS);
}
