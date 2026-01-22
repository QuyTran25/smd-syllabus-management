import { initializeApp } from 'firebase/app';
import { getMessaging, getToken, onMessage } from 'firebase/messaging';
import axios from 'axios';

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
 */
export const registerFCMToken = async () => {
  try {
    // Kiểm tra browser support
    if (!('Notification' in window)) {
      console.warn('⚠️ Browser không hỗ trợ notifications');
      return null;
    }

    // Request notification permission
    const permission = await Notification.requestPermission();
    
    if (permission === 'granted') {
      console.log('✅ Notification permission granted');
      
      // Get FCM token
      const token = await getToken(messaging, { vapidKey: VAPID_KEY });
      
      if (token) {
        console.log('✅ FCM Token:', token);
        
        // Gửi token lên backend
        try {
          await axios.put('/api/users/fcm-token', { token });
          console.log('✅ FCM Token đã lưu vào backend');
        } catch (error) {
          console.error('❌ Lỗi lưu FCM token:', error);
        }
        
        return token;
      } else {
        console.warn('⚠️ Không lấy được FCM token');
        return null;
      }
    } else {
      console.log('❌ User từ chối notification permission');
      return null;
    }
  } catch (error) {
    console.error('❌ Lỗi đăng ký FCM:', error);
    return null;
  }
};

/**
 * Unregister FCM token (khi logout)
 */
export const unregisterFCMToken = async () => {
  try {
    // Xóa token khỏi backend
    await axios.delete('/api/users/fcm-token');
    console.log('✅ FCM Token đã xóa khỏi backend');
  } catch (error) {
    console.error('❌ Lỗi xóa FCM token:', error);
  }
};

export { messaging, onMessage };
