# 🏗️ INFRASTRUCTURE AS CODE (IaC) - IMPLEMENTACIÓN COMPLETA

## 🎯 **INTRODUCCIÓN A INFRASTRUCTURE AS CODE**

**Infrastructure as Code en Arka Valenzuela** implementa la gestión automatizada de infraestructura usando Terraform, CloudFormation, y Ansible. El sistema garantiza aprovisionamiento reproducible, versionado de infraestructura, gestión de estado distribuido, y deployment multi-ambiente con pipelines automatizados.

---

## 🏗️ **ARQUITECTURA IaC**

```
                    📁 GIT REPOSITORY
                           │
                    ┌──────┴──────┐
                    │ 🔄 IaC      │
                    │ PIPELINE    │
                    │ (CI/CD)     │
                    └──────┬──────┘
                           │
          ┌────────────────┼────────────────┐
          │                │                │
   ┌──────┴──────┐  ┌──────┴──────┐  ┌──────┴──────┐
   │ 🏗️ TERRAFORM│  │ ☁️ CLOUD    │  │ ⚙️ ANSIBLE   │
   │ (PROVISIONING)│  │ FORMATION   │  │ (CONFIG)    │
   │             │  │ (AWS NATIVE)│  │             │
   │• Networking │  │• RDS        │  │• App Config │
   │• Compute    │  │• Lambda     │  │• Security   │
   │• Storage    │  │• S3         │  │• Monitoring │
   └──────┬──────┘  └──────┬──────┘  └──────┬──────┘
          │                │                │
          └────────────────┼────────────────┘
                           │ 📊 State Management
                    ┌──────┴──────┐
                    │ 💾 TERRAFORM│
                    │ BACKEND     │
                    │             │
                    │• S3 Bucket  │
                    │• DynamoDB   │
                    │• Encryption │
                    └──────┬──────┘
                           │
            ┌──────────────┼──────────────┐
            │                             │
     ┌──────┴──────┐               ┌──────┴──────┐
     │ 🌍 MULTI    │               │ 📊 MONITORING│
     │ ENVIRONMENT │               │ & ALERTS    │
     │             │               │             │
     │• Dev        │               │• CloudWatch │
     │• Staging    │               │• Prometheus │
     │• Production │               │• Grafana    │
     └─────────────┘               └─────────────┘
```

---

## 🚀 **TERRAFORM INFRASTRUCTURE**

### 📁 **Estructura de Directorios Terraform**

```
infrastructure-as-code/
├── terraform/
│   ├── modules/
│   │   ├── vpc/
│   │   │   ├── main.tf
│   │   │   ├── variables.tf
│   │   │   ├── outputs.tf
│   │   │   └── versions.tf
│   │   ├── ecs/
│   │   │   ├── main.tf
│   │   │   ├── variables.tf
│   │   │   ├── outputs.tf
│   │   │   └── ecs-service.tf
│   │   ├── rds/
│   │   │   ├── main.tf
│   │   │   ├── variables.tf
│   │   │   └── outputs.tf
│   │   ├── alb/
│   │   │   ├── main.tf
│   │   │   ├── variables.tf
│   │   │   └── outputs.tf
│   │   └── monitoring/
│   │       ├── main.tf
│   │       ├── variables.tf
│   │       └── outputs.tf
│   ├── environments/
│   │   ├── dev/
│   │   │   ├── main.tf
│   │   │   ├── terraform.tfvars
│   │   │   └── backend.tf
│   │   ├── staging/
│   │   │   ├── main.tf
│   │   │   ├── terraform.tfvars
│   │   │   └── backend.tf
│   │   └── prod/
│   │       ├── main.tf
│   │       ├── terraform.tfvars
│   │       └── backend.tf
│   ├── global/
│   │   ├── s3/
│   │   │   └── main.tf
│   │   └── iam/
│   │       └── main.tf
│   └── scripts/
│       ├── plan-all.sh
│       ├── apply-all.sh
│       └── destroy-all.sh
```

### 🌐 **Módulo VPC**

