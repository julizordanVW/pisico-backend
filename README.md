# RodandoVoy Backend

## Instalation

Clone the project using Git Clone

### 📌 Prerequisites

Install Amazon Correto 21

```bash
wget https://corretto.aws/downloads/latest/amazon-corretto-21-x64-linux-jdk.tar.gz
```
Or

```bash
curl -LO https://corretto.aws/downloads/latest/amazon-corretto-21-x64-linux-jdk.tar.gz
```

**Add the JDK to Your Project (IntelliJ IDEA or VS Code)**

##### 🛠️ For IntelliJ IDEA:
1. Go to **File** → **Project Structure** → **SDKs**.
2. Make sure **OpenJDK 21** is selected.
3. Apply the changes and restart the IDE if necessary.

#### 🛠️ For VS Code:
1. Install the **[Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)**.
2. Set `JAVA_HOME` in `settings.json` or run the following command:
   ```bash
   export JAVA_HOME=$(/usr/libexec/java_home -v 21)
   export PATH=$JAVA_HOME/bin:$PATH

## 📌 Deploy

To compile:
Make sure to be in `/pisico-backend` directory
```bash
docker-compose up   
```
```bash
./gradlew build   
```

To run: Just press the **run button** in `PisicoBackendApplication`

When the app is running you should see the following message in the console:
"Migrating schema "public" to version "1.1.0 - SCHEMAS", that means that the flyway migrations were successful.

You should connect to the database to the the created tables, these are in the  public schema
## Technologies


_Name the technologies used in the project._
* [Spring](https://spring.io/) - Framework Used.
* [React](https://reactjs.org/) - UI Library.
* [Hibernate](https://hibernate.org/) - ORM.

## Contributing

Describe the steps to follow if someone wants to contribute to your project.

## Documentation

Specify [where](https://es.wikipedia.org/wiki/Wikipedia:Portada) people can find more documentation about your project.

## Acknowledgments

_Mention all those who helped you build the project, inspired you etc._

* [Linus Torvalds](https://github.com/torvalds)
* [Dan Abramov](https://github.com/gaearon)

## License
Describe the project [license](https://choosealicense.com/) agreements.
