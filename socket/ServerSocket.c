#include <netdb.h>
#include <stdio.h>

#include "ServerSocket.h"

#define TIMEOUT 1

int createSocket(int port) {
    int socket_desc, error;
    struct sockaddr_in servaddr;

    //Create socket
    socket_desc = socket(AF_INET, SOCK_STREAM, 0);
    if (socket_desc == -1) {
        printf("Could not create socket");
    }
    //puts("Socket created");

    bzero(&servaddr, sizeof (servaddr));
    //Prepare the sockaddr_in structure
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = htonl(INADDR_ANY);
    servaddr.sin_port = htons(port);

    //Bind
    error = bind(socket_desc, (struct sockaddr *) &servaddr, sizeof (servaddr));
    if (bind < 0) {
        //print the error message
        perror("bind failed. Error");
        return 1;
    }
    //puts("Bind done\n");

    //Listen
    error = listen(socket_desc, 3);
    if (error < 0) {
        perror("listening failed. Error");
        return 1;
    }

    return socket_desc;
}

int acceptWithTimeout(int serverSocket) {
    int iResult;
    struct timeval tv;
    fd_set rfds;
    FD_ZERO(&rfds);
    FD_SET(serverSocket, &rfds);

    tv.tv_sec = (long) TIMEOUT;
    tv.tv_usec = 0;

    iResult = select(serverSocket + 1, &rfds, (fd_set *) 0, (fd_set *) 0, &tv);
    if (iResult > 0) {
        return accept(serverSocket, NULL, NULL);
    }
    
    return -1;

}

void closeSocket(int sock) {
    close(sock);
    return;
}

void sendData(int sock, void *data, int sizeOfData) {
    printf("\nClient: send data\n");

    //write(socket, messaggio, lunghezza messaggio)
    if (send(sock, data, sizeOfData, 0) < 0) {
        printf("Impossible to send data\n");
        closeSocket(sock);
        exit(1);
    }
    printf("Sending data successfully\n");
    return;
}