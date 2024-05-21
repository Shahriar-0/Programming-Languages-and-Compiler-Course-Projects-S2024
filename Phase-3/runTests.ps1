java '@arg.argfile' 'main.FunctionCraft' 'samples\sample1.fl' 'samples\out.txt'

$ansContent = Get-Content 'samples\ans.txt'
$outContent = Get-Content 'samples\out.txt'

if ($ansContent -eq $outContent) {
    Write-Host "Test Passed" -ForegroundColor Green
}
else {
    Write-Host "Test Failed" -ForegroundColor Red
    Compare-Object $ansContent $outContent | Format-Table -AutoSize | Out-File 'samples\err.txt' -Encoding utf8 -Width 1000
}