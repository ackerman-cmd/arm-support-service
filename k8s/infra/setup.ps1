Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$NAMESPACE = "infra"
$SCRIPT_DIR = $PSScriptRoot

Write-Host "==> Adding Bitnami Helm repository..." -ForegroundColor Cyan
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

Write-Host "==> Creating namespace '$NAMESPACE'..." -ForegroundColor Cyan
kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -

Write-Host "==> Installing PostgreSQL..." -ForegroundColor Cyan
helm upgrade --install postgres bitnami/postgresql `
    --namespace $NAMESPACE `
    --values "$SCRIPT_DIR\postgres-values.yaml" `
    --set image.registry=docker.io `
    --set image.repository=bitnami/postgresql `
    --set image.tag=latest `
    --set image.pullPolicy=IfNotPresent `
    --wait --timeout 5m

Write-Host "PostgreSQL is ready" -ForegroundColor Green
Write-Host "  Host: postgres.$NAMESPACE.svc.cluster.local:5432"
Write-Host "  DB:   user_service  |  User: admin  |  Pass: secret"

Write-Host "==> Installing Kafka..." -ForegroundColor Cyan
helm upgrade --install kafka bitnami/kafka `
    --version 25.3.5 `
    --namespace $NAMESPACE `
    --values "$SCRIPT_DIR\kafka-values.yaml" `
    --set image.registry=docker.io `
    --set image.repository=bitnami/kafka `
    --set image.tag=3.5 `
    --set image.pullPolicy=IfNotPresent `
    --wait --timeout 5m

Write-Host "Kafka is ready" -ForegroundColor Green
Write-Host "  Bootstrap: kafka.$NAMESPACE.svc.cluster.local:9092"

Write-Host ""
Write-Host "=== Addresses for microservice ConfigMap ===" -ForegroundColor Yellow
Write-Host "POSTGRES_HOST:            postgres.$NAMESPACE.svc.cluster.local"
Write-Host "POSTGRES_PORT:            5432"
Write-Host "KAFKA_BOOTSTRAP_SERVERS:  kafka.$NAMESPACE.svc.cluster.local:9092"

Write-Host ""
Write-Host "==> Check status:" -ForegroundColor Cyan
Write-Host "  kubectl get all -n $NAMESPACE"