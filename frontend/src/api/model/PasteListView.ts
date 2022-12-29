
export interface PasteListView {
  id: string
  title?: string
  sizeInBytes: number
  isEncrypted: boolean
  dateCreated: Date
  dateOfExpiry?: Date
}

export interface PasteList {
  pastes: Array<PasteListView>
}
