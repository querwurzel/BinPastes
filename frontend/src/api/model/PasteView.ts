
export interface PasteView {
  id: string
  title?: string
  content: string
  sizeInBytes: number
  isPublic: boolean
  isEncrypted: boolean
  isOneTime: boolean
  dateCreated: string
  dateOfExpiry?: string
  lastViewed?: string
  views: number
}
