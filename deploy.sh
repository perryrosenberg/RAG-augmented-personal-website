#!/bin/bash
# RAG-Augmented Personal Website Deployment Script
# Uses AWS credentials from environment (default AWS CLI profile)

set -e

# Parse arguments
SKIP_TERRAFORM=false
SKIP_BACKEND=false
SKIP_FRONTEND=false
SKIP_RAG_RESOURCES=false
TERRAFORM_PLAN_ONLY=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-terraform) SKIP_TERRAFORM=true; shift ;;
        --skip-backend) SKIP_BACKEND=true; shift ;;
        --skip-frontend) SKIP_FRONTEND=true; shift ;;
        --skip-rag-resources) SKIP_RAG_RESOURCES=true; shift ;;
        --terraform-plan-only) TERRAFORM_PLAN_ONLY=true; shift ;;
        *) echo "Unknown option: $1"; exit 1 ;;
    esac
done

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "========================================"
echo "RAG Personal Website Deployment Script"
echo "========================================"
echo ""

# Verify AWS credentials are available
echo "[1/4] Verifying AWS credentials..."
if ! aws sts get-caller-identity > /dev/null 2>&1; then
    echo "  ERROR: AWS credentials not configured."
    echo "  Ensure AWS credentials are configured (aws configure or environment variables)"
    exit 1
fi
echo "  AWS Identity verified"

# -----------------------------------------------------------------------------
# TERRAFORM INFRASTRUCTURE
# -----------------------------------------------------------------------------
if [ "$SKIP_TERRAFORM" = false ]; then
    echo ""
    echo "[2/4] Deploying Terraform Infrastructure..."
    
    cd "$PROJECT_ROOT"
    
    # Initialize Terraform
    echo "  Running terraform init..."
    if ! terraform init -input=false; then
        echo "  ERROR: Terraform init failed!"
        exit 1
    fi
    
    # Plan changes
    echo "  Running terraform plan..."
    if ! terraform plan -out=tfplan -input=false; then
        echo "  ERROR: Terraform plan failed!"
        exit 1
    fi
    
    if [ "$TERRAFORM_PLAN_ONLY" = true ]; then
        echo "  Plan-only mode: skipping apply"
    else
        # Apply changes
        echo "  Running terraform apply..."
        if ! terraform apply -input=false tfplan; then
            echo "  ERROR: Terraform apply failed!"
            rm -f tfplan
            exit 1
        fi
        
        # Clean up plan file
        rm -f tfplan
        
        echo "  Terraform deployment complete"
        
        # Show what was created
        echo "  Terraform outputs:"
        terraform output
    fi
else
    echo ""
    echo "[2/4] Skipping Terraform (--skip-terraform)"
fi

# Get Terraform outputs
cd "$PROJECT_ROOT"
KNOWLEDGE_BUCKET=$(terraform output -raw knowledge_bucket_name 2>/dev/null || echo "")
WEBSITE_BUCKET=$(terraform output -raw website_bucket_name 2>/dev/null || echo "")
LAMBDA_FUNCTION_NAME=$(terraform output -raw lambda_function_name 2>/dev/null || echo "")
LAMBDA_BUCKET=$(terraform output -raw lambda_bucket_name 2>/dev/null || echo "")

# -----------------------------------------------------------------------------
# BACKEND DEPLOYMENT (Lambda)
# -----------------------------------------------------------------------------
if [ "$SKIP_BACKEND" = false ]; then
    echo ""
    echo "[3/4] Deploying Backend (Lambda)..."
    
    BACKEND_DIR="$PROJECT_ROOT/backend"
    
    if [ -d "$BACKEND_DIR" ]; then
        cd "$BACKEND_DIR"
        
        # Build with Gradle
        echo "  Building Java Lambda with Gradle..."
        
        # Ensure Gradle wrapper is executable and has the jar
        if [ -f "gradlew" ]; then
            chmod +x gradlew
            # Download gradle-wrapper.jar if missing
            if [ ! -f "gradle/wrapper/gradle-wrapper.jar" ]; then
                echo "  Downloading Gradle wrapper jar..."
                mkdir -p gradle/wrapper
                curl -sL -o gradle/wrapper/gradle-wrapper.jar \
                    "https://raw.githubusercontent.com/gradle/gradle/v8.5.0/gradle/wrapper/gradle-wrapper.jar"
            fi
            ./gradlew clean build
        elif command -v gradle &> /dev/null; then
            gradle clean build
        else
            echo "  WARNING: Gradle not found, skipping backend build"
            echo "  To fix: Install Gradle or run 'gradle wrapper' in the backend directory"
            SKIP_BACKEND=true
        fi
        
        if [ "$SKIP_BACKEND" = false ] && [ $? -eq 0 ]; then
            # Find the built artifact
            BUILD_ARTIFACT=$(find build/distributions -name "*.zip" 2>/dev/null | head -1)
            if [ -z "$BUILD_ARTIFACT" ]; then
                BUILD_ARTIFACT=$(find build/libs -name "*.jar" 2>/dev/null | head -1)
            fi
            
            if [ -n "$BUILD_ARTIFACT" ] && [ -n "$LAMBDA_BUCKET" ]; then
                echo "  Uploading to S3: $(basename "$BUILD_ARTIFACT")..."
                aws s3 cp "$BUILD_ARTIFACT" "s3://$LAMBDA_BUCKET/query-handler.zip"
                
                if [ -n "$LAMBDA_FUNCTION_NAME" ]; then
                    echo "  Updating Lambda function code..."
                    aws lambda update-function-code \
                        --function-name "$LAMBDA_FUNCTION_NAME" \
                        --s3-bucket "$LAMBDA_BUCKET" \
                        --s3-key "query-handler.zip"
                fi
                
                echo "  Backend deployment complete"
            else
                echo "  WARNING: No build artifact found or bucket not configured"
            fi
        fi
    else
        echo "  WARNING: Backend directory not found"
    fi
