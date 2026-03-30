package com.cb.coaless;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import com.cb.coaless.server.AppServer;

public class Main {
    public static void main(String[] args) throws Exception {
        AppServer server = new AppServer();
        server.start(8080);
        System.out.println("Server running on http://localhost:8080");
    }
}

/*
simple-server/
├── pom.xml
└── src/main/
    ├── java/com/example/
    │   ├── Main.java
    │   ├── server/AppServer.java
    │   ├── db/JPAUtil.java
    │   ├── model/Task.java
    │   ├── repo/TaskRepository.java
    │   ├── controller/TaskController.java
    │   └── template/TemplateEngine.java
    └── resources/
        ├── META-INF/persistence.xml
        └── templates/index.html
 */