import React, { useContext } from "react";

import { Notification } from "../GameTypes";
import { KingdomContext } from "../Kingdom";
import { fetchNotificationsRequest } from "../game-api-client/NotificationsApi";
import { notificationsRefreshInterval } from "../Consts";

const Notifications: React.FC = () => {
  const [notifications, setNotifications] = React.useState<Notification[]>([]);
  const kingdomContext = useContext(KingdomContext);
  // ask someone how to better solve it, null object pattern?
  if (kingdomContext === undefined) {
    throw new Error("Kingdom context is undefined");
  }

  const reloadNotifications = async () => {
    const data = await fetchNotificationsRequest();
    setNotifications(data);
  };

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
      <h1>Notifications</h1>
      <ul>
        {notifications.map(notification => (
          <li key={notification.id}>{notification.message}</li>
        ))}
      </ul>
    </div>
  );
};

export default Notifications;
