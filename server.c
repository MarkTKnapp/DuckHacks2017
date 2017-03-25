/* A simple server in the internet domain using TCP
   The port number is passed as an argument */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h> 
#include <sys/socket.h>
#include <netinet/in.h>
#include "my.h"
#include "list.h"

struct client {
  char* user;
  int sockfd;
};

void error ( char *msg )
{
    my_str ( msg);
    exit(1);
}

/* Checks if a starts with b */
int startswith ( char* a, char* b ) {
  int i = 0;
  if ( a == NULL || b == NULL ) return 0;
  while ( a[i] != '\0' && b[i] != '\0' ) {
    if ( a[i] != b[i] ) return 0;
    i++;
  }
  if ( a[i] == '\0' && b[i] != '\0' ) return 0;
  return 1;
}

int main(int argc, char *argv[])
{
  int sockfd, portno, i=0, new, j=0, max;
  socklen_t size;
  char buffer[1025];
  char* message;
  struct sockaddr_in serv_addr, cli_addr;
  struct s_node* head = NULL;
  struct client* cl = (struct client*) malloc  ( sizeof ( struct client  ) );
  struct client* cl2;
  fd_set readfds, activefds;

  /* Ensure user provided port */
  if (argc < 2) {
    error ( "ERROR, no port provided\n" );
  }

  /* Open Socket */
  if ( ( sockfd = socket ( AF_INET, SOCK_STREAM, 0 ) ) < 0) 
    error ( "ERROR opening socket" );

  memset ( (char *) &serv_addr,0, sizeof ( serv_addr ) ); /* Zero server address */
  portno = my_atoi ( argv[1] ); /* Retrieve port number */

  /* Specify type of socket */
  serv_addr.sin_family = AF_INET;
  serv_addr.sin_addr.s_addr = INADDR_ANY;
  serv_addr.sin_port = htons ( portno );

  /* Binds the socket to port provided by user */
  if ( bind ( sockfd, (struct sockaddr *) &serv_addr, sizeof (serv_addr) ) < 0) 
    error("ERROR on binding");

  listen(sockfd,5);

  FD_ZERO ( &activefds );
  FD_SET ( sockfd, &activefds );
  cl->user = "";
  cl->sockfd = sockfd;
  add_elem ( (void*) cl, &head );

  /* keep looping through */
  while ( 1 ) {

    /* select all fds */
    readfds = activefds;
    if ( select ( FD_SETSIZE, &readfds, NULL, NULL, NULL ) < 0 )
      error ( "ERROR on select" );
    
    i = 0;
    max = count_s_nodes ( head );
    while ( i < max ) {
      cl = elem_at ( head, i );
      
      /* Did we get a message */
      if ( FD_ISSET ( cl->sockfd, &readfds ) ) {

	/* Is it a request for a new connection? */
	if ( cl->sockfd == sockfd ) {
	  
	  size = sizeof ( cli_addr );
	  new = accept ( sockfd, (struct sockaddr *) &cli_addr, &size ); /* accept connection */
	  if ( new < 0 )
	    error ( "ERROR on accept" );
	  FD_SET ( new, &activefds ); /* set it is there in our fd's */
	  
	  /* Read in username */
	  memset ( buffer,0, 1024 );
	  if ( read ( new, buffer, 1024 ) < 0)
	    error("ERROR reading from socket");
	  buffer[13] = 0;

	  /* create new client */
	  cl2 = (struct client*) malloc ( sizeof ( struct client ) );
	  cl2->user = (char*) malloc ( 13 );
	  my_strcpy ( cl2->user, buffer );
	  cl2->sockfd = new;
	  add_elem ( (void*) cl2, &head );
	  i++;
	  memset ( buffer, 0, 13 );

	  message = my_strconcat ( cl2->user, " has joined the conversation\n" );
	  j = 0;
	  while ( j < max ) {
	    cl2 = elem_at ( head, j );
	    if ( cl2->sockfd != sockfd && cl2->sockfd != new ) {
	      if ( write ( cl2->sockfd, message, my_strlen ( message ) ) < 0 )
		error("ERROR writing to socket");
	    }
	    j++;
	  }
	  free ( message );
	  
	} /* Or is it somebody writing something? */
	else {

	  /* The user disconnected */
	  if ( read ( cl->sockfd, buffer, 1024 ) < 0) {

	    close ( cl->sockfd );
	    FD_CLR ( cl->sockfd, &activefds );
	    remove_node_at ( &head, i );
	    cl->sockfd = 0;
	    my_strcpy ( buffer, cl->user );
	    memset ( cl->user, 0, 13 );
	    free ( cl->user );

	    /* Inform the users that this user has disconnected */
	    message = my_strconcat ( buffer, " has disconnected\n" );
	    j = 0;
	    while ( ( cl2 = elem_at ( head, j++ ) ) != NULL )
	      if ( write ( cl2->sockfd, "", my_strlen ( message ) ) < 0 )
		error("ERROR writing to socket"); 
	    free ( message );
	    i--; /* Don't skip next one */
	    
	  } else { /* WE GOT A MESSAGE BOIZ */

	    if ( my_strlen ( buffer ) == 5 && startswith ( "/exit", buffer ) ) {

	      if ( write ( cl->sockfd, "/youhavedisconnected\n", 24 ) < 0 )
		error("ERROR writing to socket");

	      close ( cl->sockfd );
	      FD_CLR ( cl->sockfd, &activefds );
	      remove_node_at ( &head, i );
	      cl->sockfd = 0;
	      my_strcpy ( buffer, cl->user );
	      memset ( cl->user, 0, 13 );
	      free ( cl->user );

	      i--; /* Make sure you don't skip the next one */
		
	      /* Inform the users that this user has disconnected */
	      message = my_strconcat ( buffer, " has disconnected\n" );
	      
	    } else if ( startswith ( buffer, "/nick " ) ) {
	      buffer[19] = 0;
	      message = my_strconcat ( my_strconcat ( cl->user, my_strconcat ( " has changed their name to ", buffer+6 ) ), "\n" );
	      my_strcpy ( cl->user, buffer + 6 );
	    } else if ( startswith ( buffer, "/me " ) ) {
	      message = my_strconcat ( my_strconcat ( cl->user, buffer+3 ), "\n" );
	    } else if ( startswith ( buffer, "/" ) ) {
	      if ( write ( cl->sockfd, "Error: Improper command\n", 26 ) < 0 )
		error("ERROR writing to socket");
	      message = NULL;
	    } else {
	      message = my_strconcat ( my_strconcat ( cl->user, my_strconcat ( ": ", buffer ) ), "\n" );
	    }

	    if ( message != NULL ) {
	      j = 0;
	      while ( j < max ) {
		cl2 = elem_at ( head, j );
		if ( cl2->sockfd != sockfd ) {
		  if ( write ( cl2->sockfd, message, my_strlen ( message ) ) < 0 )
		    error("ERROR writing to socket");
		}
		j++;
	      }
	      free ( message );
	    }

	    memset ( buffer, 0, 1024 );
	    
	  }
	  
	}
	
      }
      i++;
    }
    
  }
    
  close(sockfd);
  return 0; 
}
