Smart Lost & Found Matcher

A Java and MySQL-based application that helps users report lost and found items and identify possible matches based on item names, descriptions, and locations.

Features

- Report lost items
- Report found items
- View all lost and found reports
- Find possible matches between lost and found items
- Store item details in a MySQL database
- Detect duplicate reports

Technologies Used

- Java
- JDBC
- MySQL
- Eclipse IDE

Database Setup

1. Install MySQL Server.
2. Run the SQL commands in "schema.sql" to create the database and "items" table.
3. Configure your MySQL credentials using the "MYSQL_PASSWORD" environment variable.
4. Ensure the MySQL Connector/J JAR is added to the project's classpath.

Run the Project

1. Clone or download this repository.
2. Open the project in Eclipse.
3. Configure the database password in the run configuration using "MYSQL_PASSWORD".
4. Run "Main.java".

Project Structure

- "Main.java" — Application source code
- "schema.sql" — Database setup script
- ".gitignore" — Files excluded from version control
- "README.md" — Project documentation

Future Enhancements

- Add a graphical user interface
- Improve matching accuracy
- Add user authentication and report management
