#!/bin/bash
set -e

# ==============================================================================
# BarrioDigital - Script de aprovisionamiento automatizado para AWS EC2
# ==============================================================================

ROLE=$1

if [ -z "$ROLE" ]; then
    echo "Uso: ./ec2-setup.sh [apps|mq|kafka|all]"
    exit 1
fi

echo "========================================="
echo " Instalando dependencias base en EC2..."
echo "========================================="

sudo dnf update -y || sudo apt-get update -y
sudo dnf install -y docker git curl || sudo apt-get install -y docker.io git curl

sudo systemctl enable docker
sudo systemctl start docker
sudo usermod -aG docker $USER

# Si la máquina tiene <= 2GB de RAM (t3.small), configurar 4GB de Swap automáticamente
TOTAL_RAM=$(free -m | awk '/^Mem:/{print $2}')
if [ "$TOTAL_RAM" -le 2500 ] && [ ! -f /swapfile ]; then
    echo "Detectada instancia pequeña ($TOTAL_RAM MB RAM). Configurando 4GB de Swap..."
    sudo fallocate -l 4G /swapfile || sudo dd if=/dev/zero of=/swapfile bs=1M count=4096
    sudo chmod 600 /swapfile
    sudo mkswap /swapfile
    sudo swapon /swapfile
    echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
    echo "✓ Memoria Swap de 4GB activada exitosamente."
fi

# Carpetas estándar para plugins de Docker
sudo mkdir -p /usr/local/lib/docker/cli-plugins /usr/libexec/docker/cli-plugins

# Instalar Docker Buildx si no existe
if ! docker buildx version &> /dev/null; then
    echo "Instalando Docker Buildx..."
    BUILDX_URL="https://github.com/docker/buildx/releases/download/v0.19.3/buildx-v0.19.3.linux-amd64"
    sudo curl -SL "$BUILDX_URL" -o /usr/libexec/docker/cli-plugins/docker-buildx
    sudo chmod +x /usr/libexec/docker/cli-plugins/docker-buildx
    sudo cp /usr/libexec/docker/cli-plugins/docker-buildx /usr/local/lib/docker/cli-plugins/docker-buildx
fi

# Instalar Docker Compose (como binario independiente y como plugin de Docker)
if ! docker compose version &> /dev/null && ! command -v docker-compose &> /dev/null; then
    echo "Instalando Docker Compose..."
    COMPOSE_URL="https://github.com/docker/compose/releases/download/v2.32.4/docker-compose-linux-x86_64"
    sudo curl -SL "$COMPOSE_URL" -o /usr/libexec/docker/cli-plugins/docker-compose
    sudo chmod +x /usr/libexec/docker/cli-plugins/docker-compose
    sudo cp /usr/libexec/docker/cli-plugins/docker-compose /usr/local/lib/docker/cli-plugins/docker-compose
    sudo cp /usr/libexec/docker/cli-plugins/docker-compose /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
fi

echo "========================================="
echo " Desplegando stack: $ROLE"
echo "========================================="

case "$ROLE" in
    apps)
        echo "Iniciando ec2-apps (BFF, Microservicios, Frontend)..."
        cd /home/ec2-user/BarrioDigital/infra/apps || cd infra/apps
        docker-compose -f compose.yml up -d --build
        ;;
    mq)
        echo "Iniciando ec2-mq (Clúster RabbitMQ 2 nodos)..."
        cd /home/ec2-user/BarrioDigital/infra/mq || cd infra/mq
        docker-compose -f compose.yml up -d
        ;;
    kafka)
        echo "Iniciando ec2-kafka (Zookeeper 3 nodos + Kafka 3 brokers)..."
        cd /home/ec2-user/BarrioDigital/infra/kafka || cd infra/kafka
        docker-compose -f compose.yml up -d
        ;;
    all)
        echo "Iniciando stack completo unificado en una sola instancia..."
        cd /home/ec2-user/BarrioDigital/infra || cd infra
        docker-compose -f docker-compose-all-in-one.yml up -d --build
        ;;
    *)
        echo "Rol desconocido: $ROLE. Opciones válidas: apps, mq, kafka, all"
        exit 1
        ;;
esac

echo "========================================="
echo " ✓ Despliegue completado con éxito."
echo " Verifique contenedores con: docker ps"
echo "========================================="
