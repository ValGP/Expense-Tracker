# Expense Tracker

A full-stack web application for managing personal finances.
Users can create accounts, record income and expenses, categorize transactions, and track their financial activity through a simple dashboard.

The project was built as a portfolio project focused on **backend architecture, API design, and full deployment workflow**.

---

## Live Demo

Frontend
https://expense-tracker-frontend-iota-nine.vercel.app

API Documentation (Swagger)
https://expense-tracker-api-qoqv.onrender.com/swagger-ui/index.html

---

## Demo Credentials

You can explore the application using the demo account.

Email: demo@example.com \
Password: demo12345

The demo user resets automatically when logging in to ensure the environment always starts clean.

---

## Features

Authentication

* JWT based authentication
* User registration and login
* Stateless API security

Accounts

* Create and manage financial accounts
* Track balances per account

Transactions

* Record income
* Record expenses
* Transfer between accounts
* Transaction confirmation and cancellation
* Transaction tags and categories

Organization

* Custom categories
* Transaction states (confirmed / cancelled)

Dashboard

* Overview of financial activity
* Net balance calculation
* Monthly income and expense summary

Developer Experience

* REST API with clear structure
* Swagger / OpenAPI documentation
* Demo environment for recruiters
* Production deployment with cloud infrastructure

---

## Tech Stack

Backend

* Java 21
* Spring Boot
* Spring Security
* JWT Authentication
* Hibernate / JPA
* Maven
* PostgreSQL
* Swagger / OpenAPI

Frontend

* React
* Vite
* Axios
* Tailwind CSS

Infrastructure

* Backend deployment: Render
* Frontend deployment: Vercel
* Database: PostgreSQL
* Containerization: Docker

---

## API

The REST API is documented using Swagger.

API documentation
https://expense-tracker-api-qoqv.onrender.com/swagger-ui/index.html

Main endpoints

Authentication

POST /auth/register \
POST /auth/login

Accounts

GET /api/accounts \
POST /api/accounts

Transactions

POST /api/transactions/income \
POST /api/transactions/expense \
POST /api/transactions/transfer

PATCH /api/transactions/{id} \
PATCH /api/transactions/{id}/confirm \
PATCH /api/transactions/{id}/cancel

Demo

POST /demo/reset

---

## Screenshots

Dashboard  
<img src="/docs/dashboard.png" width="200"/>

Transactions \
<img src="/docs/transactions.png" width="200"/>

New Transaction \
<img src="/docs/new_transaction.png" width="200"/>

You can place screenshots inside the `/docs` folder.

---

## Running the Project Locally

Backend

Requirements

Java 21 \
Maven

Run the backend server

./mvnw spring-boot:run

The API will start on

http://localhost:8080

Swagger documentation

http://localhost:8080/swagger-ui/index.html

---

Frontend

Navigate to the frontend project: https://github.com/ValGP/Expense-Tracker-Frontend

cd frontend

Install dependencies

npm install

Start development server

npm run dev

The frontend will run on

http://localhost:5173

---


## Deployment

The application is deployed using modern cloud services.

Frontend

* Hosted on Vercel
* Built using Vite

Backend

* Hosted on Render
* Docker container running Spring Boot

Database

* PostgreSQL hosted on Render

Environment variables are configured directly in the deployment platforms.

---


## Author

Valentin Garcia Pintos

GitHub
https://github.com/ValGP

LinkedIn
https://www.linkedin.com/in/valentingarciapintos/

