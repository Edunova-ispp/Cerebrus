param(
    [string]$TestTarget = '',
    [string]$BaseUrl = 'http://localhost:5173',
    [bool]$Headless = $false
)

$headlessValue = if ($Headless) { 'true' } else { 'false' }

$failsafeArgs = @(
    '-Pe2e',
    "-Dselenium.baseUrl=$BaseUrl",
    "-Dselenium.headless=$headlessValue"
)

if (-not [string]::IsNullOrWhiteSpace($TestTarget)) {
    $testName = [System.IO.Path]::GetFileNameWithoutExtension($TestTarget)
    $failsafeArgs += "-Dit.test=$testName"
}

& "$PSScriptRoot\mvnw.cmd" @failsafeArgs `
    failsafe:integration-test `
    failsafe:verify