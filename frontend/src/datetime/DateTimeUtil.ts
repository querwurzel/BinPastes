
export const toDateTimeString = (date: string | Date): string => {
  if (!date) {
    return null;
  }

  const instant = toDate(date);
  return toTimeString(instant) + ' / ' + toDateString(instant);
}

export const toDateString = (date: string | Date): string => {
  if (!date) {
    return null;
  }

  const instant = toDate(date);
  return instant.toLocaleDateString(navigator.language || 'en', {day: '2-digit', month: 'long', year: 'numeric'});
}

export const toTimeString = (date: string | Date): string => {
  if (!date) {
    return null;
  }

  const instant = toDate(date);
  return instant.toLocaleTimeString(navigator.language || 'en', {hour: '2-digit', minute: '2-digit'});
}

export const relativeDiffLabel = (date: string | Date): string => {
  if (!date) {
    return null;
  }

  const instant = toDate(date);
  const now = new Date();
  const diffSeconds = Math.abs(Math.trunc((now.getTime() - instant.getTime()) / 1000));

  if (diffSeconds > 2419200) { // > 4 weeks: 60 * 60 * 24 * 7 * 4
    return toDateString(instant);
  }

  if (diffSeconds <= 1) {
    return "1 second ago"
  }
  if (diffSeconds < 60) { // < minute
    return diffSeconds + " seconds ago";
  }

  if (diffSeconds < 120) { // < 2 minutes: 60 * 2
    return "1 minute ago"
  }
  if (diffSeconds < 3600) { // < hour: 60 * 60
    return Math.trunc(diffSeconds / 60)  + " minutes ago";
  }

  if (diffSeconds < 7200) { // < 2 hours: (60 * 60) * 2
    return '1 hour ago';
  }
  if (diffSeconds < 86400) { // < day: (60 * 60) * 24
    return Math.trunc(diffSeconds / 3600) + ' hours ago';
  }

  if (diffSeconds < 172800) { // < 2 days: (60 * 60 * 24) * 2
    return '1 day ago';
  }
  if (diffSeconds < 604800) { // < week: (60 * 60 * 24) * 7
    return Math.trunc(diffSeconds / 86400) + ' days ago';
  }

  if (diffSeconds < 1209600) { // < 2 weeks: (60 * 60 * 24 * 7) * 2
    return '1 week ago';
  }
  if (diffSeconds < 2419200) { // < 4 weeks: (60 * 60 * 24 * 7) * 4
    return Math.trunc(diffSeconds / 604800) + ' weeks ago';
  }
};

const toDate = (date: string | Date): Date => {
  if (date instanceof Date) {
    return date;
  }

  return new Date(Date.parse(date));
}
