for ($i = 1; $i -le 4; $i++) {
    Set-Location ./"#$i"

    if (Test-Path err.txt) {
        Remove-Item err.txt
    }

    if (!(Test-Path ans.txt) -or !(Test-Path out.txt) -or $null -eq (Get-Content ans.txt) -or $null -eq (Get-Content out.txt)) {
        Write-Host -ForegroundColor Red "Test $i does not exist or is empty"
        Set-Location ..
        continue
    }

    if (Compare-Object (Get-Content ans.txt) (Get-Content out.txt)) {
        Compare-Object (Get-Content ans.txt) (Get-Content out.txt) | Format-List | Out-File err.txt
        Write-Host -ForegroundColor Red "Test $i failed"
    } else {
        Write-Host -ForegroundColor Green "Test $i passed"
    }
    Set-Location ..
}
