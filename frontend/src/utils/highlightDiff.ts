export interface HighlightSegment {
  changed: boolean;
  text: string;
}

function tokenize(text: string): string[] {
  return text.split(/(\s+|[.,!?;:()[\]{}"'`~\-_/\\]+)/g).filter((token) => token.length > 0);
}

function normalizeToken(token: string): string {
  return token.trim() === '' ? token : token.toLowerCase();
}

export function buildHighlightSegments(original: string, corrected: string): HighlightSegment[] {
  if (!corrected) {
    return [];
  }

  const originalTokens = tokenize(original ?? '');
  const correctedTokens = tokenize(corrected ?? '');

  if (!originalTokens.length) {
    return [{ changed: true, text: corrected }];
  }

  const dp: number[][] = Array.from({ length: originalTokens.length + 1 }, () =>
    Array.from({ length: correctedTokens.length + 1 }, () => 0),
  );

  for (let i = 1; i <= originalTokens.length; i += 1) {
    for (let j = 1; j <= correctedTokens.length; j += 1) {
      if (normalizeToken(originalTokens[i - 1]) === normalizeToken(correctedTokens[j - 1])) {
        dp[i][j] = dp[i - 1][j - 1] + 1;
      } else {
        dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
      }
    }
  }

  const unchanged = Array.from({ length: correctedTokens.length }, () => false);
  let i = originalTokens.length;
  let j = correctedTokens.length;

  while (i > 0 && j > 0) {
    if (normalizeToken(originalTokens[i - 1]) === normalizeToken(correctedTokens[j - 1])) {
      unchanged[j - 1] = true;
      i -= 1;
      j -= 1;
    } else if (dp[i - 1][j] >= dp[i][j - 1]) {
      i -= 1;
    } else {
      j -= 1;
    }
  }

  const segments: HighlightSegment[] = [];
  correctedTokens.forEach((token, index) => {
    const changed = !unchanged[index];
    const last = segments[segments.length - 1];
    if (last && last.changed === changed) {
      last.text += token;
    } else {
      segments.push({ changed, text: token });
    }
  });

  return segments;
}
