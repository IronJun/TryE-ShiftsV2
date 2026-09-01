package com.ispw.tryeshifts.graphcontroller.gui.utilities;

import com.ispw.tryeshifts.bean.NotificationBean;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;

public class NotificationService {
        private NotificationService() {
            throw new IllegalStateException("Utility class");
        }

        public static void showNotificationPopup(Node sourceNode, List<NotificationBean> notificationBeanList, Runnable onDeleteAll) {
            ContextMenu notificationmenu = new ContextMenu();
            notificationmenu.setStyle("-fx-background-radius: 8; -fx-padding: 8;");

            if(notificationBeanList == null ||  notificationBeanList.isEmpty()){
                MenuItem emptyItem = new MenuItem("No notifications found");
                emptyItem.setDisable(true);
                notificationmenu.getItems().add(emptyItem);
            }else{
                for(NotificationBean n : notificationBeanList){
                    CustomMenuItem item = new CustomMenuItem(createNotificationNode(n));
                    item.setHideOnClick(false);
                    notificationmenu.getItems().add(item);
                }
                SeparatorMenuItem separator = new SeparatorMenuItem();
                MenuItem clearItem = new MenuItem("Delete all notifications");
                clearItem.setOnAction(e->{
                    if(onDeleteAll != null){
                        onDeleteAll.run();
                    }
                    notificationmenu.hide();
                });
                notificationmenu.getItems().addAll(separator, clearItem);
            }

            notificationmenu.show(sourceNode,javafx.geometry.Side.BOTTOM, 0, 5);
        }

        private static Node createNotificationNode(NotificationBean n){
            VBox box = new VBox();
            box.setPadding(new Insets(5,10,5,10));
            box.setMaxWidth(260);

            String icon = switch(n.getType() != null ? n.getType().toUpperCase() : ""){
                case "SHIFTS" -> "📅 ";
                case "ACCEPTED" -> "✅ ";
                default -> "🔔 ";
            };
            Label msgLabel = new Label(icon+n.getMessage());
            msgLabel.setWrapText(true);

            if (n.isRead()) {
                msgLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");
            } else {
                msgLabel.setStyle("-fx-text-fill: #000000; -fx-font-weight: bold; -fx-font-size: 12px;");
            }


            box.getChildren().addAll(msgLabel);
            return box;
        }
}
