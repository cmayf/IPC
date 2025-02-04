/* Search Manager requirements
 * Must be written in C
 * Numeric parameter denoting delay will be present and an integer;
 * if it is zero, then use no delay
 * At least one prefix will be provided (may not be valid)
 * Only process prefixes are at least 3 characters should be processed
 * Only one prefix should be processed at a time. Once all the results
 * on a prefix returned, the next can be sent to the passage processor
 * Send a prefix message with a zero id to notify the passage processor to complete */

#include <sys/types.h>
#include <sys/ipc.h>
#include <sys/msg.h>
#include <sys/wait.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <errno.h>
#include <signal.h>
#include <pthread.h>
#include "longest_word_search.h"
#include "queue_ids.h"
 
#ifndef mac
size_t  			// O - Length of string 
strlcpy(char *dst, 		// O - Destination string
	const char *src, 	// I - Source string
	size_t size) 		// I - Size of destination string buffer
{
	size_t    srclen;       //Length of source string

	//Figure out how much room is needed...
	size --;
	srclen = strlen(src);

	//Copy the appropriate amount...
	if (srclen > size)                                                                     
		srclen = size;

	memcpy(dst, src, srclen);
	dst[srclen] = '\0';
	return (srclen);                                                               
}
#endif 

//Global vars for SIGINT capture
char* pref[1000];
int prefStatus[1000] = {0};
int requestCount;
int passageCount;

// Compare function for qsorting response buffers	
int cmp(const void *a, const void *b) {
	response_buf *A = (response_buf *)a;
	response_buf *B = (response_buf *)b;
	return (A->index - B->index);
}

// SIGINT handler: prints status of requests
void sig_handler(int signo) {
	if (signo == SIGINT) {
		printf("INTERRUPT\n");
		for (int i = 0; i < requestCount; i++) {
			printf("%s - %d of %d\n", pref[i], prefStatus[i], passageCount);
		}
		exit(0);
	}
}

int main(int argc, char* argv[]) {
	if (signal(SIGINT, sig_handler) == SIG_ERR) {
		printf("\ncan't catch SIGINT\n");
	}

	/* Read secs between sending prefix request from cmd line */
	int secsBetweenRequests = atoi(argv[1]);
	requestCount = argc - 2;
	passageCount = 0;

	int msqid;
	int msgflg = IPC_CREAT | 0666;
	key_t key = ftok(CRIMSON_ID,QUEUE_NUMBER);
	if ((msqid = msgget(key, msgflg)) < 0) {
			int errnum = errno;
			fprintf(stderr, "Value of errno: %d\n", errno);
			perror("(msgget)");
			fprintf(stderr, "Error msgget: %s\n", strerror( errnum ));
	}
	else fprintf(stderr, "msgget: msgget succeeded: msqid = %d\n", msqid);
	
	prefix_buf sbuf = {};
	response_buf rbuf = {};
	size_t sbuf_length;
	size_t rbuf_length;

	/* Read prefixes from cmd line */
	prefix_buf sbufs[requestCount];
	//for (int i = 0; i < requestCount; i++) sbufs[i] = sbuf;
	for (int i = 0; i < requestCount; i++) {
		//printf("processing prefixes...\n");
		sbuf.mtype = 1;
		strlcpy(sbuf.prefix, argv[i+2], WORD_LENGTH);
		sbuf.id = i + 1;
		sbuf_length = strlen(sbuf.prefix) + sizeof(int)+1;
		sbufs[i] = sbuf;
		pref[i] = strdup(sbufs[i].prefix);
	}

	/* Send prefix request messages (prefix string & prefix ID)
	 * via System V ipc queues */
	int r = 0;
	while ( r < requestCount) {
		int prefixReportDone = 0;
		//printf("\nSEARCH MANAGER: msgsnd\n");
		if((msgsnd(msqid, &sbufs[r], sbuf_length, IPC_NOWAIT)) < 0) {
			int errnum = errno;
			fprintf(stderr,"%d, %ld, %s, %d\n", msqid, sbufs[r].mtype, sbufs[r].prefix, (int)sbuf_length);
			perror("(msgsnd)");
			fprintf(stderr, "Error sending msg: %s\n", strerror( errnum ));
			exit(1);
		}
		else {
			fprintf(stderr,"Message(%d): \"%s\" Sent (%d bytes)\n", sbufs[r].id, sbufs[r].prefix,(int)sbuf_length);
		}

		/* Wait for passage processor to return series of responses */
		// Get initial response of passage count
		response_buf init = rbuf;
		do {
		int valid_init = msgrcv(msqid, &init, sizeof(response_buf), 2, 0);
		int errnum = errno;
		if (valid_init < 0 && errno != EINTR) {
			fprintf(stderr, "Value of errno: %d\n", errno);
			perror("Error printed by perror");
			fprintf(stderr, "Error receiving msg: %s\n", strerror( errnum ));
		}
		}
		while (init.count == rbuf.count && r == 0); 
		passageCount = init.count;
		prefStatus[r] += 1;

		// Get responses for each passage
		response_buf rbufs[passageCount];
		rbufs[0] = init;
		for (int p = 1; p < passageCount; p++) {
			int ret;
			do {	
				ret = msgrcv(msqid, &rbufs[p], sizeof(response_buf), 2, 0); //receive type 2 message
				//printf("\nSEARCH MANAGER: msgrcv\n");
				int errnum = errno;
				if (ret < 0 && errno !=EINTR){
					fprintf(stderr, "Value of errno: %d\n", errno);
					perror("Error printed by perror");
					fprintf(stderr, "Error receiving msg: %s\n", strerror( errnum ));
				}
			} while ((ret < 0 ) && (errno != 4));
			prefStatus[r] += 1;
		}


		/* Print prefix for each response */
		do {
			sleep(1);
			qsort(rbufs, sizeof(rbufs)/sizeof(*rbufs), sizeof(*rbufs), cmp);
			printf("\nReport %s\n", sbufs[r].prefix);
			for (int p = 0; p < passageCount; p++) {
				if (rbufs[p].present == 0)
					printf("Passage %d - %s - not found\n", p, rbufs[p].location_description);
				else
					printf("Passage %d - %s - %s\n", p, rbufs[p].location_description, rbufs[p].longest_word);
			}
			printf("\n");
			prefixReportDone = 1;
		} while (prefixReportDone = 0);
		r++;
		sleep(secsBetweenRequests);
	}

	/* Send message to passage processor letting
 	* it know that all request have been sent */
	prefix_buf done = sbuf;
	strncpy(done.prefix, "   ", WORD_LENGTH);
	done.id = 0;
	if((msgsnd(msqid, &done, sbuf_length, IPC_NOWAIT)) < 0) {
		int errnum = errno;
		fprintf(stderr,"%d, %ld, %s, %d\n", msqid, done.mtype, done.prefix, (int)sbuf_length);
		perror("(msgsnd)");
		fprintf(stderr, "Error sending msg: %s\n", strerror( errnum ));
		exit(1);
	}
	else {
		fprintf(stderr,"Message(%d): \"%s\" Sent (%d bytes)\n", done.id, done.prefix,(int)sbuf_length);
	}

	/* When all responses are recieved search manager terminates. */
	printf("Exiting ... ");
	exit(0);

}

