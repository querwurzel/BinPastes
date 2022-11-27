
export const toDateTimeString = (date: string | Date): string => {
  return date
    ? toTimeString(date) + ' / ' + toDateString(date)
    : null;
}

export const toDateString = (date: string | Date): string => {
  if (!date) {
    return null;
  }

  const instance = date instanceof Date ? date : toDate(date);
  return instance.toLocaleDateString(navigator.language || 'en', {day: '2-digit', month: 'long', year: 'numeric'});
}

export const toTimeString = (date: string | Date): string => {
  if (!date) {
    return null;
  }

  const instance = date instanceof Date ? date : toDate(date);
  return instance.toLocaleTimeString(navigator.language || 'en', {hour: '2-digit', minute: '2-digit'});
}

export const relativeDiffLabel = (date: string | Date): string => {
  if (!date) {
    return null;
  }

  const instance = date instanceof Date ? date : toDate(date);
  const now = new Date();
  const diff = Math.trunc((now.getTime() - instance.getTime()) / 1000); // seconds

  if (diff > 2419200) { // > 4 weeks 60 * 60 * 24 * 7 * 4
    return toDateString(instance);
  }

  if (diff < 120) { // minute 60 * 2
    return "1 minute ago"
  }
  if (diff < 3600) { // minutes 60 * 60
    return Math.trunc(diff / 60)  + " minutes ago";
  }

  if (diff < 7200) { // hour (60 * 60) * 2
    return '1 hour ago';
  }
  if (diff < 86400) { // hours (60 * 60) * 24
    return Math.trunc(diff / 3600) + ' hours ago';
  }

  if (diff < 172800) { // day (60 * 60 * 24) * 2
    return '1 day ago';
  }
  if (diff < 604800) { // days (60 * 60 * 24) * 7
    return Math.trunc(diff / 86400) + ' days ago';
  }

  if (diff < 1209600) { // week (60 * 60 * 24 * 7) * 2
    return '1 week ago';
  }
  if (diff < 2419200) { // weeks (60 * 60 * 24 * 7) * 4
    return Math.trunc(diff / 604800) + ' weeks ago';
  }
};

const toDate = (date: string): Date => {
  return new Date(Date.parse(date));
}
