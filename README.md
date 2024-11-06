# LikeHome

## Requirements
Make sure you have Java 21 installed. You can download it from [here](https://www.azul.com/downloads/?version=java-21-lts&package=jdk#zulu).

## Running from command line
1. Set the environment variables `DB_URL` and `DB_NAME` to the URL and name of the MongoDB database.
2. Run the following command:

Linux/macOS:
```shell
./gradlew
```
Windows:
```shell
gradlew
```
You should see `Started RestApplication...` in the output, which means the server is running.

## Running from IntelliJ IDEA
1. Open the file `src/main/java/com/likehome/RestApplication.java` in the editor and click the green play button next to the class definition.
2. Click on `Modify Run Configuration`
3. Click on the `Modify Options` dropdown and select `Environment Variables` under `Operating System`.
4. Add the environment variables `DB_URL` and `DB_NAME` with the URL and name of the MongoDB database and click `OK`.
5. Ensure that `RestApplication` is selected in the dropdown in the top right corner of the IntelliJ window next to the green play button.
6. Click the green play button to run the server.

Note: you can modify environment variables further by clicking on the `Edit Configurations...` option in the dropdown next to the green play button.

## Running tests
(Linux/macOS) `./gradlew test`

(Windows) `gradlew test`

IntelliJ:
1. Open the Gradle tool window on the right side of the window, navigate to `Tasks` -> `verification` -> `test` and right-click test.
2. Click on `Modify Run Configuration` and follow the same instructions with environment variables as for running the server.
3. Ensure that `LikeHome [test]` is selected in the dropdown next to the play button, and click it to run tests.

## Running with Docker

```shell
# Ensure you have the environment variables DB_URL and DB_NAME beforehand
docker build -t likehome .
docker run -e DB_URL -e DB_NAME -p 8080:8080 -d likehome
```