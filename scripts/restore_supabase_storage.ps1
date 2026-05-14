param(
    [Parameter(Mandatory = $true)]
    [string]$ProjectUrl,

    [Parameter(Mandatory = $true)]
    [string]$ServiceRoleKey,

    [Parameter(Mandatory = $true)]
    [string]$SourceRoot,

    [string]$BucketName = "pomodoro",

    [switch]$CreateBucketIfMissing
)

$ErrorActionPreference = "Stop"

function Get-AuthHeaders {
    param(
        [string]$Key
    )

    return @{
        apikey        = $Key
        Authorization = "Bearer $Key"
    }
}

function Ensure-Bucket {
    param(
        [string]$BaseUrl,
        [string]$Key,
        [string]$Bucket
    )

    $headers = Get-AuthHeaders -Key $Key
    $bucketUrl = "$BaseUrl/storage/v1/bucket/$Bucket"

    try {
        Invoke-RestMethod -Method Get -Uri $bucketUrl -Headers $headers | Out-Null
        Write-Host "Bucket '$Bucket' already exists."
        return
    } catch {
        Write-Host "Bucket '$Bucket' was not found."
    }

    $createUrl = "$BaseUrl/storage/v1/bucket"
    $body = @{
        id        = $Bucket
        name      = $Bucket
        public    = $true
        file_size_limit = 5242880
    } | ConvertTo-Json

    Invoke-RestMethod `
        -Method Post `
        -Uri $createUrl `
        -Headers ($headers + @{ "Content-Type" = "application/json" }) `
        -Body $body | Out-Null

    Write-Host "Bucket '$Bucket' created."
}

function Upload-FileToSupabase {
    param(
        [string]$BaseUrl,
        [string]$Key,
        [string]$Bucket,
        [string]$RelativePath,
        [string]$FullPath
    )

    $normalizedPath = $RelativePath.Replace("\", "/")
    $uploadUrl = "$BaseUrl/storage/v1/object/$Bucket/$normalizedPath"
    $headers = Get-AuthHeaders -Key $Key
    $headers["x-upsert"] = "true"
    $headers["Content-Type"] = "image/jpeg"

    Invoke-RestMethod `
        -Method Post `
        -Uri $uploadUrl `
        -Headers $headers `
        -InFile $FullPath | Out-Null

    Write-Host "Uploaded $normalizedPath"
}

$baseUrl = $ProjectUrl.TrimEnd("/")
$source = Resolve-Path -LiteralPath $SourceRoot

if ($CreateBucketIfMissing) {
    Ensure-Bucket -BaseUrl $baseUrl -Key $ServiceRoleKey -Bucket $BucketName
}

$files = Get-ChildItem -LiteralPath $source -Recurse -File |
    Where-Object { $_.Name -ne ".emptyFolderPlaceholder" }

foreach ($file in $files) {
    $relativePath = $file.FullName.Substring($source.Path.Length).TrimStart("\")
    Upload-FileToSupabase `
        -BaseUrl $baseUrl `
        -Key $ServiceRoleKey `
        -Bucket $BucketName `
        -RelativePath $relativePath `
        -FullPath $file.FullName
}

Write-Host "Storage restore completed. Uploaded $($files.Count) files."
