import AES from 'crypto-js/aes';
import CryptoJS from 'crypto-js/core';

export const encrypt = (clearText: string, key: string): string => {
  return AES.encrypt(clearText, key).toString();
}

export const decrypt = (cipherText: string, key: string): string | null => {
  const wordArray = AES.decrypt(cipherText, key);
  const clearText = wordArray.toString(CryptoJS.enc.Utf8);

  return clearText || null;
}
