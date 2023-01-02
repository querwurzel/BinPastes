
export const toDateTimeString = (date: string): string => {
  return toTimeString(date) + ' ' + toDateString(date);
}

export const toDateString = (date: string): string => {
  const instance = toDate(date);
  return instance.toLocaleDateString('de', {day: '2-digit', month: '2-digit', year: 'numeric'});
}

export const toTimeString = (date: string): string => {
  const instance = toDate(date);
  return instance.toLocaleTimeString('de');
}

const toDate = (date: string): Date => {
  return new Date(Date.parse(date));
}