```hcl
# 📁 terraform/modules/vpc/main.tf

terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

# ===========================
# DATA SOURCES
# ===========================
data "aws_availability_zones" "available" {
  state = "available"
}

data "aws_region" "current" {}

# ===========================
# VPC PRINCIPAL
# ===========================
resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = merge(var.common_tags, {
    Name        = "${var.project_name}-vpc-${var.environment}"
    Environment = var.environment
    Type        = "networking"
  })
}

# ===========================
# INTERNET GATEWAY
# ===========================
resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id

  tags = merge(var.common_tags, {
    Name        = "${var.project_name}-igw-${var.environment}"
    Environment = var.environment
  })
}

# ===========================
# SUBNETS PÚBLICAS
# ===========================
resource "aws_subnet" "public" {
  count = var.public_subnet_count

  vpc_id                  = aws_vpc.main.id
  cidr_block              = cidrsubnet(var.vpc_cidr, 8, count.index)
  availability_zone       = data.aws_availability_zones.available.names[count.index]
  map_public_ip_on_launch = true

  tags = merge(var.common_tags, {
    Name        = "${var.project_name}-public-${count.index + 1}-${var.environment}"
    Environment = var.environment
    Type        = "public"
    Tier        = "web"
  })
}

# ===========================
# SUBNETS PRIVADAS
# ===========================
resource "aws_subnet" "private" {
  count = var.private_subnet_count

  vpc_id            = aws_vpc.main.id
  cidr_block        = cidrsubnet(var.vpc_cidr, 8, count.index + var.public_subnet_count)
  availability_zone = data.aws_availability_zones.available.names[count.index]

  tags = merge(var.common_tags, {
    Name        = "${var.project_name}-private-${count.index + 1}-${var.environment}"
    Environment = var.environment
    Type        = "private"
    Tier        = "app"
  })
}

# ===========================
# SUBNETS DE DATOS
# ===========================
resource "aws_subnet" "data" {
  count = var.data_subnet_count

  vpc_id            = aws_vpc.main.id
  cidr_block        = cidrsubnet(var.vpc_cidr, 8, count.index + var.public_subnet_count + var.private_subnet_count)
  availability_zone = data.aws_availability_zones.available.names[count.index]

  tags = merge(var.common_tags, {
    Name        = "${var.project_name}-data-${count.index + 1}-${var.environment}"
    Environment = var.environment
    Type        = "private"
    Tier        = "data"
  })
}

# ===========================
# ELASTIC IPs PARA NAT GATEWAYS
# ===========================
resource "aws_eip" "nat" {
  count = var.enable_nat_gateway ? var.private_subnet_count : 0

  domain = "vpc"

  tags = merge(var.common_tags, {
    Name        = "${var.project_name}-nat-eip-${count.index + 1}-${var.environment}"
    Environment = var.environment
  })

  depends_on = [aws_internet_gateway.main]
}

# ===========================
# NAT GATEWAYS
# ===========================
resource "aws_nat_gateway" "main" {
  count = var.enable_nat_gateway ? var.private_subnet_count : 0

  allocation_id = aws_eip.nat[count.index].id
  subnet_id     = aws_subnet.public[count.index].id

  tags = merge(var.common_tags, {
    Name        = "${var.project_name}-nat-${count.index + 1}-${var.environment}"
    Environment = var.environment
  })

  depends_on = [aws_internet_gateway.main]
}

# ===========================
# ROUTE TABLES
# ===========================
# Route table pública
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }

  tags = merge(var.common_tags, {
    Name        = "${var.project_name}-public-rt-${var.environment}"
    Environment = var.environment
    Type        = "public"
  })
}

# Route tables privadas
resource "aws_route_table" "private" {
  count = var.private_subnet_count

  vpc_id = aws_vpc.main.id

  dynamic "route" {
    for_each = var.enable_nat_gateway ? [1] : []
    content {
      cidr_block     = "0.0.0.0/0"
      nat_gateway_id = aws_nat_gateway.main[count.index].id
    }
  }

  tags = merge(var.common_tags, {
    Name        = "${var.project_name}-private-rt-${count.index + 1}-${var.environment}"
    Environment = var.environment
    Type        = "private"
  })
}

# Route table para subnets de datos
resource "aws_route_table" "data" {
  vpc_id = aws_vpc.main.id

  tags = merge(var.common_tags, {
    Name        = "${var.project_name}-data-rt-${var.environment}"
    Environment = var.environment
    Type        = "data"
  })
}

# ===========================
# ROUTE TABLE ASSOCIATIONS
# ===========================
# Asociaciones públicas
resource "aws_route_table_association" "public" {
  count = var.public_subnet_count

  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public.id
}

# Asociaciones privadas
resource "aws_route_table_association" "private" {
  count = var.private_subnet_count

  subnet_id      = aws_subnet.private[count.index].id
  route_table_id = aws_route_table.private[count.index].id
}

# Asociaciones de datos
resource "aws_route_table_association" "data" {
  count = var.data_subnet_count

  subnet_id      = aws_subnet.data[count.index].id
  route_table_id = aws_route_table.data.id
}

# ===========================
# SECURITY GROUPS
# ===========================
# Security Group para ALB
resource "aws_security_group" "alb" {
  name_prefix = "${var.project_name}-alb-${var.environment}"
  vpc_id      = aws_vpc.main.id

  ingress {
    description = "HTTP"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "HTTPS"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(var.common_tags, {
    Name        = "${var.project_name}-alb-sg-${var.environment}"
    Environment = var.environment
    Type        = "alb"
  })

  lifecycle {
    create_before_destroy = true
  }
}

# Security Group para ECS
resource "aws_security_group" "ecs" {
  name_prefix = "${var.project_name}-ecs-${var.environment}"
  vpc_id      = aws_vpc.main.id

  ingress {
    description     = "HTTP from ALB"
    from_port       = 8080
    to_port         = 8090
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
  }

  ingress {
    description = "Internal communication"
    from_port   = 0
    to_port     = 65535
    protocol    = "tcp"
    self        = true
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(var.common_tags, {
    Name        = "${var.project_name}-ecs-sg-${var.environment}"
    Environment = var.environment
    Type        = "ecs"
  })

  lifecycle {
    create_before_destroy = true
  }
}

# Security Group para RDS
resource "aws_security_group" "rds" {
  name_prefix = "${var.project_name}-rds-${var.environment}"
  vpc_id      = aws_vpc.main.id

  ingress {
    description     = "MySQL from ECS"
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.ecs.id]
  }

  tags = merge(var.common_tags, {
    Name        = "${var.project_name}-rds-sg-${var.environment}"
    Environment = var.environment
    Type        = "rds"
  })

  lifecycle {
    create_before_destroy = true
  }
}

# ===========================
# VPC ENDPOINTS
# ===========================
# S3 Gateway Endpoint
resource "aws_vpc_endpoint" "s3" {
  count = var.enable_vpc_endpoints ? 1 : 0

  vpc_id       = aws_vpc.main.id
  service_name = "com.amazonaws.${data.aws_region.current.name}.s3"
  
  route_table_ids = concat(
    [aws_route_table.public.id],
    aws_route_table.private[*].id
  )

  tags = merge(var.common_tags, {
    Name        = "${var.project_name}-s3-endpoint-${var.environment}"
    Environment = var.environment
    Type        = "vpc-endpoint"
  })
}

# DynamoDB Gateway Endpoint
resource "aws_vpc_endpoint" "dynamodb" {
  count = var.enable_vpc_endpoints ? 1 : 0

  vpc_id       = aws_vpc.main.id
  service_name = "com.amazonaws.${data.aws_region.current.name}.dynamodb"
  
  route_table_ids = concat(
    [aws_route_table.public.id],
    aws_route_table.private[*].id
  )

  tags = merge(var.common_tags, {
    Name        = "${var.project_name}-dynamodb-endpoint-${var.environment}"
    Environment = var.environment
    Type        = "vpc-endpoint"
  })
}

# ECR Interface Endpoints
resource "aws_vpc_endpoint" "ecr_dkr" {
  count = var.enable_vpc_endpoints ? 1 : 0

  vpc_id              = aws_vpc.main.id
  service_name        = "com.amazonaws.${data.aws_region.current.name}.ecr.dkr"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = aws_subnet.private[*].id
  security_group_ids  = [aws_security_group.vpc_endpoints[0].id]
  
  private_dns_enabled = true

  tags = merge(var.common_tags, {
    Name        = "${var.project_name}-ecr-dkr-endpoint-${var.environment}"
    Environment = var.environment
    Type        = "vpc-endpoint"
  })
}

# Security Group para VPC Endpoints
resource "aws_security_group" "vpc_endpoints" {
  count = var.enable_vpc_endpoints ? 1 : 0

  name_prefix = "${var.project_name}-vpc-endpoints-${var.environment}"
  vpc_id      = aws_vpc.main.id

  ingress {
    description = "HTTPS"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = [var.vpc_cidr]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(var.common_tags, {
    Name        = "${var.project_name}-vpc-endpoints-sg-${var.environment}"
    Environment = var.environment
    Type        = "vpc-endpoint"
  })

  lifecycle {
    create_before_destroy = true
  }
}
```

