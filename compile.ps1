# compile.ps1

function Get-ScriptDirectory {
    $Invocation = (Get-Variable MyInvocation -Scope 1).Value
    return Split-Path $Invocation.MyCommand.Path
}

# Define the source and destination directories relative to the script's location
$scriptDir = Get-ScriptDirectory
$sourceDir = Join-Path -Path $scriptDir -ChildPath "src"
$destinationDir = Join-Path -Path $scriptDir -ChildPath "output"
$jarFilePath = Join-Path -Path $scriptDir -ChildPath "mcspritegen.jar"

$mainClass = "spritegenerator.MainApp"  # Replace with your main class package and name

# Prepare output directory
Write-Output "Preparing output directory..."
if (Test-Path $destinationDir) {
    Remove-Item -Path $destinationDir -Recurse -Force
}
New-Item -Path $destinationDir -ItemType Directory

# Function to copy items safely
function Copy-ItemSafely {
    param (
        [string]$source,
        [string]$destination
    )

    if ($source -ne $destination) {
        Copy-Item -Path $source -Destination $destination -Force -Recurse
    }
}

# Copy files and directories from source to destination, excluding .java files
Get-ChildItem -Path $sourceDir -Recurse | Where-Object { $_.Extension -ne ".java" } | ForEach-Object {
    $relativePath = $_.FullName.Substring($sourceDir.Length + 1)
    $destinationPath = Join-Path -Path $destinationDir -ChildPath $relativePath

    if (Test-Path -Path $_.FullName -PathType Container) {
        # Ensure the destination directory exists
        if (-not (Test-Path -Path $destinationPath)) {
            New-Item -Path $destinationPath -ItemType Directory
        }
    } else {
        Copy-ItemSafely -source $_.FullName -destination $destinationPath
    }
}

# Compile Java source code
Write-Output "Compiling Java source code..."
javac -d $destinationDir -sourcepath $sourceDir (Get-ChildItem -Path $sourceDir -Recurse -Include *.java).FullName

# Create MANIFEST.MF file
$manifestContent = "Main-Class: $mainClass`n"
$manifestFilePath = Join-Path -Path $destinationDir -ChildPath "MANIFEST.MF"
Set-Content -Path $manifestFilePath -Value $manifestContent

# Create JAR file
Write-Output "Creating JAR file..."
jar -cfm $jarFilePath $manifestFilePath -C $destinationDir .

# Run the JAR file
Write-Output "Running the JAR file..."
java -jar $jarFilePath