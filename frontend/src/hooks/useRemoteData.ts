import { useEffect, useEffectEvent, useState, type DependencyList } from 'react';

interface RemoteDataState<T> {
  data: T | null;
  error: string | null;
  loading: boolean;
  reload: () => Promise<void>;
}

export function useRemoteData<T>(
  load: () => Promise<T>,
  dependencies: DependencyList,
): RemoteDataState<T> {
  const [data, setData] = useState<T | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const runLoad = useEffectEvent(async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await load();
      setData(response);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : '데이터를 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  });

  useEffect(() => {
    void runLoad();
  }, dependencies);

  return { data, error, loading, reload: runLoad };
}
