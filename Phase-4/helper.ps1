$ErrorActionPreference = "SilentlyContinue"

function CompileTest() {
    java "@arg.argfile" "main.FunctionCraft" "samples/sample.fl" > "./codeGenOutput/compileOutput.txt"

    if (Test-Path "./codeGenOutput/compileOutput.txt") {
        $ansContent = Get-Content "./codeGenOutput/compileOutput.txt"
        $outContent = Get-Content "./codeGenOutput/ans.txt"

        $ansContentNoWS = $ansContent -replace '\s', ''
        $outContentNoWS = $outContent -replace '\s', ''

        $diff = Compare-Object -ReferenceObject $ansContentNoWS -DifferenceObject $outContentNoWS

        if ($null -eq $diff) {
            Write-Host "Test Passed" -ForegroundColor Green
        }
        else {
            Write-Host "Test Failed" -ForegroundColor Red
        }
    }
    else {
        Write-Host "Compilation failed" -ForegroundColor Red
    }
}

function ConvertJasminToClass() {
    java -jar "./utilities/jarFiles/jasmin.jar" "./codeGenOutput/*.j" > $null
    Remove-Item "./codeGenOutput/*.class" -Force
    Move-Item "./*.class" "./codeGenOutput"
}

function ConvertClassToJavaByteCode($classFile) {
    $classFile = "./samples" + $classFile + ".class"
    javap -c -l -s -v $classFile
}
    
function ConvertClassToJasmin($classFile) {
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

function DeleteFiles() {
    Remove-Item "./codeGenOutput/*"
}

function FixPrecedingSpaces($file) {
    $file = "./codeGenOutput/" + $file + ".j"
    $content = Get-Content $file
    $content | ForEach-Object {
        if ($_ -match "^\s*\.method") {
            "`n`n" + ($_ -replace "^\s+", "")
        }
        elseif ($_ -match "^\s*\.") {
            $_ -replace "^\s+", ""
        }
        elseif ($_ -match "^\s*Label") {
            $_ -replace "^\s+", "`t"
        }
        else {
            $_
        }
    } | Set-Content $file
}

if ($args.Length -eq 0) {
    CompileTest
    FixPrecedingSpaces Main
}
else {
    if ($args[0] -eq "-t" -or $args[0] -eq "--test") {
        CompileTest
    }
    elseif ($args[0] -eq "-c" -or $args[0] -eq "--convert") {
        ConvertJasminToClass
    }
    elseif ($args[0] -eq "-b" -or $args[0] -eq "--bytecode") {
        ConvertClassToJavaByteCode $args[1]
    }
    elseif ($args[0] -eq "-j" -or $args[0] -eq "--jasmin") {
        ConvertClassToJasmin $args[1]
    }
    elseif ($args[0] -eq "-r" -or $args[0] -eq "--run") {
        RunCompiledCode
    }
    elseif ($args[0] -eq "-m" -or $args[0] -eq "--move") {
        MoveFilesToSample $args[1]
    }
    elseif ($args[0] -eq "-d" -or $args[0] -eq "--delete") {
        DeleteFiles
    }
    elseif ($args[0] -eq "-f" -or $args[0] -eq "--fix") {
        FixPrecedingSpaces $args[1]
    }
    elseif ($args[0] -eq "-h" -or $args[0] -eq "--help") {
        Write-Host "Usage: runTests.ps1 [OPTION] [ARGUMENT]"
        Write-Host "Options:"
        Write-Host "  -t, --test        Compile the test file"
        Write-Host "  -c, --convert     Convert Jasmin to class files"
        Write-Host "  -b, --bytecode    Convert class file to Java bytecode"
        Write-Host "  -j, --jasmin      Convert class file to Jasmin"
        Write-Host "  -r, --run         Run the compiled code"
        Write-Host "  -m, --move        Move files to samples directory"
        Write-Host "  -d, --delete      Delete files in codeGenOutput directory"
        Write-Host "  -f, --fix         Fix preceding spaces in a file"
        Write-Host "  -h, --help        Display this help message"
    }
    else {
        Write-Host "Invalid argument" -ForegroundColor Red
    }
}

# © Sayeh