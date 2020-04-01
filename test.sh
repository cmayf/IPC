javac edu/cs300/*java
javac CtCILibrary/*.java
javac -h . edu/cs300/MessageJNI.java
#export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
export JAVA_HOME=/usr/java/latest
gcc -c -fPIC -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux system5_msg.c -o edu_cs300_MessageJNI.o
gcc -shared -o libsystem5msg.so edu_cs300_MessageJNI.o -lc
gcc -std=c99 -D_GNU_SOURCE msgsnd_pr.c -o msgsnd
gcc -std=c99 -D_GNU_SOURCE msgrcv_lwr.c -o msgrcv

#./msgsnd con
#java -cp . -Djava.library.path=. edu.cs300.MessageJNI
#./msgrcv
#java edu.cs300.ParallelTextSearch con

echo "START"
echo "compiling searchmanager"
gcc -std=c99 -D_GNU_SOURCE searchmanager.c -o searchmanager
echo "running searchmanager"
./searchmanager 3 con pre wor & java -cp . -Djava.library.path=. edu.cs300.PassageProcessor

#./searchmanager 3 con pre wor

#Test Commands
#gcc -std=c99 -Wall -ggdb3 -D_GNU_SOURCE searchmanager.c -o searchmanager
#valgrind --show-leak-kinds=all --track-origins=yes --verbose ./searchmanager 3 con pre wor
