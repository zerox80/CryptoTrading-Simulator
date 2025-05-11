# My Crypto Price Viewer

![Java](https://img.shields.io/badge/Java-21-blue.svg) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg) ![Maven](https://img.shields.io/badge/Maven-orange.svg) ![License](https://img.shields.io/badge/License-GPLv3-blue.svg)
![image](https://github.com/user-attachments/assets/c1cb8c6e-77f0-44b5-b0d9-5724f95da014)

Hey there! 👋

This is a project I'm building to create a simple web application using Java and Spring Boot. The main idea is to fetch current cryptocurrency prices from the CoinGecko API and display them in a user-friendly way on a webpage.

It's a great way for me to dive deeper into:

*   Making API calls from a Java backend (using Spring WebFlux).
*   Building web interfaces with Spring Boot and Thymeleaf.
*   Handling and displaying data dynamically.
*   Implementing security features with Spring Security.
*   Caching data for better performance.
*   Working with databases using Spring Data JPA.

## ✨ Features

This application aims to provide a comprehensive yet easy-to-use interface for tracking cryptocurrency data. Here's what it currently offers or is planned:

*   **Real-time Crypto Data:** Fetches current prices, market capitalization, 24-hour trading volume, and percentage change for a wide range of cryptocurrencies.
*   **Portfolio Tracking:** Allows you to create and manage your personal cryptocurrency portfolio. You can add transactions, view your holdings, and see your overall profit/loss.
*   **User-Friendly Interface:** Displays data in a clean, sortable, and paginated table format for easy viewing and analysis.
*   **Currency Selection:** Supports viewing prices in different fiat currencies (e.g., USD, EUR, GBP) based on user preference.
*   **Data Caching:** Implements caching for API responses to enhance performance, reduce load times, and minimize the number of calls to the CoinGecko API.
*   **Security:** Basic security measures are implemented using Spring Security to protect user data and application resources. (Further enhancements planned).
*   **Responsive Design:** The frontend is designed to be responsive, ensuring a good user experience across various devices (desktops, tablets, and mobile phones).

## 🛠️ Technologies Used

I'm utilizing a modern Java-based stack to build this application:

*   **Programming Language:** Java 21
*   **Framework:** Spring Boot 3.2.5
    *   **Spring WebFlux:** For reactive programming and efficient handling of API calls.
    *   **Spring MVC:** For building the web application structure.
    *   **Thymeleaf:** A server-side Java template engine for creating dynamic HTML5 web pages.
    *   **Spring Security:** For authentication and authorization.
    *   **Spring Data JPA:** For database interaction and Object-Relational Mapping (ORM).
*   **Build Tool:** Apache Maven (for dependency management and project build).
*   **API:** CoinGecko API (as the primary source for cryptocurrency market data).
*   **JSON Processing:** Jackson library (for serializing and deserializing JSON data from the API).
*   **Utility:** Lombok (to reduce boilerplate code like getters, setters, constructors, etc.).
*   **Frontend Styling:** Bootstrap (for responsive and modern UI components).
*   **Database:**
    *   **H2 Database:** In-memory database used for the default development profile (easy setup, no external dependencies needed for development).
    *   **PostgreSQL:** Supported as an option for a persistent, production-ready database.
*   **Caching:** Caffeine Cache (a high-performance, near-optimal caching library for Java).

## 🚀 Project Status

This project is currently **under active development**. I'm continuously learning, adding new features, and refining existing ones. My goal is to build a robust and feature-rich application while expanding my knowledge of the Spring ecosystem and modern web development practices.

## 🏁 Getting Started

Follow these instructions to get a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

Before you begin, ensure you have the following installed on your system:

*   **Java Development Kit (JDK):** Version 21 or newer. You can download it from [Oracle](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html) or use an alternative distribution like OpenJDK.
*   **Apache Maven:** A build automation tool used primarily for Java projects. You can download it from the [Apache Maven Project](https://maven.apache.org/download.cgi).
*   **IDE (Optional but Recommended):** An Integrated Development Environment like IntelliJ IDEA, Eclipse, or VS Code with Java and Spring Boot support will make development easier.

### Installation

1.  **Clone the repository:**
    Open your terminal or command prompt and run the following command:
    ```bash
    git clone https://github.com/zerox80/CryptoTrading-Simulator.git
    ```
2.  **Navigate to the project directory:**
    ```bash
    cd CoinGecko-WebApp
    ```
3.  **Build the project with Maven:**
    This command will download all necessary dependencies (as defined in `pom.xml`) and compile the project.
    ```bash
    mvn clean install
    ```
    If the build is successful, you should see a `BUILD SUCCESS` message in your console.

## ▶️ How to Run

You can run the Spring Boot application in a couple of ways:

#### From your IDE

*   Open the cloned project in your preferred IDE (IntelliJ IDEA, Eclipse, VS Code, etc.).
*   Locate the main application class: `org.zerox80.coingeckowebapp.CoinGeckoWebApplication` (usually found in the `src/main/java` directory).
*   Right-click on the `CoinGeckoWebApplication.java` file and select "Run 'CoinGeckoWebApplication.main()'" or a similar option to start the application.

#### Using Maven

*   Open your terminal or command prompt in the root directory of the project.
*   Execute the following Spring Boot Maven plugin command:
    ```bash
    mvn spring-boot:run
    ```

Once the application starts, you'll see log messages in the console, typically ending with something like "Started CoinGeckoWebApplication in X.XXX seconds".

## 💻 Usage

After starting the application, open your favorite web browser and navigate to:

<http://localhost:8080/>

You should see the main page of the Crypto Price Viewer.

The default configuration uses an H2 in-memory database. This means that any data you enter (like portfolio information) will be lost when the application stops. For persistent storage, you can configure the application to use PostgreSQL. Configuration properties can be found and modified in:

*   `src/main/resources/application.properties` (for general properties)
*   `src/main/resources/application-dev.properties` (for development-specific properties, overrides general ones if the 'dev' profile is active)

## 🤝 Contributing

I'm always open to collaboration and learning! If you'd like to contribute to this project, feel free to:

*   **Fork the repository.**
*   **Create a new branch** for your feature or bug fix (`git checkout -b feature/your-feature-name`).
*   **Make your changes** and commit them (`git commit -m 'Add some feature'`).
*   **Push to your branch** (`git push origin feature/your-feature-name`).
*   **Open a Pull Request** with a clear description of your changes.

As I'm still learning and developing this project, any feedback, suggestions, or bug reports are highly appreciated! Please feel free to open an issue on GitHub.

## 📄 License

This project is licensed under the **GNU General Public License v3.0 (GPLv3)**.
See the [LICENSE](LICENSE) file for the full license text.

---

Thank you for checking out my project! I hope you find it interesting.
If you have any questions or just want to chat about it, feel free to reach out.
