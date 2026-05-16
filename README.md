# 🕵️ Fake Social Media Account Detection

A full-stack web application that detects and flags fake or suspicious social media accounts using rule-based analysis and machine learning techniques. Built with a Java backend, a relational database, and a responsive HTML/CSS/JavaScript frontend.

---

## 📌 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Running the Application](#running-the-application)
- [How It Works](#how-it-works)
- [Screenshots](#screenshots)
- [Contributing](#contributing)
- [License](#license)

---

## Overview

Fake social media accounts are a growing threat — they're used for spam, misinformation, harassment, and fraud. This project provides a tool to analyze social media account profiles and determine whether they are genuine or fake, based on behavioral and profile-based features.

---

## ✨ Features

- 🔍 Analyze social media account profiles for suspicious patterns
- 🤖 Machine learning / rule-based classification of accounts as **Real** or **Fake**
- 📊 Dashboard to view detection results and statistics
- 🗄️ Database integration to store and retrieve analyzed accounts
- 🌐 Clean and responsive web interface
- 🔐 User authentication (login/register)

---

## 🛠️ Tech Stack

| Layer      | Technology                        |
|------------|-----------------------------------|
| Frontend   | HTML5, CSS3, JavaScript           |
| Backend    | Java (Spring Boot / Servlets)     |
| Database   | MySQL / PostgreSQL                |
| Build Tool | Maven                             |

---

## 📁 Project Structure

```
fake-social-media-account-detection/
│
├── backend/               # Java backend (API & business logic)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/      # Java source files (controllers, services, models)
│   │   │   └── resources/ # Configuration files (application.properties)
│   └── pom.xml            # Maven dependencies
│
├── frontend/              # Web interface
│   ├── index.html         # Main landing page
│   ├── css/               # Stylesheets
│   └── js/                # JavaScript files
│
├── database/              # Database scripts
│   └── schema.sql         # Table definitions and seed data
│
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites

Make sure you have the following installed:

- [Java JDK 11+](https://www.oracle.com/java/technologies/downloads/)
- [Apache Maven 3.6+](https://maven.apache.org/download.cgi)
- [MySQL](https://dev.mysql.com/downloads/) or [PostgreSQL](https://www.postgresql.org/download/)
- A modern web browser (Chrome, Firefox, Edge)

---

### Installation

1. **Clone the repository**

```bash
git clone https://github.com/Deepa4113/fake-social-media-account-detection.git
cd fake-social-media-account-detection
```

2. **Set up the database**

- Create a new database (e.g., `fake_account_db`)
- Run the SQL script to create the required tables:

```bash
mysql -u root -p fake_account_db < database/schema.sql
```

3. **Configure the backend**

Edit `backend/src/main/resources/application.properties` and update your database credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/fake_account_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

4. **Build the backend**

```bash
cd backend
mvn clean install
```

---

### Running the Application

1. **Start the backend server**

```bash
cd backend
mvn spring-boot:run
```

The server will start at `http://localhost:8080`

2. **Open the frontend**

Open `frontend/index.html` in your browser, or serve it via a local server:

```bash
# Using Python
python -m http.server 3000
```

Then navigate to `http://localhost:3000`

---

## ⚙️ How It Works

The system analyzes account features to classify them as real or fake. Key indicators include:

| Feature                        | Description                                      |
|-------------------------------|--------------------------------------------------|
| Profile completeness           | Missing bio, photo, or name                      |
| Follower-to-following ratio    | Unusually high or low ratios                     |
| Account age                    | Newly created accounts flagged higher            |
| Post frequency & content       | Spam-like patterns or no posts                   |
| Username patterns              | Random strings or numeric suffixes               |
| Engagement rate                | Very low likes/comments relative to followers    |

Based on a scoring model, each account receives a **Fake Probability Score** and is labeled as:
- ✅ **Genuine** — likely a real account
- ⚠️ **Suspicious** — exhibits some fake indicators
- ❌ **Fake** — high probability of being a bot or fake account

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a new branch: `git checkout -b feature/your-feature-name`
3. Commit your changes: `git commit -m "Add your feature"`
4. Push to the branch: `git push origin feature/your-feature-name`
5. Open a Pull Request

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

---

## 👩‍💻 Author

**Deepa** — [@Deepa4113](https://github.com/Deepa4113)

---

> ⚡ *Built to make social media safer, one detection at a time.*
