import {PasteCreateCmd} from './model/PasteCreateCmd';
import {PasteListView} from './model/PasteListView';
import {PasteView} from './model/PasteView';
import {PasteSearchView} from './model/PasteSearchView';

function apiBaseUrl() {
  switch (window.location.host) {
    case 'localhost:3000': // development
    case 'localhost:4173': // development
      return 'http://localhost:8080';
    default:
      return window.location.origin;
  }
}

function createPaste(cmd: PasteCreateCmd): Promise<PasteView> {
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
        throw new Error(resp.status.toString())
      }
    });
}

function findOne(id: string): Promise<PasteView> {
  const url = new URL('/api/v1/paste/' + id, apiBaseUrl());

  return fetch(url)
    .then(resp => {
      if (resp.ok) {
        return resp.json()
      } else {
        throw new Error(resp.status.toString())
      }
    })
}

function findAll(): Promise<Array<PasteListView>> {
  const url = new URL('/api/v1/paste', apiBaseUrl());

  return fetch(url)
    .then(value => value.json())
    .then(value => value.pastes);
}

function searchAll(term: string): Promise<Array<PasteSearchView>> {
  const params = new URLSearchParams([['term', term]]);
  const url = new URL('/api/v1/paste/search?' + encodeURI(params.toString()), apiBaseUrl());

  return fetch(url)
    .then(value => value.json())
    .then(value => value.pastes)
    .catch(_ => [])
}

function deletePaste(id: string): Promise<void> {
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
