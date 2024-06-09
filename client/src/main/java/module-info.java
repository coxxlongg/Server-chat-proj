module kz.timka {
    requires javafx.controls;
    requires javafx.fxml;

    opens kz.ori.client to javafx.fxml;
    exports kz.ori.client;
}