### 📋 **Variables de VPC**

```hcl
# 📁 terraform/modules/vpc/variables.tf

variable "project_name" {
  description = "Name of the project"
  type        = string
  default     = "arka-valenzuela"
}

variable "environment" {
  description = "Environment name"
  type        = string
  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "Environment must be one of: dev, staging, prod."
  }
}

variable "vpc_cidr" {
  description = "CIDR block for VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_count" {
  description = "Number of public subnets"
  type        = number
  default     = 2
  validation {
    condition     = var.public_subnet_count >= 2 && var.public_subnet_count <= 6
    error_message = "Public subnet count must be between 2 and 6."
  }
}

variable "private_subnet_count" {
  description = "Number of private subnets"
  type        = number
  default     = 2
  validation {
    condition     = var.private_subnet_count >= 2 && var.private_subnet_count <= 6
    error_message = "Private subnet count must be between 2 and 6."
  }
}

variable "data_subnet_count" {
  description = "Number of data subnets"
  type        = number
  default     = 2
  validation {
    condition     = var.data_subnet_count >= 2 && var.data_subnet_count <= 6
    error_message = "Data subnet count must be between 2 and 6."
  }
}

variable "enable_nat_gateway" {
  description = "Enable NAT gateway for private subnets"
  type        = bool
  default     = true
}

variable "enable_vpc_endpoints" {
  description = "Enable VPC endpoints for AWS services"
  type        = bool
  default     = true
}

variable "common_tags" {
  description = "Common tags to be applied to all resources"
  type        = map(string)
  default = {
    Project     = "arka-valenzuela"
    ManagedBy   = "terraform"
    Owner       = "devops-team"
    CostCenter  = "engineering"
  }
}
```

### 📤 **Outputs de VPC**

```hcl
# 📁 terraform/modules/vpc/outputs.tf

output "vpc_id" {
  description = "ID of the VPC"
  value       = aws_vpc.main.id
}

output "vpc_cidr_block" {
  description = "CIDR block of the VPC"
  value       = aws_vpc.main.cidr_block
}

output "internet_gateway_id" {
  description = "ID of the Internet Gateway"
  value       = aws_internet_gateway.main.id
}

# Subnets públicas
output "public_subnet_ids" {
  description = "IDs of the public subnets"
  value       = aws_subnet.public[*].id
}

output "public_subnet_cidrs" {
  description = "CIDR blocks of the public subnets"
  value       = aws_subnet.public[*].cidr_block
}

# Subnets privadas
output "private_subnet_ids" {
  description = "IDs of the private subnets"
  value       = aws_subnet.private[*].id
}

output "private_subnet_cidrs" {
  description = "CIDR blocks of the private subnets"
  value       = aws_subnet.private[*].cidr_block
}

# Subnets de datos
output "data_subnet_ids" {
  description = "IDs of the data subnets"
  value       = aws_subnet.data[*].id
}

output "data_subnet_cidrs" {
  description = "CIDR blocks of the data subnets"
  value       = aws_subnet.data[*].cidr_block
}

# Security Groups
output "alb_security_group_id" {
  description = "ID of the ALB security group"
  value       = aws_security_group.alb.id
}

output "ecs_security_group_id" {
  description = "ID of the ECS security group"
  value       = aws_security_group.ecs.id
}

output "rds_security_group_id" {
  description = "ID of the RDS security group"
  value       = aws_security_group.rds.id
}

# NAT Gateways
output "nat_gateway_ids" {
  description = "IDs of the NAT gateways"
  value       = aws_nat_gateway.main[*].id
}

output "nat_public_ips" {
  description = "Public IPs of the NAT gateways"
  value       = aws_eip.nat[*].public_ip
}

# Availability Zones
output "availability_zones" {
  description = "List of availability zones used"
  value       = data.aws_availability_zones.available.names
}
```

