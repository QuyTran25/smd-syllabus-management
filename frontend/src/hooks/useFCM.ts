import { useEffect, useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { App } from 'antd';
import { registerFCMToken, messaging, onMessage } from '@/config/firebase';
import type { MessagePayload } from 'firebase/messaging';

/**
 * Hook to manage Firebase Cloud Messaging
 * - Registers FCM token on mount
 * - Listens for foreground messages
 * - Auto-refreshes notification list when receiving new notifications
 */
export const useFCM = (isAuthenticated: boolean, userId?: string) => {
  const [fcmToken, setFcmToken] = useState<string | null>(null);
  const queryClient = useQueryClient();
  const { notification } = App.useApp();

  useEffect(() => {
    if (!isAuthenticated || !userId) {
      console.log('⏭️ User not authenticated or userId not available, skipping FCM registration');
      return;
    }

    // Register FCM token when user is authenticated
    const initFCM = async () => {
      try {
        console.log('🔔 Initializing FCM...');
        const token = await registerFCMToken(userId);
        
        if (token) {
          setFcmToken(token);
          console.log('✅ FCM initialized successfully');
        } else {
          console.warn('⚠️ FCM token registration failed');
        }
      } catch (error) {
        console.error('❌ Error initializing FCM:', error);
      }
    };

    initFCM();

    // Listen for foreground messages (when app is open)
    const unsubscribe = onMessage(messaging, (payload: MessagePayload) => {
      console.log('📨 Foreground notification received:', payload);

      // Extract notification data
      const title = payload.notification?.title || 'Thông báo mới';
      const body = payload.notification?.body || '';

      // Show Ant Design notification
      notification.info({
        message: title,
        description: body,
        duration: 5,
        placement: 'topRight',
        onClick: () => {
          // Navigate to action URL if available
          if (payload.data?.actionUrl) {
            window.location.href = payload.data.actionUrl;
          }
        },
      });

      // Refresh notifications list to show new notification
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
      queryClient.invalidateQueries({ queryKey: ['student-notifications'] });
      
      console.log('✅ Notification list refreshed');
    });

    // Cleanup listener on unmount
    return () => {
      console.log('🧹 Cleaning up FCM listener');
      unsubscribe();
    };
  }, [isAuthenticated, userId, queryClient, notification]);

  return { fcmToken };
};
