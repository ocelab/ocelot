#include <sys/types.h>
#include <sys/socket.h>
//"in" per "sockaddr_in"
#include <netinet/in.h>
//"fcntl" per la funzione "fcntl"
#include <fcntl.h>
#include <stdlib.h>

int createSocket(int port);
int acceptWithTimeout(int serverSocket);
void closeSocket(int sock);
void sendData(int sock, void *data, int sizeOfData);