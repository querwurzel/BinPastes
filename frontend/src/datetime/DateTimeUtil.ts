
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
  return instance.toLocaleDateString('de', {day: '2-digit', month: '2-digit', year: 'numeric'});
}

export const toTimeString = (date: string | Date): string => {
  if (!date) {
    return null;
  }

  const instance = date instanceof Date ? date : toDate(date);
  return instance.toLocaleTimeString('de', {hour: '2-digit', minute: '2-digit'});
}

export const relativeDiffLabel = (date: string | Date): string => {
  if (!date) {
    return null;
  }

  const instance = date instanceof Date ? date : toDate(date);
  const now = new Date();
  const diff = Math.trunc((now.getTime() / 1000) - (instance.getTime() / 1000)); // seconds

  if (diff > 60 * 60 * 24 * 7 * 4) { // > 4 weeks
    return toDateTimeString(instance);
  }

  if (diff < 60 * 2) { // minute
    return "1 minute ago"
  }
  if (diff < 60 * 60) { // minutes
    return Math.trunc(diff / 60)  + " minutes ago";
  }

  if (diff < (60 * 60) * 2) { // hour
    return '1 hour ago';
  }
  if (diff < (60 * 60) * 24) { // hours
    return Math.trunc(diff / (60 * 60)) + ' hours ago';
  }

  if (diff < (60 * 60 * 24) * 2) { // day
    return '1 day ago';
  }
  if (diff < (60 * 60 * 24) * 7) { // days
    return Math.trunc(diff / (60 * 60 * 24)) + ' days ago';
  }

  if (diff < (60 * 60 * 24 * 7) * 2) { // week
    return '1 week ago';
  }
  if (diff < (60 * 60 * 24 * 7) * 4) { // weeks
    return Math.trunc(diff / (60 * 60 * 24 * 7)) + ' weeks ago';
  }
};

const toDate = (date: string): Date => {
  return new Date(Date.parse(date));
}
