export type AffiliatePlatform = 'AMAZON';
export type ArticleType = '一般' | 'アフィリエイト' | '有料(500円)';

export interface GenerateArticleRequest {
  theme: string;
  affiliatePlatforms: AffiliatePlatform[];
  targetPainPoint?: string;
  targetIdealState?: string;
  storyTrigger?: string;
  uniqueInsight?: string;
  articleType?: ArticleType;
  ctaInfo?: string;
}

export interface AffiliateLinkDto {
  url: string;
  trackingId: string;
  platform: AffiliatePlatform;
  productName: string;
  price: number;
}

export interface GenerateArticleResponse {
  articleId: string;
  title: string;
  content: string;
  imageUrl: string;
  affiliateLinks: AffiliateLinkDto[];
  status: string;
}

export interface ArticleSummaryResponse {
  articleId: string;
  title: string;
  status: string;
}

export type GenerationStep = 'KEYWORDS' | 'SEARCH' | 'CONTENT' | 'IMAGE' | 'FINALIZE';
export type GenerationStepStatus = 'pending' | 'in-progress' | 'completed' | 'error';

export interface GenerationProgressState {
  currentStep: GenerationStep;
  stepStatus: Record<GenerationStep, GenerationStepStatus>;
  elapsedSeconds: number;
  estimatedTotalSeconds: number;
}

export type GenerationErrorCode =
  | 'VALIDATION_ERROR'
  | 'UNAUTHORIZED'
  | 'AI_SERVICE_UNAVAILABLE'
  | 'AFFILIATE_API_UNAVAILABLE'
  | 'INTERNAL_SERVER_ERROR';

export interface GenerationError {
  code: GenerationErrorCode;
  message: string;
  retryable: boolean;
}

export interface AuthResponse {
  token: string;
  userId: string;
  email: string;
  name: string;
}

export interface AuthError {
  code: 'USER_ALREADY_EXISTS' | 'INVALID_CREDENTIALS' | 'VALIDATION_ERROR';
  message: string;
}
