// Import Firebase scripts for Service Worker
importScripts('https://www.gstatic.com/firebasejs/10.7.1/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/10.7.1/firebase-messaging-compat.js');

// Initialize Firebase in Service Worker
firebase.initializeApp({
  apiKey: "AIzaSyDBa7_Mrv8omekOtnURmNWj6Ogq5BGgZ50",
  authDomain: "smd-syllabus-management.firebaseapp.com",
  projectId: "smd-syllabus-management",
  storageBucket: "smd-syllabus-management.firebasestorage.app",
  messagingSenderId: "724610983642",
  appId: "1:724610983642:web:a40010f090f29e0d31a7e3",
  measurementId: "G-9PF14FDSW2"
});

const messaging = firebase.messaging();

// Handle background messages (khi app không focus)
messaging.onBackgroundMessage((payload) => {
  console.log('📩 [Service Worker] Background notification received:', payload);
  
  const notificationTitle = payload.notification?.title || 'Thông báo mới';
  const notificationOptions = {
    body: payload.notification?.body || 'Bạn có thông báo mới',
    icon: '/favicon.ico',
    badge: '/favicon.ico',
    data: payload.data || {},
    tag: payload.data?.notificationId || 'default',
    requireInteraction: false,
  };
  
  // Show notification
  self.registration.showNotification(notificationTitle, notificationOptions);
});

// Handle notification click
self.addEventListener('notificationclick', (event) => {
  console.log('🔔 [Service Worker] Notification clicked:', event.notification);
  
  event.notification.close();
  
  // Get action URL from notification data
  const actionUrl = event.notification.data?.actionUrl || '/';
  
  // Open or focus the app window
  event.waitUntil(
    clients.matchAll({ type: 'window', includeUncontrolled: true }).then((clientList) => {
      // Check if there's already a window open
      for (const client of clientList) {
        if (client.url.includes(self.location.origin) && 'focus' in client) {
          client.focus();
          client.postMessage({
            type: 'NOTIFICATION_CLICKED',
            url: actionUrl,
            data: event.notification.data
          });
          return;
        }
      }
      
      // If no window is open, open a new one
      if (clients.openWindow) {
        return clients.openWindow(actionUrl);
      }
    })
  );
});

console.log('✅ [Service Worker] Firebase Messaging Service Worker loaded');