---

## 🎯 **MÓDULO ECS**

### 🐳 **ECS Cluster y Servicios**

```hcl
# 📁 terraform/modules/ecs/main.tf

terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

# ===========================
# DATA SOURCES
# ===========================
data "aws_caller_identity" "current" {}
data "aws_region" "current" {}

# ===========================
# ECS CLUSTER
# ===========================
resource "aws_ecs_cluster" "main" {
  name = "${var.project_name}-cluster-${var.environment}"

  configuration {
    execute_command_configuration {
      kms_key_id = var.enable_ecs_exec ? aws_kms_key.ecs[0].arn : null
      logging    = var.enable_ecs_exec ? "OVERRIDE" : "DEFAULT"

      dynamic "log_configuration" {
        for_each = var.enable_ecs_exec ? [1] : []
        content {
          cloud_watch_encryption_enabled = true
          cloud_watch_log_group_name     = aws_cloudwatch_log_group.ecs_exec[0].name
        }
      }
    }
  }

  setting {
    name  = "containerInsights"
    value = var.enable_container_insights ? "enabled" : "disabled"
  }

  tags = merge(var.common_tags, {
    Name        = "${var.project_name}-cluster-${var.environment}"
    Environment = var.environment
    Type        = "ecs-cluster"
  })
}

# ===========================
# CAPACITY PROVIDERS
# ===========================
resource "aws_ecs_cluster_capacity_providers" "main" {
  cluster_name = aws_ecs_cluster.main.name

  capacity_providers = var.capacity_providers

  dynamic "default_capacity_provider_strategy" {
    for_each = var.default_capacity_provider_strategy
    content {
      capacity_provider = default_capacity_provider_strategy.value.capacity_provider
      weight           = default_capacity_provider_strategy.value.weight
      base             = default_capacity_provider_strategy.value.base
    }
  }
}

# ===========================
# KMS KEY PARA ECS EXEC
# ===========================
resource "aws_kms_key" "ecs" {
  count = var.enable_ecs_exec ? 1 : 0

  description             = "ECS Exec encryption key for ${var.project_name}-${var.environment}"
  deletion_window_in_days = 7
  enable_key_rotation     = true

  tags = merge(var.common_tags, {
    Name        = "${var.project_name}-ecs-exec-key-${var.environment}"
    Environment = var.environment
    Type        = "kms"
  })
}

resource "aws_kms_alias" "ecs" {
  count = var.enable_ecs_exec ? 1 : 0

  name          = "alias/${var.project_name}-ecs-exec-${var.environment}"
  target_key_id = aws_kms_key.ecs[0].key_id
}

# ===========================
# CLOUDWATCH LOG GROUPS
# ===========================
resource "aws_cloudwatch_log_group" "ecs_exec" {
  count = var.enable_ecs_exec ? 1 : 0

  name              = "/aws/ecs/${var.project_name}-exec-${var.environment}"
  retention_in_days = var.log_retention_days
  kms_key_id        = aws_kms_key.ecs[0].arn

  tags = merge(var.common_tags, {
    Name        = "${var.project_name}-ecs-exec-logs-${var.environment}"
    Environment = var.environment
    Type        = "cloudwatch-logs"
  })
}

# Log groups para servicios
resource "aws_cloudwatch_log_group" "services" {
  for_each = var.services

  name              = "/aws/ecs/${var.project_name}/${each.key}-${var.environment}"
  retention_in_days = var.log_retention_days

  tags = merge(var.common_tags, {
    Name        = "${var.project_name}-${each.key}-logs-${var.environment}"
    Environment = var.environment
    Service     = each.key
    Type        = "cloudwatch-logs"
  })
}

# ===========================
# IAM ROLES
# ===========================
# Task execution role
resource "aws_iam_role" "task_execution" {
  name = "${var.project_name}-ecs-execution-role-${var.environment}"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })

  tags = merge(var.common_tags, {
    Name        = "${var.project_name}-ecs-execution-role-${var.environment}"
    Environment = var.environment
    Type        = "iam-role"
  })
}

# Attach execution role policy
resource "aws_iam_role_policy_attachment" "task_execution" {
  role       = aws_iam_role.task_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# Custom policy para ECR y CloudWatch
resource "aws_iam_role_policy" "task_execution_custom" {
  name = "${var.project_name}-ecs-execution-custom-${var.environment}"
  role = aws_iam_role.task_execution.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ecr:GetAuthorizationToken",
          "ecr:BatchCheckLayerAvailability",
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage"
        ]
        Resource = "*"
      },
      {
        Effect = "Allow"
        Action = [
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ]
        Resource = "arn:aws:logs:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:*"
      },
      {
        Effect = "Allow"
        Action = [
          "secretsmanager:GetSecretValue"
        ]
        Resource = [
          "arn:aws:secretsmanager:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:secret:${var.project_name}/*"
        ]
      }
    ]
  })
}

# Task role para aplicaciones
resource "aws_iam_role" "task_role" {
  for_each = var.services

  name = "${var.project_name}-${each.key}-task-role-${var.environment}"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })

  tags = merge(var.common_tags, {
    Name        = "${var.project_name}-${each.key}-task-role-${var.environment}"
    Environment = var.environment
    Service     = each.key
    Type        = "iam-role"
  })
}

# Policies específicas por servicio
resource "aws_iam_role_policy" "task_role_policy" {
  for_each = var.services

  name = "${var.project_name}-${each.key}-task-policy-${var.environment}"
  role = aws_iam_role.task_role[each.key].id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = concat([
      # Base permissions
      {
        Effect = "Allow"
        Action = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ]
        Resource = "arn:aws:logs:*:*:*"
      }
    ], each.value.additional_policies != null ? each.value.additional_policies : [])
  })
}

# ===========================
# TASK DEFINITIONS
# ===========================
resource "aws_ecs_task_definition" "services" {
  for_each = var.services

  family                   = "${var.project_name}-${each.key}-${var.environment}"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = each.value.cpu
  memory                   = each.value.memory
  execution_role_arn       = aws_iam_role.task_execution.arn
  task_role_arn           = aws_iam_role.task_role[each.key].arn

  container_definitions = jsonencode([
    {
      name      = each.key
      image     = "${var.ecr_registry}/${var.project_name}/${each.key}:latest"
      essential = true

      portMappings = [
        {
          containerPort = each.value.container_port
          protocol      = "tcp"
        }
      ]

      environment = concat([
        {
          name  = "SPRING_PROFILES_ACTIVE"
          value = var.environment
        },
        {
          name  = "EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE"
          value = "http://arka-eureka-alb-${var.environment}.internal:8761/eureka/"
        },
        {
          name  = "SPRING_CLOUD_CONFIG_URI"
          value = "http://arka-config-alb-${var.environment}.internal:8888"
        }
      ], each.value.environment_variables != null ? each.value.environment_variables : [])

      secrets = each.value.secrets != null ? each.value.secrets : []

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.services[each.key].name
          "awslogs-region"        = data.aws_region.current.name
          "awslogs-stream-prefix" = "ecs"
        }
      }

      healthCheck = each.value.health_check != null ? {
        command     = each.value.health_check.command
        interval    = each.value.health_check.interval
        timeout     = each.value.health_check.timeout
        retries     = each.value.health_check.retries
        startPeriod = each.value.health_check.start_period
      } : {
        command     = ["CMD-SHELL", "curl -f http://localhost:${each.value.container_port}/actuator/health || exit 1"]
        interval    = 30
        timeout     = 5
        retries     = 3
        startPeriod = 60
      }

      linuxParameters = var.enable_ecs_exec ? {
        initProcessEnabled = true
      } : null
    }
  ])

  tags = merge(var.common_tags, {
    Name        = "${var.project_name}-${each.key}-task-${var.environment}"
    Environment = var.environment
    Service     = each.key
    Type        = "ecs-task-definition"
  })
}

# ===========================
# ECS SERVICES
# ===========================
resource "aws_ecs_service" "services" {
  for_each = var.services

  name            = "${var.project_name}-${each.key}-${var.environment}"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.services[each.key].arn
  desired_count   = each.value.desired_count
  launch_type     = "FARGATE"

  platform_version = "LATEST"

  deployment_configuration {
    maximum_percent         = each.value.max_capacity != null ? each.value.max_capacity : 200
    minimum_healthy_percent = each.value.min_capacity != null ? each.value.min_capacity : 50
  }

  network_configuration {
    subnets          = var.private_subnet_ids
    security_groups  = [var.ecs_security_group_id]
    assign_public_ip = false
  }

  dynamic "load_balancer" {
    for_each = each.value.load_balancer_config != null ? [each.value.load_balancer_config] : []
    content {
      target_group_arn = load_balancer.value.target_group_arn
      container_name   = each.key
      container_port   = each.value.container_port
    }
  }

  dynamic "service_registries" {
    for_each = each.value.service_discovery_config != null ? [each.value.service_discovery_config] : []
    content {
      registry_arn = service_registries.value.registry_arn
    }
  }

  enable_execute_command = var.enable_ecs_exec

  tags = merge(var.common_tags, {
    Name        = "${var.project_name}-${each.key}-service-${var.environment}"
    Environment = var.environment
    Service     = each.key
    Type        = "ecs-service"
  })

  depends_on = [
    aws_iam_role_policy_attachment.task_execution,
    aws_iam_role_policy.task_execution_custom
  ]

  lifecycle {
    ignore_changes = [task_definition]
  }
}

# ===========================
# APPLICATION AUTO SCALING
# ===========================
resource "aws_appautoscaling_target" "ecs_target" {
  for_each = {
    for k, v in var.services : k => v
    if v.auto_scaling != null && v.auto_scaling.enabled
  }

  max_capacity       = each.value.auto_scaling.max_capacity
  min_capacity       = each.value.auto_scaling.min_capacity
  resource_id        = "service/${aws_ecs_cluster.main.name}/${aws_ecs_service.services[each.key].name}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"

  tags = merge(var.common_tags, {
    Name        = "${var.project_name}-${each.key}-scaling-target-${var.environment}"
    Environment = var.environment
    Service     = each.key
  })
}

# CPU utilization scaling policy
resource "aws_appautoscaling_policy" "ecs_cpu_policy" {
  for_each = {
    for k, v in var.services : k => v
    if v.auto_scaling != null && v.auto_scaling.enabled
  }

  name               = "${var.project_name}-${each.key}-cpu-scaling-${var.environment}"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.ecs_target[each.key].resource_id
  scalable_dimension = aws_appautoscaling_target.ecs_target[each.key].scalable_dimension
  service_namespace  = aws_appautoscaling_target.ecs_target[each.key].service_namespace

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }
    target_value       = each.value.auto_scaling.cpu_target
    scale_in_cooldown  = each.value.auto_scaling.scale_in_cooldown
    scale_out_cooldown = each.value.auto_scaling.scale_out_cooldown
  }
}

# Memory utilization scaling policy
resource "aws_appautoscaling_policy" "ecs_memory_policy" {
  for_each = {
    for k, v in var.services : k => v
    if v.auto_scaling != null && v.auto_scaling.enabled
  }

  name               = "${var.project_name}-${each.key}-memory-scaling-${var.environment}"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.ecs_target[each.key].resource_id
  scalable_dimension = aws_appautoscaling_target.ecs_target[each.key].scalable_dimension
  service_namespace  = aws_appautoscaling_target.ecs_target[each.key].service_namespace

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageMemoryUtilization"
    }
    target_value       = each.value.auto_scaling.memory_target
    scale_in_cooldown  = each.value.auto_scaling.scale_in_cooldown
    scale_out_cooldown = each.value.auto_scaling.scale_out_cooldown
  }
}
```

