import dayjs from 'dayjs';
import { DATE_FORMATS } from '@/constants';

export const formatDate = (date: string | Date): string => {
  if (!date) return '';
  return dayjs(date).format(DATE_FORMATS.DISPLAY);
};

export const formatDateTime = (date: string | Date): string => {
  if (!date) return '';
  return dayjs(date).format(DATE_FORMATS.DISPLAY_TIME);
};

export const truncateText = (text: string, maxLength: number): string => {
  if (!text || text.length <= maxLength) return text;
  return `${text.substring(0, maxLength)}...`;
};

export const isValidEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};
