# SketchNote Backend

A comprehensive microservices-based backend system for a collaborative sketching and note-taking platform with integrated learning management, payment processing, and real-time collaboration features.

## 🏗️ Architecture Overview

This project follows a **microservices architecture** pattern with service discovery, API gateway, and event-driven communication using Kafka. The system is designed for scalability, maintainability, and high availability.

### System Architecture

```
┌─────────────┐
│   Clients   │
└──────┬──────┘
       │
┌──────▼──────────────┐
│   API Gateway       │ (Port 8888)
│   Spring Cloud      │
└──────┬──────────────┘
       │
┌──────▼──────────────┐
│  Eureka Server      │ (Port 8761)
│  Service Discovery  │
└──────┬──────────────┘
       │
       ├─────────────────────────────────────┐
       │                                     │
┌──────▼──────────┐              ┌──────────▼─────────┐
│ Identity Service│              │  Project Service   │
│ (Authentication)│              │  (Collaboration)   │
└─────────────────┘              └────────────────────┘
       │                                     │
┌──────▼──────────┐              ┌──────────▼─────────┐
│ Learning Service│              │  Order Service     │
│   (Courses)     │              │   (Payments)       │
└─────────────────┘              └────────────────────┘
       │                                     │
       └─────────────┬───────────────────────┘
                     │
       ┌─────────────▼─────────────┐
       │   Shared Infrastructure   │
       ├───────────────────────────┤
       │ PostgreSQL + PgBouncer    │
       │ Redis Cache               │
       │ Kafka Message Broker      │
       │ Keycloak (OAuth2/OIDC)    │
       └───────────────────────────┘
```

## 🚀 Microservices

### 1. **API Gateway** (Port: 8888)
- **Technology**: Spring Cloud Gateway
- **Purpose**: Single entry point for all client requests
- **Features**:
  - Request routing and load balancing
  - Centralized authentication/authorization
  - Rate limiting and circuit breaking
  - API documentation aggregation (Swagger UI)

### 2. **Eureka Server** (Port: 8761)
- **Technology**: Spring Cloud Netflix Eureka
- **Purpose**: Service discovery and registration
- **Features**:
  - Dynamic service registration
  - Health monitoring
  - Load balancing support

### 3. **Identity Service**
- **Purpose**: User management, authentication, and authorization
- **Key Features**:
  - User registration and profile management
  - OAuth2/OIDC integration with Keycloak
  - Role-based access control (RBAC)
  - Blog and content management
  - Payment integration (PayOS)
  - Subscription plan management
  - Wallet and transaction management
  - Feedback and notification system
  - Real-time messaging (WebSocket)
- **Database**: PostgreSQL (usersdb)
- **Tech Stack**:
  - Spring Boot 3.5.6
  - Spring Security + OAuth2
  - Keycloak Admin Client
  - Redis for caching
  - Kafka for event streaming
  - WebSocket (STOMP)

### 4. **Project Service**
- **Purpose**: Project and canvas management with real-time collaboration
- **Key Features**:
  - Project CRUD operations
  - Page/Canvas management
  - Real-time collaboration via WebSocket
  - Project sharing and permissions
  - AWS S3 integration for file storage
  - Real-time chat functionality
- **Database**: PostgreSQL
- **Tech Stack**:
  - Spring Boot 3.5.6
  - Spring Security + OAuth2
  - AWS S3 SDK (Presigned URLs)
  - WebSocket (STOMP)
  - Redis for caching
  - Kafka for events

### 5. **Learning Service**
- **Purpose**: Learning management system (LMS)
- **Key Features**:
  - Course management
  - Lesson creation and organization
  - Student enrollment
  - Progress tracking
  - Course completion certificates
- **Database**: PostgreSQL
- **Tech Stack**:
  - Spring Boot 3.5.5
  - Spring Security + OAuth2
  - MapStruct for DTO mapping
  - OpenFeign for inter-service communication

### 6. **Order Service**
- **Purpose**: E-commerce and resource management
- **Key Features**:
  - Order processing
  - Template marketplace
  - Icon and resource management
  - Subscription handling
  - Payment integration
  - Admin dashboard analytics
