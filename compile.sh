#!/bin/bash

# Define variables
PROJECT_DIR=$(pwd)
MAIN_CLASS=spritegenerator.MainApp
JAR_NAME=mcspritegen.jar
MANIFEST_FILE=Manifest.txt

# Step 1: Prepare output directory and copy non-java/class files
echo "Preparing output directory..."
mkdir -p $PROJECT_DIR/out

# Copy non-java/class files to out directory
rsync -a --exclude '*.java' --exclude '*.class' --exclude '*.jar' $PROJECT_DIR/src/ $PROJECT_DIR/out/

# Step 2: Compile Java source code
echo "Compiling Java source code..."
javac -cp $PROJECT_DIR/src -d $PROJECT_DIR/out $(find $PROJECT_DIR/src -name '*.java')

# Step 3: Create Manifest file
echo "Creating Manifest file..."
echo "Main-Class: $MAIN_CLASS" > $PROJECT_DIR/$MANIFEST_FILE
echo "" >> $PROJECT_DIR/$MANIFEST_FILE

# Step 4: Create JAR file
echo "Creating JAR file..."
jar cfm $PROJECT_DIR/$JAR_NAME $PROJECT_DIR/$MANIFEST_FILE -C $PROJECT_DIR/out .

echo "JAR file created: $PROJECT_DIR/$JAR_NAME"

# Step 5: Clean up .class files
echo "Cleaning up .class files..."
find $PROJECT_DIR/out -name "*.class" -delete

# Step 6: Run the JAR file
echo "Running the application..."
java -jar $PROJECT_DIR/$JAR_NAME

echo "Script execution completed."

