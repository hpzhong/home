JAVA_HOME = /usr/lib/jvm/java-7-openjdk-amd64
CPPFLAGS += -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux/ -I$(JAVA_HOME)/include/jre/lib/amd64/server/
CPPFLAGS += -L$(JAVA_HOME)/jre/lib/amd64/server/ -ljvm


all: TestJNI java
%:%.cpp
	g++ $(CPPFLAGS) $^ -o $@

java:
	@echo "buid">/dev/null
	$(shell cd sample; javac *.java)

clean:
	@rm sample/*.class -fr
	@rm TestJNI -fr