---

## 🔄 **PIPELINE DE INFRAESTRUCTURA**

### ⚡ **GitHub Actions para IaC**

```yaml
# 📁 .github/workflows/infrastructure.yml
name: 🏗️ Infrastructure as Code Pipeline

on:
  push:
    branches: [main, develop]
    paths:
      - 'infrastructure-as-code/**'
      - '.github/workflows/infrastructure.yml'
  pull_request:
    branches: [main]
    paths:
      - 'infrastructure-as-code/**'
  workflow_dispatch:
    inputs:
      environment:
        description: 'Target environment'
        required: true
        default: 'dev'
        type: choice
        options:
          - dev
          - staging
          - prod
      action:
        description: 'Terraform action'
        required: true
        default: 'plan'
        type: choice
        options:
          - plan
          - apply
          - destroy
      auto_approve:
        description: 'Auto approve apply'
        required: false
        default: false
        type: boolean

env:
  AWS_REGION: us-east-1
  TF_VERSION: 1.5.7
  TERRAGRUNT_VERSION: 0.50.17

jobs:
  # ===============================
  # 🔍 TERRAFORM VALIDATION
  # ===============================
  terraform-validate:
    name: 🔍 Terraform Validation
    runs-on: ubuntu-latest
    
    steps:
      - name: 📥 Checkout code
        uses: actions/checkout@v4

      - name: 🔧 Setup Terraform
        uses: hashicorp/setup-terraform@v3
        with:
          terraform_version: ${{ env.TF_VERSION }}

      - name: 📋 Terraform Format Check
        run: |
          cd infrastructure-as-code/terraform
          terraform fmt -check -recursive

      - name: ✅ Terraform Init & Validate
        run: |
          cd infrastructure-as-code/terraform/environments/dev
          terraform init -backend=false
          terraform validate

      - name: 🔍 TFLint
        uses: terraform-linters/setup-tflint@v4
        with:
          tflint_version: latest

      - name: 🔍 Run TFLint
        run: |
          cd infrastructure-as-code/terraform
          tflint --init
          tflint --recursive

  # ===============================
  # 🔒 SECURITY SCANNING
  # ===============================
  terraform-security:
    name: 🔒 Terraform Security Scan
    runs-on: ubuntu-latest
    needs: [terraform-validate]

    steps:
      - name: 📥 Checkout code
        uses: actions/checkout@v4

      - name: 🔒 Run Checkov
        uses: bridgecrewio/checkov-action@master
        with:
          directory: infrastructure-as-code/terraform
          framework: terraform
          output_format: sarif
          output_file_path: checkov.sarif

      - name: 📤 Upload Checkov results
        uses: github/codeql-action/upload-sarif@v2
        if: always()
        with:
          sarif_file: checkov.sarif

      - name: 🔍 Run Terrascan
        uses: tenable/terrascan-action@main
        with:
          iac_type: 'terraform'
          iac_dir: 'infrastructure-as-code/terraform'
          policy_type: 'aws'
          only_warn: true
          sarif_upload: true

  # ===============================
  # 📊 COST ESTIMATION
  # ===============================
  terraform-cost:
    name: 📊 Cost Estimation
    runs-on: ubuntu-latest
    needs: [terraform-validate]
    if: github.event_name == 'pull_request'

    steps:
      - name: 📥 Checkout code
        uses: actions/checkout@v4

      - name: 🔧 Setup Terraform
        uses: hashicorp/setup-terraform@v3
        with:
          terraform_version: ${{ env.TF_VERSION }}

      - name: 🔐 Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v4
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: ${{ env.AWS_REGION }}

      - name: 📊 Infracost estimation
        uses: infracost/actions/setup@v2
        with:
          api-key: ${{ secrets.INFRACOST_API_KEY }}

      - name: 📊 Generate cost estimate
        run: |
          cd infrastructure-as-code/terraform/environments/dev
          terraform init
          terraform plan -out=tfplan.binary
          terraform show -json tfplan.binary > plan.json
          infracost breakdown --path=plan.json --format=json --out-file=cost.json

      - name: 📊 Post cost comment
        uses: infracost/actions/comment@v1
        with:
          path: infrastructure-as-code/terraform/environments/dev/cost.json
          behavior: update

  # ===============================
  # 🏗️ TERRAFORM PLAN
  # ===============================
  terraform-plan:
    name: 🏗️ Terraform Plan
    runs-on: ubuntu-latest
    needs: [terraform-validate, terraform-security]
    strategy:
      matrix:
        environment: 
          - ${{ github.event.inputs.environment || (github.ref == 'refs/heads/main' && 'prod' || 'dev') }}
    
    steps:
      - name: 📥 Checkout code
        uses: actions/checkout@v4

      - name: 🔧 Setup Terraform
        uses: hashicorp/setup-terraform@v3
        with:
          terraform_version: ${{ env.TF_VERSION }}

      - name: 🔐 Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v4
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: ${{ env.AWS_REGION }}

      - name: 📦 Terraform Init
        run: |
          cd infrastructure-as-code/terraform/environments/${{ matrix.environment }}
          terraform init

      - name: 🏗️ Terraform Plan
        id: plan
        run: |
          cd infrastructure-as-code/terraform/environments/${{ matrix.environment }}
          terraform plan -detailed-exitcode -out=tfplan || exit_code=$?
          echo "exit_code=$exit_code" >> $GITHUB_OUTPUT
          
          if [ $exit_code -eq 1 ]; then
            echo "❌ Terraform plan failed"
            exit 1
          elif [ $exit_code -eq 2 ]; then
            echo "📋 Changes detected"
          else
            echo "✅ No changes detected"
          fi

      - name: 📋 Upload plan artifact
        if: steps.plan.outputs.exit_code == '2'
        uses: actions/upload-artifact@v3
        with:
          name: terraform-plan-${{ matrix.environment }}
          path: infrastructure-as-code/terraform/environments/${{ matrix.environment }}/tfplan
          retention-days: 5

      - name: 📊 Plan summary
        if: steps.plan.outputs.exit_code == '2'
        run: |
          cd infrastructure-as-code/terraform/environments/${{ matrix.environment }}
          terraform show -no-color tfplan > plan-output.txt
          echo "## 🏗️ Terraform Plan for ${{ matrix.environment }}" >> $GITHUB_STEP_SUMMARY
          echo '```' >> $GITHUB_STEP_SUMMARY
          head -100 plan-output.txt >> $GITHUB_STEP_SUMMARY
          echo '```' >> $GITHUB_STEP_SUMMARY

  # ===============================
  # 🚀 TERRAFORM APPLY
  # ===============================
  terraform-apply:
    name: 🚀 Terraform Apply
    runs-on: ubuntu-latest
    needs: [terraform-plan]
    if: |
      (github.ref == 'refs/heads/main' || github.event_name == 'workflow_dispatch') &&
      (github.event.inputs.action == 'apply' || github.event.inputs.action == '' || github.event_name == 'push')
    environment:
      name: ${{ github.event.inputs.environment || (github.ref == 'refs/heads/main' && 'prod' || 'dev') }}
      url: https://${{ github.event.inputs.environment || (github.ref == 'refs/heads/main' && 'prod' || 'dev') }}.arka.com

    strategy:
      matrix:
        environment: 
          - ${{ github.event.inputs.environment || (github.ref == 'refs/heads/main' && 'prod' || 'dev') }}

    steps:
      - name: 📥 Checkout code
        uses: actions/checkout@v4

      - name: 🔧 Setup Terraform
        uses: hashicorp/setup-terraform@v3
        with:
          terraform_version: ${{ env.TF_VERSION }}

      - name: 🔐 Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v4
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: ${{ env.AWS_REGION }}

      - name: 📥 Download plan artifact
        uses: actions/download-artifact@v3
        with:
          name: terraform-plan-${{ matrix.environment }}
          path: infrastructure-as-code/terraform/environments/${{ matrix.environment }}/

      - name: 📦 Terraform Init
        run: |
          cd infrastructure-as-code/terraform/environments/${{ matrix.environment }}
          terraform init

      - name: 🚀 Terraform Apply
        run: |
          cd infrastructure-as-code/terraform/environments/${{ matrix.environment }}
          if [ -f tfplan ]; then
            echo "📋 Applying saved plan..."
            terraform apply -auto-approve tfplan
          else
            echo "🏗️ Planning and applying..."
            terraform apply -auto-approve
          fi

      - name: 📊 Output infrastructure endpoints
        run: |
          cd infrastructure-as-code/terraform/environments/${{ matrix.environment }}
          echo "## 🌐 Infrastructure Endpoints" >> $GITHUB_STEP_SUMMARY
          echo "| Service | Endpoint |" >> $GITHUB_STEP_SUMMARY
          echo "|---------|----------|" >> $GITHUB_STEP_SUMMARY
          terraform output -json | jq -r 'to_entries[] | "| \(.key) | \(.value.value) |"' >> $GITHUB_STEP_SUMMARY

      - name: 🎉 Deployment notification
        if: always()
        uses: 8398a7/action-slack@v3
        with:
          status: ${{ job.status }}
          channel: '#infrastructure'
          webhook_url: ${{ secrets.SLACK_WEBHOOK }}
          text: |
            🏗️ Infrastructure deployment ${{ job.status }} on ${{ matrix.environment }}!
            Commit: ${{ github.sha }}
            Author: ${{ github.actor }}

  # ===============================
  # 🧪 INFRASTRUCTURE TESTS
  # ===============================
  infrastructure-tests:
    name: 🧪 Infrastructure Tests
    runs-on: ubuntu-latest
    needs: [terraform-apply]
    if: needs.terraform-apply.result == 'success'

    strategy:
      matrix:
        environment: 
          - ${{ github.event.inputs.environment || (github.ref == 'refs/heads/main' && 'prod' || 'dev') }}

    steps:
      - name: 📥 Checkout code
        uses: actions/checkout@v4

      - name: 🔧 Setup Terraform
        uses: hashicorp/setup-terraform@v3
        with:
          terraform_version: ${{ env.TF_VERSION }}

      - name: 🔐 Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v4
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: ${{ env.AWS_REGION }}

      - name: 🧪 Infrastructure connectivity tests
        run: |
          cd infrastructure-as-code/terraform/environments/${{ matrix.environment }}
          terraform init
          
          # Obtener outputs
          VPC_ID=$(terraform output -raw vpc_id)
          ALB_DNS=$(terraform output -raw alb_dns_name)
          
          echo "🔍 Testing VPC connectivity..."
          aws ec2 describe-vpcs --vpc-ids $VPC_ID --query 'Vpcs[0].State' --output text
          
          echo "🔍 Testing ALB health..."
          timeout 300 bash -c 'until curl -f http://'$ALB_DNS'/health; do sleep 10; done'

      - name: 🧪 Security group validation
        run: |
          cd infrastructure-as-code/terraform/environments/${{ matrix.environment }}
          
          echo "🔒 Validating security groups..."
          # Verificar que no hay reglas 0.0.0.0/0 en puertos críticos
          aws ec2 describe-security-groups \
            --filters "Name=group-name,Values=*arka-valenzuela*" \
            --query 'SecurityGroups[?IpPermissions[?IpRanges[?CidrIp==`0.0.0.0/0`] && (FromPort==`22` || FromPort==`3306` || FromPort==`5432`)]]' \
            --output text | wc -l | xargs -I {} test {} -eq 0

      - name: 🧪 Cost optimization check
        run: |
          echo "💰 Checking for cost optimization opportunities..."
          
          # Verificar instancias no utilizadas
          aws ec2 describe-instances \
            --filters "Name=instance-state-name,Values=running" "Name=tag:Project,Values=arka-valenzuela" \
            --query 'Reservations[].Instances[?CpuOptions.CoreCount>`2`]' \
            --output table

  # ===============================
  # 🔄 TERRAFORM DESTROY
  # ===============================
  terraform-destroy:
    name: 🔄 Terraform Destroy
    runs-on: ubuntu-latest
    if: github.event_name == 'workflow_dispatch' && github.event.inputs.action == 'destroy'
    environment:
      name: ${{ github.event.inputs.environment }}-destroy

    strategy:
      matrix:
        environment: 
          - ${{ github.event.inputs.environment }}

    steps:
      - name: 📥 Checkout code
        uses: actions/checkout@v4

      - name: 🔧 Setup Terraform
        uses: hashicorp/setup-terraform@v3
        with:
          terraform_version: ${{ env.TF_VERSION }}

      - name: 🔐 Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v4
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: ${{ env.AWS_REGION }}

      - name: 📦 Terraform Init
        run: |
          cd infrastructure-as-code/terraform/environments/${{ matrix.environment }}
          terraform init

      - name: 🗑️ Terraform Destroy
        run: |
          cd infrastructure-as-code/terraform/environments/${{ matrix.environment }}
          terraform destroy -auto-approve

      - name: 🚨 Destroy notification
        if: always()
        uses: 8398a7/action-slack@v3
        with:
          status: ${{ job.status }}
          channel: '#infrastructure'
          webhook_url: ${{ secrets.SLACK_WEBHOOK }}
          text: |
            🗑️ Infrastructure DESTROYED on ${{ matrix.environment }}!
            Status: ${{ job.status }}
            Triggered by: ${{ github.actor }}
            ⚠️ Please verify all resources have been removed!
```

---

## 🏆 **BENEFICIOS DE INFRASTRUCTURE AS CODE**

### ✅ **Automatización Completa**

```
🤖 IaC IMPLEMENTADO:
├── Terraform modular y reutilizable ✅
├── Multiple ambientes (dev/staging/prod) ✅
├── State management distribuido ✅
├── Pipelines CI/CD automatizados ✅
└── Validación y testing automático ✅
```

### ✅ **Seguridad y Compliance**

```
🛡️ SEGURIDAD GARANTIZADA:
├── Security scanning con Checkov/Terrascan ✅
├── Least privilege IAM policies ✅
├── Encrypted state storage ✅
├── VPC endpoints para servicios AWS ✅
└── Network segmentation implementada ✅
```

### ✅ **Observabilidad y Monitoring**

```
📊 MONITORING INTEGRADO:
├── CloudWatch logs y métricas ✅
├── Cost monitoring y alertas ✅
├── Infrastructure testing automatizado ✅
├── Compliance validation ✅
└── Performance monitoring ✅
```

---

*Documentación de Infrastructure as Code*  
*Proyecto: Arka Valenzuela*  
*Implementación completa con Terraform y pipelines*  
*Fecha: 8 de Septiembre de 2025*
