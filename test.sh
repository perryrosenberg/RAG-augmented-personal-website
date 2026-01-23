#!/bin/bash

##############################################################################
# Test Runner Script - RAG-Augmented Personal Website
#
# Runs comprehensive test suite for both backend (Java) and frontend (TypeScript).
# This script is designed to run locally during development and can be integrated
# into CI/CD pipelines.
#
# Usage:
#   ./test.sh                 # Run all tests
#   ./test.sh --backend       # Run only backend tests
#   ./test.sh --frontend      # Run only frontend tests
#   ./test.sh --coverage      # Run all tests with coverage reports
#
# Exit codes:
#   0 - All tests passed
#   1 - Test failures detected
#   2 - Configuration or setup error
##############################################################################

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test flags
RUN_BACKEND=true
RUN_FRONTEND=true
WITH_COVERAGE=false

# Parse command line arguments
while [[ $# -gt 0 ]]; do
  case $1 in
    --backend)
      RUN_FRONTEND=false
      shift
      ;;
    --frontend)
      RUN_BACKEND=false
      shift
      ;;
    --coverage)
      WITH_COVERAGE=true
      shift
      ;;
    --help)
      echo "Usage: ./test.sh [OPTIONS]"
      echo ""
      echo "Options:"
      echo "  --backend     Run only backend (Java) tests"
      echo "  --frontend    Run only frontend (TypeScript) tests"
      echo "  --coverage    Generate coverage reports"
      echo "  --help        Show this help message"
      exit 0
      ;;
    *)
      echo -e "${RED}Unknown option: $1${NC}"
      echo "Run './test.sh --help' for usage information"
      exit 2
      ;;
  esac
done

# Print test configuration
echo -e "${BLUE}╔════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║${NC}  RAG-Augmented Personal Website - Test Suite                  ${BLUE}║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${YELLOW}Configuration:${NC}"
echo -e "  Backend Tests:  ${RUN_BACKEND}"
echo -e "  Frontend Tests: ${RUN_FRONTEND}"
echo -e "  Coverage:       ${WITH_COVERAGE}"
echo ""

# Track test results
BACKEND_STATUS=0
FRONTEND_STATUS=0

##############################################################################
# Backend Tests (Java + Gradle)
##############################################################################

if [ "$RUN_BACKEND" = true ]; then
  echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
  echo -e "${BLUE}Running Backend Tests (Java + Gradle)${NC}"
  echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
  echo ""

  cd backend || {
    echo -e "${RED}Error: backend directory not found${NC}"
    exit 2
  }

  # Determine Gradle command based on OS
  if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
    GRADLE_CMD="./gradlew.bat"
  else
    GRADLE_CMD="./gradlew"
  fi

  # Run tests
  if [ "$WITH_COVERAGE" = true ]; then
    echo -e "${YELLOW}Running tests with JaCoCo coverage...${NC}"
    if $GRADLE_CMD clean test jacocoTestReport; then
      echo -e "${GREEN}✓ Backend tests passed${NC}"
      echo ""
      echo -e "${YELLOW}Coverage report: backend/build/reports/jacoco/test/html/index.html${NC}"
    else
      echo -e "${RED}✗ Backend tests failed${NC}"
      BACKEND_STATUS=1
    fi
  else
    echo -e "${YELLOW}Running tests...${NC}"
    if $GRADLE_CMD test; then
      echo -e "${GREEN}✓ Backend tests passed${NC}"
    else
      echo -e "${RED}✗ Backend tests failed${NC}"
      BACKEND_STATUS=1
    fi
  fi

  echo ""
  echo -e "${YELLOW}Test report: backend/build/reports/tests/test/index.html${NC}"
  echo ""

  cd .. || exit 2
fi

##############################################################################
# Frontend Tests (TypeScript + Jest)
##############################################################################

if [ "$RUN_FRONTEND" = true ]; then
  echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
  echo -e "${BLUE}Running Frontend Tests (TypeScript + Jest)${NC}"
  echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
  echo ""

  cd frontend || {
    echo -e "${RED}Error: frontend directory not found${NC}"
    exit 2
  }

  # Check if node_modules exists
  if [ ! -d "node_modules" ]; then
    echo -e "${YELLOW}Installing frontend dependencies...${NC}"
    npm install || {
      echo -e "${RED}Error: npm install failed${NC}"
      exit 2
    }
  fi

  # Run tests
  if [ "$WITH_COVERAGE" = true ]; then
    echo -e "${YELLOW}Running tests with coverage...${NC}"
    if npm run test:coverage; then
      echo -e "${GREEN}✓ Frontend tests passed${NC}"
      echo ""
      echo -e "${YELLOW}Coverage report: frontend/coverage/lcov-report/index.html${NC}"
    else
      echo -e "${RED}✗ Frontend tests failed${NC}"
      FRONTEND_STATUS=1
    fi
  else
    echo -e "${YELLOW}Running tests...${NC}"
    if npm test -- --passWithNoTests; then
      echo -e "${GREEN}✓ Frontend tests passed${NC}"
    else
      echo -e "${RED}✗ Frontend tests failed${NC}"
      FRONTEND_STATUS=1
    fi
  fi

  echo ""

  cd .. || exit 2
fi

##############################################################################
# Test Summary
##############################################################################

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Test Summary${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

if [ "$RUN_BACKEND" = true ]; then
  if [ $BACKEND_STATUS -eq 0 ]; then
    echo -e "  Backend:  ${GREEN}✓ PASSED${NC}"
  else
    echo -e "  Backend:  ${RED}✗ FAILED${NC}"
  fi
fi

if [ "$RUN_FRONTEND" = true ]; then
  if [ $FRONTEND_STATUS -eq 0 ]; then
    echo -e "  Frontend: ${GREEN}✓ PASSED${NC}"
  else
    echo -e "  Frontend: ${RED}✗ FAILED${NC}"
  fi
fi

echo ""

# Overall exit status
OVERALL_STATUS=$((BACKEND_STATUS + FRONTEND_STATUS))

if [ $OVERALL_STATUS -eq 0 ]; then
  echo -e "${GREEN}╔════════════════════════════════════════════════════════════════╗${NC}"
  echo -e "${GREEN}║${NC}  ✓ All tests passed successfully!                             ${GREEN}║${NC}"
  echo -e "${GREEN}╚════════════════════════════════════════════════════════════════╝${NC}"
  exit 0
else
  echo -e "${RED}╔════════════════════════════════════════════════════════════════╗${NC}"
  echo -e "${RED}║${NC}  ✗ Some tests failed. Please review the output above.         ${RED}║${NC}"
  echo -e "${RED}╚════════════════════════════════════════════════════════════════╝${NC}"
  exit 1
fi
