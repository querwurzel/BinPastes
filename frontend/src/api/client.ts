import {PasteCreateCmd} from './model/PasteCreateCmd';
import {PasteListView} from './model/PasteListView';
import {PasteView} from './model/PasteView';

const HOST_LOCALHOST = 'localhost:8080';
const HOST_DEVELOPMENT = 'localhost:3000';

const apiBaseUrl = () => {
  switch (window.location.host) {
    case HOST_DEVELOPMENT:
    case HOST_LOCALHOST:
      return 'http://localhost:8080';
    default:
      return 'https://paste.wilke-it.com';
  }
}

const createPaste = (cmd: PasteCreateCmd): Promise<PasteView> => {
  const url = new URL('/api/v1/paste', apiBaseUrl());

  return fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(cmd)
    })
    .then(resp => {
      if (resp.ok) {
        return resp.json()
      } else {
        throw new Error()
      }
    });
}

const findOne = (id: string): Promise<PasteView> => {
  const url = new URL('/api/v1/paste/' + id, apiBaseUrl());

  return fetch(url)
    .then(resp => {
      if (resp.ok) {
        return resp.json()
      } else {
        throw new Error('404')
      }
    })
}

const findAll = (): Promise<Array<PasteListView>> => {
  const url = new URL('/api/v1/paste', apiBaseUrl());

  return fetch(url)
    .then(value => value.json())
    .then(value => value.pastes);
}

const searchAll = (term: string): Promise<Array<PasteListView>> => {
  const params = new URLSearchParams([['text', term]]);
  const url = new URL('/api/v1/paste/search?' + params.toString(), apiBaseUrl());

  return fetch(url)
    .then(value => value.json())
    .then(value => value.pastes);
}

const deletePaste = (id: string) => {
  const url = new URL('/api/v1/paste/' + id, apiBaseUrl());

  return fetch(url, {
    method: 'DELETE'
  });
}

const ApiClient = {
  createPaste,
  findOne,
  findAll,
  searchAll,
  deletePaste
}

export default ApiClient;
