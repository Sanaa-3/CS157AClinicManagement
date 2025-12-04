**Prompt Used**

“Our team is completing Phase C of the JDBC project. The setup instructions we were given assume an IntelliJ-style environment, but our group works more comfortably in VS Code. How do we configure JDBC, load the MySQL connector, and run everything properly in VS Code?”

**What AI Suggested**

VS Code Setup Compared to IntelliJ: 
- Explained that IntelliJ automatically configures external libraries when you add the MySQL connector JAR, while VS Code requires manual classpath configuration using the terminal.

- Recommended making a lib/ folder in the workspace to store the MySQL connector JAR.

- Provided the correct compile/run commands for VS Code:

Windows:
javac -cp .;lib/mysql-connector-j.jar Main.java
java  -cp .;lib/mysql-connector-j.jar Main

Mac/Linux:
javac -cp .:lib/mysql-connector-j.jar Main.java
java  -cp .:lib/mysql-connector-j.jar Main

- Suggested creating a small ConnectionTest.java file to confirm that JDBC connection, driver loading, and the app.properties file were working before building the full menu and transaction logic.

**What We Used**

- Using the manual classpath commands

- Testing the DB connection separately before integrating the entire menu program

- This made setup consistent across our team since everyone used VS Code.

**What We Changed**

- The AI recommended a directory structure similar to IntelliJ that included separate packages and src folders.

- We simplified it to a single-folder structure, which works better for VS Code and keeps classpath commands straightforward.


**What We did not use**

- The AI suggested using Maven or other automated dependency tools.

- We rejected this because our project is meant to be run as plain Java without build tools.


**What AI Got Wrong & How We Fixed It**

- AI instructions initially assumed IntelliJ-like automation for library handling. We clarified the need for VS Code instructions, and the AI then provided manual classpath steps.

- AI suggested a more complex, multi-package structure. We simplified this to avoid unnecessary complications in VS Code.