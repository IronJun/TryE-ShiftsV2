#Avvia l'applicazione E-Shifts. Main permette poi di scegliere GUI oppure CLI.
$projectRoot = $PSScriptRoot
Set-Location -LiteralPath $projectRoot

if (-not $env:JAVA_HOME) {
    # MODIFICA: prova a ricavare JAVAHOME dal comando java disponibile sul PC.
    $javaCommand = Get-Command java -ErrorAction SilentlyContinue
    if ($javaCommand) {
        $javaSettings = & $javaCommand.Source '-XshowSettings:properties' '-version' 2>&1
        $javaHomeLine = $javaSettings | Where-Object { $ -match '^\sjava.home\s=' } | Select-Object -First 1
        if ($javaHomeLine -match '=\s*(.+)$') {
            $env:JAVA_HOME = $matches[1].Trim()
            Write-Host "Using detected JAVA_HOME: $env:JAVA_HOME"
        }
    }
}

if (-not $env:JAVA_HOME) {
    Write-Error "JAVA_HOME is not configured and Java was not found in PATH. Install JDK 23 or configure JAVA_HOME."
    exit 1
}

#MODIFICA: usa esattamente il JDK indicato da JAVA_HOME, non un eventuale Java 8 precedente nel PATH.
$javaExecutable = Join-Path $env:JAVA_HOME 'bin\java.exe'
if (-not (Test-Path -LiteralPath $javaExecutable)) {
    Write-Error "java.exe was not found under JAVA_HOME: $env:JAVA_HOME"
    exit 1
}

& .\mvnw.cmd compile
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

& .\mvnw.cmd dependency:build-classpath '-Dmdep.outputFile=target\classpath.txt'
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

$dependencies = (Get-Content -LiteralPath '.\target\classpath.txt' -Raw).Trim()
& $javaExecutable -cp ".\target\classes;$dependencies" com.ispw.tryeshifts.Main