else
    echo ""
    echo "[3/4] Skipping Backend (--skip-backend)"
fi

# -----------------------------------------------------------------------------
# FRONTEND DEPLOYMENT
# -----------------------------------------------------------------------------
if [ "$SKIP_FRONTEND" = false ]; then
    echo ""
    echo "[4/4] Deploying Frontend..."
    
    # Try to use nvm to switch to correct Node.js version if available
    if [ -f "$HOME/.nvm/nvm.sh" ]; then
        echo "  Found nvm, loading..."
        export NVM_DIR="$HOME/.nvm"
        . "$NVM_DIR/nvm.sh"
        
        # Check if Node 20 is installed, if not install it
        if ! nvm ls 20 &>/dev/null; then
            echo "  Installing Node.js 20 via nvm..."
            nvm install 20
        fi
        nvm use 20
    fi
    
    # Check Node.js version (Next.js 16 requires Node.js 20.9.0+)
    NODE_VERSION=$(node -v 2>/dev/null | sed 's/v//')
    NODE_MAJOR=$(echo "$NODE_VERSION" | cut -d. -f1)
    if [ -z "$NODE_VERSION" ]; then
        echo "  ERROR: Node.js is not installed"
        echo "  Please install Node.js 20.9.0 or later: https://nodejs.org/"
        echo "  Or install nvm: https://github.com/nvm-sh/nvm"
        exit 1
    elif [ "$NODE_MAJOR" -lt 20 ]; then
        echo "  ERROR: Node.js version $NODE_VERSION is too old"
        echo "  Next.js 16 requires Node.js 20.9.0 or later"
        echo "  Current version: $NODE_VERSION"
        echo ""
        echo "  To fix, either:"
        echo "    1. Install nvm and run: nvm install 20 && nvm use 20"
        echo "    2. Or upgrade Node.js directly: https://nodejs.org/"
        exit 1
    fi
    echo "  Node.js version: $NODE_VERSION"
    
    FRONTEND_DIR="$PROJECT_ROOT/frontend"
    
    if [ -d "$FRONTEND_DIR" ]; then
        cd "$FRONTEND_DIR"
        
        # Install dependencies
        echo "  Installing dependencies..."
        if command -v pnpm &> /dev/null; then
            pnpm install
        else
            npm install
        fi
        
        # Build for production
        echo "  Building Next.js application..."
        npm run build
        
        if [ $? -eq 0 ] && [ -n "$WEBSITE_BUCKET" ]; then
            # For static export, sync the out directory
            OUT_DIR="out"
            if [ ! -d "$OUT_DIR" ]; then
                OUT_DIR=".next"
            fi
            
            echo "  Syncing to S3: $WEBSITE_BUCKET..."
            aws s3 sync "$OUT_DIR" "s3://$WEBSITE_BUCKET" --delete
            
            echo "  Frontend deployment complete"
        else
            echo "  WARNING: Build failed or website bucket not configured"
        fi
    else
        echo "  WARNING: Frontend directory not found"
    fi
else
    echo ""
    echo "[4/4] Skipping Frontend (--skip-frontend)"
fi

# -----------------------------------------------------------------------------
# RAG RESOURCES UPLOAD
# -----------------------------------------------------------------------------
if [ "$SKIP_RAG_RESOURCES" = false ]; then
    echo ""
    echo "[Bonus] Uploading RAG Resources..."
    
    RAG_RESOURCES_DIR="$PROJECT_ROOT/rag-resources"
    
    if [ -d "$RAG_RESOURCES_DIR" ]; then
        FILE_COUNT=$(find "$RAG_RESOURCES_DIR" -type f | wc -l)
        
        if [ "$FILE_COUNT" -gt 0 ] && [ -n "$KNOWLEDGE_BUCKET" ]; then
            echo "  Found $FILE_COUNT file(s) to upload..."
            aws s3 sync "$RAG_RESOURCES_DIR" "s3://$KNOWLEDGE_BUCKET/documents" --delete
            echo "  RAG resources uploaded to $KNOWLEDGE_BUCKET"
        else
            echo "  No files in rag-resources or bucket not configured"
        fi
    else
        echo "  rag-resources directory not found"
    fi
else
    echo ""
    echo "[Bonus] Skipping RAG Resources (--skip-rag-resources)"
fi

# -----------------------------------------------------------------------------
# SUMMARY
# -----------------------------------------------------------------------------
echo ""
echo "========================================"
echo "Deployment Complete!"
echo "========================================"

[ -n "$WEBSITE_BUCKET" ] && echo "Website Bucket: $WEBSITE_BUCKET"
[ -n "$KNOWLEDGE_BUCKET" ] && echo "Knowledge Bucket: $KNOWLEDGE_BUCKET"
[ -n "$LAMBDA_FUNCTION_NAME" ] && echo "Lambda Function: $LAMBDA_FUNCTION_NAME"

echo ""
echo "Run 'terraform output' for full resource details"
