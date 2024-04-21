
export type PasteListView = {
  id: string
  title?: string
  isEncrypted: boolean
  isPermanent: boolean
  sizeInBytes: number
  dateCreated: string
  dateOfExpiry?: string
}
