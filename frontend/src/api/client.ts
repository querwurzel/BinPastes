const HOST_LOCALHOST = 'localhost:8080';
const HOST_DEVELOPMENT = 'localhost:3000';

const apiBaseUrl = () => {
  switch (window.location.host) {
    case HOST_DEVELOPMENT:
    case HOST_LOCALHOST:
      return 'http://localhost:8080';
    default:
      console.log('Host:', window.location.host);
      return 'https://paste.wilke-it.com';
  }
}

export const createPaste = (cmd: CreatePasteCmd): Promise<Object> => {
  const url = new URL('/api/v1/paste', apiBaseUrl());

  return fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(cmd)
    })
    .then(value => value.json())
    .catch(reason => console.error(reason))
}

export const findOne = (id: string) => {
  const url = new URL('/api/v1/paste/' + id, apiBaseUrl());
}

export const findAll = (): Promise<any[]> => {
  const url = new URL('/api/v1/paste', apiBaseUrl());

  return fetch(url)
    .then(value => value.json())
    .then(value => value.pastes)
}

export const searchAll = (term: string): Promise<any[]> => {
  const params = new URLSearchParams([['text', term]]);
  const url = new URL('/api/v1/paste/search?' + params.toString(), apiBaseUrl());

  return fetch(url)
    .then(value => value.json())
    .then(value => value.pastes)
}

export const deletePaste = (id: string) => {
  const url = new URL('/api/v1/paste/' + id, apiBaseUrl());

  fetch(url, {
    method: 'DELETE'
  });
}
