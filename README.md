# Service

This repository contains the Service application for the ITT platform.

## 📚 Documentation

### For Developers
- **[🔐 Comprehensive Security Guide](./COMPREHENSIVE_SECURITY_GUIDE.md)** - Complete security reference for authentication, authorization, and rate limiting
- **[🧪 Developer Testing Guide](./DEVELOPER_TESTING_GUIDE.md)** - Complete testing reference for unit tests, integration tests, and code coverage
- **[📋 API Design Guidelines](./API_DESIGN_GUIDELINES.md)** - API design standards and best practices

## 🚀 Quick Start

### Running Tests
```bash
# Run all tests with coverage
.\mvnw.cmd clean test jacoco:report

# View coverage report
start target/site/jacoco/index.html

# Run specific test class
.\mvnw.cmd test -Dtest=RoleManagementServiceImplTest
```

### Testing Guidelines
For complete testing guidelines including:
- ✅ **Writing test cases** for services, controllers, and repositories
- ✅ **Running and debugging tests** 
- ✅ **Generating reports**
- ✅ **Best practices** and common issues

See the [Developer Testing Guide](./DEVELOPER_TESTING_GUIDE.md).