# My Crypto Price Viewer

![image](https://github.com/user-attachments/assets/7798a5a8-8240-4981-87df-7c7be409144b)


Hey there! 👋

This is a project I'm building to create a simple web application using Java and Spring Boot. The main idea is to fetch current cryptocurrency prices from the CoinGecko API and display them in a user-friendly way on a webpage.

It's a great way for me to dive deeper into:
- Making API calls from a Java backend (using Spring WebFlux).
- Building web interfaces with Spring Boot and Thymeleaf.
- Handling and displaying data dynamically.
- Implementing security features with Spring Security.
- Caching data for better performance.
- Working with databases using Spring Data JPA.

## ✨ Features

- Fetches current prices and other market data (like 24h change, market cap) for various cryptocurrencies.
- **Portfolio Tracking:** Lets you track your own crypto investments with a personal portfolio feature!
- Displays data in a clean table format.
- Allows selecting different base currencies (e.g., EUR, USD).
- Implements caching for API responses to improve performance and reduce API call frequency.
- Basic security setup with Spring Security.

## 🛠️ Technologies Used

- **Java:** Version 21
- **Spring Boot:** Version 3.2.5 (includes WebFlux, Thymeleaf, Spring Security, Spring Data JPA)
- **Maven:** For project dependency management and build.
- **CoinGecko API:** The source for all cryptocurrency data.
- **Thymeleaf:** Server-side Java template engine for web pages.
- **Jackson:** For handling JSON data from the API.
- **Lombok:** To reduce boilerplate code.
- **Bootstrap:** For frontend styling.
- **H2 Database:** In-memory database for development (default profile).
- **PostgreSQL:** Option for a persistent database.
- **Caffeine Cache:** For in-memory caching.

## 🚀 Project Status

This project is currently under active development. I'm continuously learning and adding new features.

## 🏁 Getting Started

Here's how you can get this project up and running on your local machine.

### Prerequisites

- Java Development Kit (JDK) 21 or newer.
- Apache Maven.
- An IDE like IntelliJ IDEA, Eclipse, or VS Code with Java/Spring Boot support (recommended).

### Installation

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/zerox80/CoinGecko-WebApp.git
    cd CoinGecko-WebApp
    ```
2.  **Build the project with Maven:**
    Open your terminal in the project root directory and run:
    ```bash
    mvn clean install
    ```
    This command downloads dependencies and builds the project.

## ▶️ How to Run

You can run the Spring Boot application in a couple of ways:

#### From your IDE

- Open the project in your IDE.
- Locate the main application class: `org.zerox80.coingeckowebapp.CoinGeckoWebApplication` (usually in `src/main/java`).
- Right-click on it and select "Run 'CoinGeckoWebApplication.main()'".

#### Using Maven

- Open your terminal in the project root directory.
- Execute the Spring Boot Maven plugin:
    ```bash
    mvn spring-boot:run
    ```

## 💻 Usage

Once the application is running (check your console for messages like "Started CoinGeckoWebApplication"), open your web browser and navigate to:

[http://localhost:8080/](http://localhost:8080/)

The default configuration uses an H2 in-memory database. You can find other settings in `src/main/resources/application.properties` and `src/main/resources/application-dev.properties`.

## 🤝 Contributing

Feel free to fork this repository, experiment with the code, or suggest improvements! As I'm learning, any feedback is welcome.

## 📄 License

This project is licensed under the GPLv3 License. See the [LICENSE](LICENSE) file for details.
