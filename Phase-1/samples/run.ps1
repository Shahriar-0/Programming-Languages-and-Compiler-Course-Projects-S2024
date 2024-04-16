$env:CLASSPATH="../utilities/antlr-4.13.1-complete.jar;$env:CLASSPATH"

$current_dir = $PWD
Set-Location ../src/main/grammar
antlr4 -o .antlr/
javac -cp $env:CLASSPATH .antlr/*.java
Set-Location $current_dir

for ($i = 1; $i -le 10; $i++) {
    if (!(Test-Path "#$i") -or !(Test-Path "#$i/in.fl")) {
        Write-Host -ForegroundColor Red "Test $i does not exist"
        continue
    }

    java org.antlr.v4.gui.TestRig main.grammar.FunctionCraft program -tree "#$i/in.fl"  > "#$i/out.txt"
}
