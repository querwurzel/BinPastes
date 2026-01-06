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
    .then(resp => toJson(resp));
}

function findPaste(id: string): Promise<PasteView> {
  const url = new URL('/api/v1/paste/' + id, apiBaseUrl());

  return fetch(url)
    .then(resp => toJson(resp));
}

function findOneTimePaste(id: String): Promise<PasteView> {
  const url = new URL('/api/v1/paste/' + id, apiBaseUrl());

  return fetch(url, {
      method: 'POST'
    })
    .then(resp => toJson(resp));
}

function findAll(): Promise<Array<PasteListView>> {
  const url = new URL('/api/v1/paste', apiBaseUrl());

  return fetch(url)
    .then(resp => toJson(resp))
    .then(value => value.pastes);
}

function searchAll(term: string): Promise<Array<PasteSearchView>> {
  const url = new URL('/api/v1/paste/search', apiBaseUrl());
  url.search = new URLSearchParams({ term });

  return fetch(url)
    .then(resp => toJson(resp))
    .then(value => value.pastes)
    .catch(() => []);
}

function deletePaste(id: string): Promise<Response> {
  const url = new URL('/api/v1/paste/' + id, apiBaseUrl());

  return fetch(url, {
    method: 'DELETE'
  });
}

function toJson(resp: Response) {
  if (resp.ok) {
    return resp.json()
  }
  throw new Error(resp.status.toString())
}

const ApiClient = {
  createPaste,
  findPaste,
  findOneTimePaste,
  findAll,
  searchAll,
  deletePaste
}

export default ApiClient;