- **Database**: PostgreSQL
- **Tech Stack**:
  - Spring Boot 3.5.6
  - Spring Security + OAuth2
  - Kafka Stream for event processing
  - MapStruct for DTO mapping

## 🛠️ Technology Stack

### Backend Framework
- **Spring Boot**: 3.5.5 - 3.5.6
- **Spring Cloud**: 2025.0.0
- **Java**: 17

### Core Technologies
- **Spring Cloud Gateway**: API Gateway
- **Spring Cloud Netflix Eureka**: Service Discovery
- **Spring Cloud OpenFeign**: Inter-service Communication
- **Spring Data JPA**: ORM and Database Access
- **Spring Security**: Authentication & Authorization
- **Spring Kafka**: Event-driven Architecture

### Databases & Caching
- **PostgreSQL 16**: Primary database
- **PgBouncer**: Connection pooling
- **Redis 7**: Distributed caching and session management

### Message Broker
- **Apache Kafka 3.7**: Event streaming and asynchronous communication

### Authentication & Authorization
- **Keycloak 26.0.2**: Identity and Access Management
- **OAuth2 + OIDC**: Authentication protocol
- **JWT**: Token-based authentication

### Real-time Communication
- **WebSocket (STOMP)**: Real-time messaging and collaboration

### Cloud & Storage
- **AWS S3**: File and media storage
- **Presigned URLs**: Secure file upload/download

### Payment Integration
- **PayOS**: Vietnamese payment gateway

### Development Tools
- **Lombok**: Reduce boilerplate code
- **MapStruct**: Type-safe bean mapping
- **SpringDoc OpenAPI**: API documentation (Swagger)
- **Spring Boot DevTools**: Hot reload during development

### Containerization
- **Docker**: Containerization
- **Docker Compose**: Multi-container orchestration

## 📋 Prerequisites

Before running this project, ensure you have the following installed:

- **Java 17** or higher
- **Maven 3.8+**
- **Docker** and **Docker Compose**
- **Git**

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/SketchNote_BE.git
cd SketchNote_BE
```

### 2. Start Infrastructure Services

Start PostgreSQL, Redis, Kafka, Keycloak, and PgBouncer using Docker Compose:

```bash
docker-compose up -d
```

This will start:
- PostgreSQL (Port: 5432)
- PgBouncer (Port: 6432)
- Redis (Port: 6379)
- Kafka (Port: 9092)
- Keycloak (Port: 8090)

### 3. Configure Keycloak

1. Access Keycloak Admin Console: `http://localhost:8090`
2. Login with credentials:
   - Username: `admin`
   - Password: `admin`
3. Create a new realm for SketchNote
4. Configure OAuth2 clients for each service
5. Set up roles and permissions

### 4. Configure Application Properties

Each service has its own `application.properties` or `application.yml` file. Update the following configurations:

#### Database Configuration
```properties
spring.datasource.url=jdbc:postgresql://localhost:6432/your_database
spring.datasource.username=admin
spring.datasource.password=admin
```

#### Redis Configuration
```properties
spring.redis.host=localhost
spring.redis.port=6379
```

#### Kafka Configuration
```properties
spring.kafka.bootstrap-servers=localhost:9092
```

#### Keycloak Configuration
```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8090/realms/your-realm
```

### 5. Build and Run Services

#### Option A: Run with Maven (Development)

Start services in the following order:

```bash
# 1. Start Eureka Server
cd eureka-server
mvn spring-boot:run

# 2. Start API Gateway
cd ../api-gateway
mvn spring-boot:run

# 3. Start Microservices (in parallel)
cd ../identity-service
mvn spring-boot:run

cd ../project-service
mvn spring-boot:run

cd ../learning-service
mvn spring-boot:run

cd ../order-service
mvn spring-boot:run
```

#### Option B: Run with Docker (Production)

Build and run all services using Docker Compose:

```bash
docker-compose -f docker-compose-services.yml up -d
```

### 6. Verify Services

