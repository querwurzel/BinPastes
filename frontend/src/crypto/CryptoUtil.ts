import AES from 'crypto-js/aes';
import CryptoJS from 'crypto-js/core';

function encrypt(clearText: string, key: string): string {
  return AES.encrypt(clearText, key).toString();
}

function decrypt(cipherText: string, key: string): string | null {
  try {
    const wordArray = AES.decrypt(cipherText, key);
    return wordArray.toString(CryptoJS.enc.Utf8) || null;
  } catch (_) {
    return null;
  }
}

export {
  encrypt,
  decrypt
}
