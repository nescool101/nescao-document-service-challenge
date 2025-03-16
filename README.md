# Document Management Service

A Spring Boot service for managing PDF documents with MinIO storage and PostgreSQL metadata management made by nescao(nestor alvarez).

## Features

- Upload PDF documents (up to 500MB)
- Search documents by user, name, and tags
- Download documents via temporary URLs
- Efficient memory management (50MB container limit)

## Prerequisites

- Docker and Docker Compose
- Java 17
- Maven

## Quick Start

1. Clone the repository
2. Create `.env` file in the docker directory similar like this one:

```env
POSTGRESQL_USERNAME=a
POSTGRESQL_PASSWORD=b
MINIO_ROOT_USER=d
MINIO_ROOT_PASSWORD=e
```
3. also you can edit the envExample file that is in the docker directory.

4. Start the application using Docker Compose:
```
docker-compose up --build
```
## API Usage

### Upload Document
```
curl -X POST http://localhost:8080/api/documents \
  -H "Content-Type: multipart/form-data" \
  -F "metadata={\"userId\":\"user1\",\"documentName\":\"sample.pdf\",\"tags\":[\"tag1\",\"tag2\"]}" \
  -F "file=@/path/to/your/file.pdf"
```

### Search Documents
```
# Search with filters
curl "http://localhost:8080/api/documents?userId=user1&documentName=sample&tags=tag1&page=0&size=10"

# Get all documents
curl "http://localhost:8080/api/documents?page=0&size=10"

```

### Download Document
```
# Get temporary download URL
curl http://localhost:8080/api/documents/{documentId}/download

```

## Available Services
- Document Management Service: http://localhost:8080
- MinIO Console: http://localhost:9001
- MinIO API: http://localhost:9000
- PostgreSQL: localhost:5432