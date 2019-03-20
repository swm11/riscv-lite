# Generate all debugging info
J_DEBUGGING_FLAG = -g
# Flag for class path
J_CLASS_FLAG = -d
# Specify the path where to place .class files
J_CLASS_PATH = ./bin
# Specify where to place generated class files
J_CLASS = $(J_CLASS_FLAG) $(J_CLASS_PATH)

# Flag for source path
J_SOURCE_FLAG = -sourcepath
# Specify the path where are the source files .java
J_SOURCE_PATH = ./src
# Specify where are the source files .java
J_SOURCE = $(J_SOURCE_FLAG) $(J_SOURCE_PATH)
# Top class
J_TOP = jsim

# Identify source files
SRC = $(wildcard $(J_SOURCE_PATH)/*.java)

# Création des fichiers .class correspondant
# TMP = $(SRC:.java=.class)
# OBJ = $(TMP:$(J_SOURCE_PATH)=$(J_CLASS_PATH))
OBJ = $(patsubst $(J_SOURCE_PATH)/%.java,$(J_CLASS_PATH)/%.class,$(SRC))
JC = javac

JAVAC_COMPILE = $(JC) $(J_SOURCE) $(J_CLASS)

default: bin $(OBJ)

run: bin $(OBJ)
	java --class-path $(J_CLASS_PATH) $(J_TOP)

$(J_CLASS_PATH)/%.class: %.java
	$(JAVAC_COMPILE) $*.java

bin: 
	@mkdir -p $(J_CLASS_PATH)

clean:
	rm -Rfv $(J_CLASS_PATH)/*

fullclean : clean
	rmdir $(J_CLASS_PATH)

