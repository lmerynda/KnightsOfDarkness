import React, { useContext } from "react";
import { Notification } from "../GameTypes";
import { KingdomContext } from "../Kingdom";
import { fetchNotificationsRequest, markNotificationAsRead } from "../game-api-client/NotificationsApi";
import { notificationsRefreshInterval } from "../Consts";
import { List, ListItem, ListItemText, Typography } from "@mui/material";
import { useTheme } from "@mui/material/styles";

const Notifications: React.FC = () => {
  const [notifications, setNotifications] = React.useState<Notification[]>([]);
  const kingdomContext = useContext(KingdomContext);
  const theme = useTheme();

  if (kingdomContext === undefined) {
    throw new Error("Kingdom context is undefined");
  }

  const reloadNotifications = React.useCallback(async () => {
    const data = await fetchNotificationsRequest();
    setNotifications(data);
  }, []);

  React.useEffect(() => {
    reloadNotifications();
    const interval = setInterval(() => {
      reloadNotifications();
    }, notificationsRefreshInterval);

    return () => {
      clearInterval(interval);
    };
  }, [reloadNotifications]);

  return (
    <div>
      <Typography variant="h4">Notifications</Typography>
      <List>
        {notifications.map(notification => (
          <ListItem
            key={notification.id}
            onClick={async () => {
              await markNotificationAsRead(notification.id);
              reloadNotifications();
            }}
            sx={{
              backgroundColor: notification.isRead ? theme.palette.background.paper : theme.palette.action.hover,
              fontWeight: notification.isRead ? "normal" : "bold",
              cursor: "pointer",
              ":hover": {
                backgroundColor: notification.isRead ? theme.palette.action.selected : theme.palette.primary.dark,
              },
            }}
          >
            <ListItemText primary={notification.message} />
          </ListItem>
        ))}
      </List>
    </div>
  );
};

export default Notifications;
