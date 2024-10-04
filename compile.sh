#!/bin/bash

# Define variables
PROJECT_DIR=$(pwd)
MAIN_CLASS=spritegenerator.MainApp
JAR_NAME=mcspritegen.jar
MANIFEST_FILE=Manifest.txt

command_exists() {
    command -v "$1" >/dev/null 2>&1
}

if ! command_exists javac; then
    echo "Error: 'javac' command not found. Make sure JDK is installed and in your PATH."
    exit 1
fi

# Check for rsync
if ! command_exists rsync; then
    echo "Error: 'rsync' command not found. Please install rsync to proceed."
    exit 1
fi


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

echo "Script execution completed."

