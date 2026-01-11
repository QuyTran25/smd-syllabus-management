# Test notification payload
Write-Host "=== Checking Notification Payload ===" -ForegroundColor Cyan

$query = @"
SELECT 
    id,
    title,
    type,
    payload
FROM core_service.notifications 
WHERE type = 'ASSIGNMENT' 
ORDER BY created_at DESC 
LIMIT 3;
"@

docker exec smd-postgres psql -U smd_user -d smd_database -c $query

Write-Host "`n=== Payload Details ===" -ForegroundColor Cyan

$detailQuery = @"
SELECT 
    id,
    title,
    payload->>'actionUrl' as action_url,
    payload->>'actionLabel' as action_label,
    payload->>'assignmentId' as assignment_id
FROM core_service.notifications 
WHERE type = 'ASSIGNMENT' 
ORDER BY created_at DESC 
LIMIT 3;
"@

docker exec smd-postgres psql -U smd_user -d smd_database -c $detailQuery
