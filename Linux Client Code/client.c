#include <stdlib.h>
#include <string.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <netdb.h>
#include <stdio.h>

int main(int argc, char **argv){
  unsigned short port;
  char buf[256];
  struct hostent *hostnm;
  struct sockaddr_in server;
  int s;

  if (argc != 3){
    fprintf(stderr, "Usage: %s hostname port \n", argv[0]);
    exit(1);
  }

  hostnm = gethostbyname(argv[1]);

  if (hostnm == (struct hostent *) 0){
    fprintf(stderr, "Gethostbyname failed\n");
    exit(2);
  }

  port = (unsigned short) atoi(argv[2]);

  strcpy(buf, "Hello there");
  server.sin_family = AF_INET;
  server.sin_port = htons(port);
  server.sin_addr.s_addr = *((unsigned long *) hostnm->h_addr);

  if ((s = socket(AF_INET, SOCK_STREAM, 0)) < 0){
    perror("Socket()");
    exit(3);
  }

  if (connect(s, (struct sockaddr *)&server, sizeof(server)) < 0){
    perror("Connect()");
    exit(4);
  }

  if (send(s, buf, sizeof(buf), 0) < 0){
    perror("Send()");
    exit(5);
  }

  return 0;
}