- **Eureka Dashboard**: http://localhost:8761
- **API Gateway**: http://localhost:8888
- **Swagger UI**: http://localhost:8888/swagger-ui.html
- **Keycloak**: http://localhost:8090

## 📡 API Documentation

Once all services are running, access the centralized API documentation:

```
http://localhost:8888/swagger-ui.html
```

Individual service documentation:
- **Identity Service**: http://localhost:8888/account-service/swagger-ui.html
- **Project Service**: http://localhost:8888/project-service/swagger-ui.html
- **Learning Service**: http://localhost:8888/learning-service/swagger-ui.html
- **Order Service**: http://localhost:8888/order-service/swagger-ui.html

## 🔌 WebSocket Endpoints

### Real-time Chat (Project Service)
```
WebSocket URL: ws://localhost:8888/ws
STOMP Endpoint: /ws
Subscribe: /topic/messages
Send: /app/chat.send
```

### Real-time Collaboration
```
Subscribe: /topic/project/{projectId}
Send: /app/project/{projectId}/update
```

## 🗄️ Database Schema

Each service has its own database schema:

- **usersdb**: Identity Service (users, roles, blogs, payments, subscriptions)
- **projectdb**: Project Service (projects, pages, collaborations)
- **learningdb**: Learning Service (courses, lessons, enrollments)
- **orderdb**: Order Service (orders, templates, resources)

Initialize databases:
```bash
docker exec -it postgres psql -U admin -f /docker-entrypoint-initdb.d/postgres_init.sql
```

## 🔐 Security

### Authentication Flow

1. Client requests authentication via API Gateway
2. Gateway forwards to Identity Service
3. Identity Service validates credentials with Keycloak
4. JWT token is issued and returned to client
5. Client includes JWT in subsequent requests
6. Each service validates JWT independently

### Authorization

- **Role-based Access Control (RBAC)**: Users are assigned roles
- **Resource-level Permissions**: Fine-grained access control
- **OAuth2 Scopes**: API access control

## 📊 Monitoring & Observability

### Health Checks

Each service exposes actuator endpoints:

```
http://localhost:{port}/actuator/health
```

### Service Discovery

Monitor registered services:

```
http://localhost:8761
```

## 🧪 Testing

### Run Unit Tests

```bash
cd {service-name}
mvn test
```

### Run Integration Tests

```bash
mvn verify
```

### WebSocket Testing

Use the provided test client:

```bash
open chat-test-client.html
```

## 🐛 Troubleshooting

### Common Issues

1. **Service not registering with Eureka**
   - Check Eureka server is running
   - Verify `eureka.client.service-url.defaultZone` configuration

2. **Database connection errors**
   - Ensure PostgreSQL is running
   - Check PgBouncer configuration
   - Verify database credentials

3. **Kafka connection issues**
   - Ensure Kafka is running
   - Check `KAFKA_CFG_ADVERTISED_LISTENERS` matches your host

4. **WebSocket connection failures**
   - Check CORS configuration
   - Verify WebSocket endpoint configuration
   - Ensure proper authentication headers

### Logs

View service logs:

```bash
# Docker logs
docker logs -f {container-name}

# Application logs
tail -f {service-name}/logs/application.log
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Team

- **Project Type**: Capstone Project
- **Organization**: SketchNote Team

## 📞 Support

For support and questions, please contact the development team or open an issue in the repository.

## 🔄 Version History

- **v0.0.1-SNAPSHOT**: Initial development version
  - Microservices architecture implementation
  - Core features: Authentication, Projects, Learning, Orders
  - Real-time collaboration via WebSocket
  - Payment integration
  - AWS S3 file storage

## 🎯 Future Enhancements

- [ ] Implement distributed tracing (Zipkin/Jaeger)
- [ ] Add comprehensive logging (ELK Stack)
- [ ] Implement API rate limiting
- [ ] Add GraphQL support
- [ ] Mobile app backend optimization
- [ ] Advanced analytics and reporting
- [ ] Multi-language support (i18n)
- [ ] Enhanced security features (2FA, biometric)

---

**Built with ❤️ by the SketchNote Team**
