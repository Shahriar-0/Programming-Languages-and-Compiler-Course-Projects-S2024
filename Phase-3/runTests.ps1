$folders = Get-ChildItem "samples" -Directory

$ErrorActionPreference = "SilentlyContinue"

for ($i = 0; $i -lt $folders.Length; $i++) {
    Remove-Item "samples\$i\out.txt" "samples\$i\diff.txt" > $null 2>&1

    java "@arg.argfile" "main.FunctionCraft" "samples\$i\sample.fl" "samples\$i\out.txt" > $null

    $ansContent = Get-Content "samples\$i\ans.txt"
    $outContent = Get-Content "samples\$i\out.txt"

    if ($ansContent -eq $outContent) {
        Write-Host "Test number $i Passed" -ForegroundColor Green
    }
    else {
        Write-Host "Test number $i Failed" -ForegroundColor Red
        if ($outContent.Length -gt 0) {
            Compare-Object $ansContent $outContent | Format-Table -AutoSize | Out-File "samples\$i\diff.txt"
        }
    }
}