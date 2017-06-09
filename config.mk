##############################
#    Environment variables   #
##############################

JAVA  = java
JAVAC = javac
#JAVAC = /usr/bin/jikes
JAVACOPTS =
# +E -deprecation
JAVACC = $(JAVAC) $(JAVACOPTS)
RMIC = rmic
RMIREGISTRY= rmiregistry
CLASSPATH = .:$(J2EE_HOME)/lib/j2ee.jar:$(JAVA_HOME)/jre/lib/rt.jar:/cluster/opt/jakarta-tomcat-3.2.3/lib/servlet.jar:$(PWD)
JAVADOC = javadoc
JAR = jar

GENIC = ${JONAS_ROOT}/bin/unix/GenIC

MAKE = gmake
CP = cp
RM = rm
MKDIR = mkdir


# EJB server: supported values are jonas or jboss
EJB_SERVER = jonas

# DB server: supported values are MySQL or PostgreSQL
DB_SERVER = MySQL

%.class: %.java
	${JAVACC} -classpath ${CLASSPATH} $<

