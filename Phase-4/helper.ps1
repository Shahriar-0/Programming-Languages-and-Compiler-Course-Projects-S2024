$ErrorActionPreference = "SilentlyContinue"

function CompileTest() {
    java "@arg.argfile" "main.FunctionCraft" "samples/sample.fl" > $null
}

function ConvertJasmineToClass() {
    java -jar "./utilities/jarFiles/jasmin.jar" "./codeGenOutput/*.j" > $null
    Move-Item "./*.class" "./codeGenOutput"
}

function ConvertClassToJavaByteCode($classFile) {
    $classFile = "./samples" + $classFile + ".class"
    javap -c -l -s -v $classFile
}
    
function ConvertClassToJasmine($classFile) {
    $classFile = "./samples" + $classFile + ".class"
    java -jar "./utilities/jarFiles/classFileAnalyzer.jar" $classFile > $null
}

function RunCompiledCode() {
    Set-Location "./codeGenOutput"
    java Main
    Set-Location ".."
}

function MoveFilesToSample($i) {
    New-Item -ItemType Directory -Path "./samples/$i" -Force > $null
    Move-Item "./codeGenOutput/*.j" "./samples/$i"
    Move-Item "./codeGenOutput/*.class" "./samples/$i"
    Copy-Item "./samples/sample.fl" "./samples/$i"
    Move-Item "./samples/typeCheckErrors.txt" "./samples/$i"
}

if ($args.Length -eq 0) {
    CompileTest
}
else {
    if ($args[0] -eq "-t" -or $args[0] -eq "--test") {
        CompileTest
    }
    elseif ($args[0] -eq "-c" -or $args[0] -eq "--convert") {
        ConvertJasmineToClass
    }
    elseif ($args[0] -eq "-b" -or $args[0] -eq "--bytecode") {
        ConvertClassToJavaByteCode $args[1]
    }
    elseif ($args[0] -eq "-j" -or $args[0] -eq "--jasmine") {
        ConvertClassToJasmine $args[1]
    }
    elseif ($args[0] -eq "-r" -or $args[0] -eq "--run") {
        RunCompiledCode
    }
    elseif ($args[0] -eq "-m" -or $args[0] -eq "--move") {
        MoveFilesToSample $args[1]
    }
    elseif ($args[0] -eq "-h" -or $args[0] -eq "--help") {
        Write-Host "Usage: runTests.ps1 [OPTION] [ARGUMENT]"
        Write-Host "Options:"
        Write-Host "  -t, --test        Compile the test file"
        Write-Host "  -c, --convert     Convert Jasmine to class files"
        Write-Host "  -b, --bytecode    Convert class file to Java bytecode"
        Write-Host "  -j, --jasmine     Convert class file to Jasmine"
        Write-Host "  -r, --run         Run the compiled code"
        Write-Host "  -m, --move        Move files to samples directory"
        Write-Host "  -h, --help        Display this help message"
    }
    else {
        Write-Host "Invalid argument" -ForegroundColor Red
    }
}

# © Sayeh