import { initializeApp } from 'firebase/app';
import { getMessaging, getToken, onMessage } from 'firebase/messaging';
import { apiClient } from './api-config';

// Firebase configuration từ Firebase Console
const firebaseConfig = {
  apiKey: "AIzaSyDBa7_Mrv8omekOtnURmNWj6Ogq5BGgZ50",
  authDomain: "smd-syllabus-management.firebaseapp.com",
  projectId: "smd-syllabus-management",
  storageBucket: "smd-syllabus-management.firebasestorage.app",
  messagingSenderId: "724610983642",
  appId: "1:724610983642:web:a40010f090f29e0d31a7e3",
  measurementId: "G-9PF14FDSW2"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const messaging = getMessaging(app);

// VAPID key for Web Push
const VAPID_KEY = "BHyaLLjx0t-1O5dIDWB5vdkLIUTM4Wmtj-g2ddFU-H33rr2JuApwzPP9hstZeMzEDcxd8YXTy5-iWGpUUAqrKpI";

/**
 * Đăng ký FCM token và gửi lên backend
 * @param userId - ID của user hiện tại
 */
export const registerFCMToken = async (userId: string) => {
  try {
    // Kiểm tra browser support
    if (!('Notification' in window)) {
      console.warn('⚠️ Browser không hỗ trợ notifications');
      return null;
    }

    // Request notification permission
    const permission = await Notification.requestPermission();
    
    if (permission === 'granted') {
      // Get FCM token
      const token = await getToken(messaging, { vapidKey: VAPID_KEY });
      
      if (token) {
        // Gửi token lên backend
        try {
          await apiClient.put('/users/fcm-token', { token });
        } catch (error) {
          console.error('❌ Lỗi lưu FCM token:', error);
        }
        
        return token;
      } else {
        console.warn('⚠️ Không lấy được FCM token');
        return null;
      }
    } else {
      return null;
    }
  } catch (error) {
    console.error('❌ Lỗi đăng ký FCM:', error);
    return null;
  }
};

/**
 * Unregister FCM token (khi logout)
 * @param userId - ID của user hiện tại
 */
export const unregisterFCMToken = async (userId: string) => {
  try {
    // Xóa token khỏi backend
    await apiClient.delete('/users/fcm-token');
  } catch (error) {
    console.error('❌ Lỗi xóa FCM token:', error);
  }
};

export { messaging, onMessage };
