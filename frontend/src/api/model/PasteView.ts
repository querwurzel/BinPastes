
export interface PasteView {
  id: string
  title?: string
  content: string
  sizeInBytes: number
  isEncrypted: boolean
  isOneTime: boolean
  dateCreated: Date
  dateOfExpiry?: Date
  lastViewed?: Date
  views: number
}
