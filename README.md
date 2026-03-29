# 🎮 Real-Time Tic Tac Toe — Microservices Edition

> ⚠️ **This project is currently under active development.** Expect breaking changes, incomplete features, and evolving architecture.

---

## What is this?

A **real-time multiplayer Tic Tac Toe game** built on a production-grade microservices architecture using **Spring Boot** and **WebSocket**. This is not just a game — it's a learning ground and reference implementation for building scalable, containerized microservices with real-time communication.

---

## 🧱 Planned Architecture

| Service                  | Responsibility                                                |
| ------------------------ | ------------------------------------------------------------- |
| **API Gateway**          | Single entry point, routing, load balancing                   |
| **Auth Service**         | User registration, login, JWT token management                |
| **Game Service**         | Game creation, move validation, real-time state via WebSocket |
| **User Service**         | Player profiles and stats                                     |
| **Notification Service** | Real-time events and alerts                                   |

---

## 🛠️ Tech Stack (Planned)

- **Backend** — Spring Boot, Spring WebSocket (STOMP)
- **Service Discovery** — HashiCorp Consul
- **API Gateway** — Spring Cloud Gateway
- **Containerization** — Docker & Docker Compose
- **Authentication** — JWT (JSON Web Tokens)
- **Communication** — WebSocket (real-time), REST (service-to-service)

---

## 🚧 Current Status

The project is in its **initial design and scaffolding phase**. Nothing is production-ready yet.
