import { useEffect, useRef } from "react";

type PollingCallback = () => void | Promise<void>;

type UsePollingOptions = {
  interval: number;
  enabled?: boolean;
};

export function usePolling(callback: PollingCallback, { interval, enabled = true }: UsePollingOptions) {
  const savedCallback = useRef<PollingCallback>();

  useEffect(() => {
    savedCallback.current = callback;
  }, [callback]);

  useEffect(() => {
    if (!enabled) {
      return;
    }

    const tick = () => {
      void savedCallback.current?.();
    };

    const id = setInterval(tick, interval);
    tick();

    return () => {
      clearInterval(id);
    };
  }, [enabled, interval]);
}
