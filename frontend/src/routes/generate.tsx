import { createRoute, redirect, useNavigate } from '@tanstack/react-router';
import { useState } from 'react';
import { Route as rootRoute } from './__root';
import { ArticleGenerateForm } from '../components/ArticleGenerateForm';
import { GenerationProgress } from '../components/GenerationProgress';
import { ErrorModal } from '../components/ErrorModal';
import { generateArticle } from '../api/articles';
import type {
  GenerateArticleRequest,
  GenerationProgressState,
  GenerationError,
  GenerationStep,
} from '../types';

export const Route = createRoute({
  getParentRoute: () => rootRoute,
  path: '/generate',
  beforeLoad: ({ context }) => {
    if (!context.isAuthenticated) {
      throw redirect({ to: '/login' });
    }
  },
  component: GeneratePage,
});

const STEP_ORDER: GenerationStep[] = ['KEYWORDS', 'SEARCH', 'CONTENT', 'IMAGE', 'FINALIZE'];
const STEP_DURATIONS: Record<GenerationStep, number> = {
  KEYWORDS: 4,
  SEARCH: 5,
  CONTENT: 12,
  IMAGE: 6,
  FINALIZE: 2,
};
const TOTAL_SECONDS = Object.values(STEP_DURATIONS).reduce((a, b) => a + b, 0);

function createInitialProgress(): GenerationProgressState {
  return {
    currentStep: 'KEYWORDS',
    stepStatus: {
      KEYWORDS: 'in-progress',
      SEARCH: 'pending',
      CONTENT: 'pending',
      IMAGE: 'pending',
      FINALIZE: 'pending',
    },
    elapsedSeconds: 0,
    estimatedTotalSeconds: TOTAL_SECONDS,
  };
}

function GeneratePage() {
  const navigate = useNavigate();
  const [isGenerating, setIsGenerating] = useState(false);
  const [progress, setProgress] = useState<GenerationProgressState | null>(null);
  const [error, setError] = useState<GenerationError | null>(null);
  const [retryCount, setRetryCount] = useState(0);
  const [lastRequest, setLastRequest] = useState<GenerateArticleRequest | null>(null);

  const MAX_RETRIES = 3;

  const startProgressSimulation = (): ReturnType<typeof setInterval> => {
    const initial = createInitialProgress();
    setProgress(initial);

    let elapsed = 0;
    let stepIndex = 0;
    let stepElapsed = 0;

    const interval = setInterval(() => {
      elapsed += 1;
      stepElapsed += 1;

      const currentStepKey = STEP_ORDER[stepIndex];
      const duration = STEP_DURATIONS[currentStepKey];

      if (stepElapsed >= duration && stepIndex < STEP_ORDER.length - 1) {
        stepIndex += 1;
        stepElapsed = 0;

        setProgress((prev) => {
          if (!prev) return prev;
          const newStatus = { ...prev.stepStatus };
          newStatus[currentStepKey] = 'completed';
          newStatus[STEP_ORDER[stepIndex]] = 'in-progress';
          return {
            ...prev,
            currentStep: STEP_ORDER[stepIndex],
            stepStatus: newStatus,
            elapsedSeconds: elapsed,
          };
        });
      } else {
        setProgress((prev) => {
          if (!prev) return prev;
          return { ...prev, elapsedSeconds: elapsed };
        });
      }
    }, 1000);

    return interval;
  };

  const handleSubmit = async (req: GenerateArticleRequest) => {
    setLastRequest(req);
    setError(null);
    setIsGenerating(true);

    const interval = startProgressSimulation();

    try {
      const response = await generateArticle(req);
      clearInterval(interval);
      setIsGenerating(false);
      await navigate({
        to: '/preview/$articleId',
        params: { articleId: response.articleId },
      });
    } catch (err) {
      clearInterval(interval);
      setIsGenerating(false);
      setProgress(null);
      setError(err as GenerationError);
    }
  };

  const handleRetry = async () => {
    if (!lastRequest) return;
    setRetryCount((c) => c + 1);
    setError(null);
    await handleSubmit(lastRequest);
  };

  const handleDismissError = () => {
    setError(null);
    setRetryCount(0);
  };

  if (error) {
    return (
      <ErrorModal
        error={error}
        onRetry={handleRetry}
        onDismiss={handleDismissError}
        retryCount={retryCount}
        maxRetries={MAX_RETRIES}
      />
    );
  }

  if (isGenerating && progress) {
    return <GenerationProgress state={progress} />;
  }

  return <ArticleGenerateForm onSubmit={handleSubmit} />;
}
