include ../config.mk

####################
#       Client     #
####################

ClientFiles = URLGenerator URLGeneratorEJB URLGeneratorServlets URLGeneratorPHP RUBiSProperties Stats \
	      TransitionTable ClientEmulator UserSession InitDB ../beans/TimeManagement

all_client_sources =  $(addprefix edu/rice/rubis/client/, $(addsuffix .java, $(ClientFiles)))
all_client_obj = $(addprefix edu/rice/rubis/client/, $(addsuffix .class, $(ClientFiles)))

client: $(all_client_obj)
	${JAR} cvf rubis_client.jar $(all_client_obj)

all: client

initDB:
	${JAVA} -classpath .:./database edu.rice.rubis.client.InitDB ${PARAM}

emulator:
	${JAVA} -classpath . edu.rice.rubis.client.ClientEmulator

javadoc :
	${MKDIR} -p doc
	${JAVADOC} -d ./doc -bootclasspath ${CLASSPATH} -version -author -windowtitle "RUBiS Client API" -header "<b>RUBiS (C)2001 Rice University/INRIA</b><br>"  edu.rice.rubis.client

clean:
	${RM} -Rf edu/rice/rubis/client/*.class edu/rice/rubis/beans/*.class doc/* rubis_client.jar

