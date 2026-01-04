import { useState, useEffect, useCallback, useRef } from 'react';
import { aiService, TaskStatusResponse } from '../services/aiService';
import { message as antdMessage } from 'antd';

interface UseTaskPollingOptions {
  taskId: string | null;
  onSuccess?: (result: any) => void;
  onError?: (error: string) => void;
  enabled?: boolean;
}

interface UseTaskPollingResult {
  status: TaskStatusResponse['status'] | null;
  progress: number;
  result: any;
  error: string | null;
  isPolling: boolean;
}

/**
 * Custom hook để poll task status với exponential backoff
 * - 0-5s: poll mỗi 1 giây
 * - 5-15s: poll mỗi 2 giây  
 * - 15-30s: poll mỗi 5 giây
 * - Timeout sau 30 giây
 */
export function useTaskPolling({
  taskId,
  onSuccess,
  onError,
  enabled = true,
}: UseTaskPollingOptions): UseTaskPollingResult {
  const [status, setStatus] = useState<TaskStatusResponse['status'] | null>(null);
  const [progress, setProgress] = useState<number>(0);
  const [result, setResult] = useState<any>(null);
  const [error, setError] = useState<string | null>(null);
  const [isPolling, setIsPolling] = useState<boolean>(false);
  
  const startTimeRef = useRef<number>(0);
  const timeoutIdRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const pollingCountRef = useRef<number>(0);

  const pollTask = useCallback(async () => {
    if (!taskId || !enabled) return;

    try {
      const response = await aiService.getTaskStatus(taskId);
      
      setStatus(response.status);
      setProgress(response.progress || 0);
      
      // Check terminal states
      if (response.status === 'SUCCESS') {
        setResult(response.result);
        setIsPolling(false);
        onSuccess?.(response.result);
        antdMessage.success('AI analysis completed!');
        return;
      }
      
      if (response.status === 'FAILED') {
        const errorMsg = response.error || 'Task failed';
        setError(errorMsg);
        setIsPolling(false);
        onError?.(errorMsg);
        antdMessage.error(`Task failed: ${errorMsg}`);
        return;
      }
      
      if (response.status === 'NOT_FOUND') {
        const errorMsg = 'Task not found';
        setError(errorMsg);
        setIsPolling(false);
        onError?.(errorMsg);
        antdMessage.error(errorMsg);
        return;
      }
      
      // Continue polling if QUEUED or PROCESSING
      scheduleNextPoll();
      
    } catch (err: any) {
      console.error('Error polling task:', err);
      const errorMsg = err.response?.data?.message || err.message || 'Unknown error';
      setError(errorMsg);
      setIsPolling(false);
      onError?.(errorMsg);
      antdMessage.error(`Error: ${errorMsg}`);
    }
  }, [taskId, enabled, onSuccess, onError]);

  const scheduleNextPoll = useCallback(() => {
    const elapsedSeconds = (Date.now() - startTimeRef.current) / 1000;
    pollingCountRef.current += 1;
    
    // Timeout after 30 seconds
    if (elapsedSeconds > 30) {
      setError('Task timeout after 30 seconds');
      setIsPolling(false);
      onError?.('Task timeout');
      antdMessage.warning('Task is taking longer than expected');
      return;
    }
    
    // Exponential backoff
    let delay: number;
    if (elapsedSeconds < 5) {
      delay = 1000; // 1 second
    } else if (elapsedSeconds < 15) {
      delay = 2000; // 2 seconds
    } else {
      delay = 5000; // 5 seconds
    }
    
    timeoutIdRef.current = setTimeout(pollTask, delay);
  }, [pollTask, onError]);

  // Start polling when taskId changes
  useEffect(() => {
    if (taskId && enabled) {
      setStatus(null);
      setProgress(0);
      setResult(null);
      setError(null);
      setIsPolling(true);
      startTimeRef.current = Date.now();
      pollingCountRef.current = 0;
      
      // Start first poll immediately
      pollTask();
    }
    
    // Cleanup
    return () => {
      if (timeoutIdRef.current) {
        clearTimeout(timeoutIdRef.current);
        timeoutIdRef.current = null;
      }
    };
  }, [taskId, enabled, pollTask]);

  return {
    status,
    progress,
    result,
    error,
    isPolling,
  };
}

export default useTaskPolling;
